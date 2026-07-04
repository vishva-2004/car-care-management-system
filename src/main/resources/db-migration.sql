-- ============================================================
-- Car Care DB Migration — Run these if upgrading existing DB
-- ============================================================

-- 1. Booking table (NEW — required for vehicle booking feature)
CREATE TABLE IF NOT EXISTS vehicle_booking (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id    BIGINT NOT NULL,
    user_id       BIGINT NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    booking_date  DATE NOT NULL,
    notes         TEXT,
    CONSTRAINT fk_booking_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicle(id),
    CONSTRAINT fk_booking_user    FOREIGN KEY (user_id)    REFERENCES users(id)
);

-- 2. These were already in old version but listed here for completeness
-- ALTER TABLE users ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'APPROVED';
-- ALTER TABLE users ADD COLUMN IF NOT EXISTS requested_role VARCHAR(50);
-- ALTER TABLE customer ADD COLUMN IF NOT EXISTS user_id BIGINT NULL;

-- ============================================================
-- IMPORTANT: Run this if you see "customer_id doesn't have a default value"
-- This happens because the old sale table still has customer_id NOT NULL
-- ============================================================

-- Step 1: Drop the foreign key on customer_id (find the name first if needed)
-- Run this to find the FK name: SHOW CREATE TABLE sale;
-- Then drop it: ALTER TABLE sale DROP FOREIGN KEY <fk_name>;

-- Step 2: Make customer_id nullable so old rows don't break
ALTER TABLE sale MODIFY COLUMN customer_id BIGINT NULL DEFAULT NULL;

-- Step 3: Add user_id column if not already there
ALTER TABLE sale ADD COLUMN IF NOT EXISTS user_id BIGINT NULL;

-- Step 4: Add foreign key for user_id
ALTER TABLE sale ADD CONSTRAINT fk_sale_user FOREIGN KEY (user_id) REFERENCES users(id);

-- Step 5: Repeat for service_record
ALTER TABLE service_record MODIFY COLUMN customer_id BIGINT NULL DEFAULT NULL;
ALTER TABLE service_record ADD COLUMN IF NOT EXISTS user_id BIGINT NULL;
ALTER TABLE service_record ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED';
ALTER TABLE service_record ADD CONSTRAINT fk_sr_user FOREIGN KEY (user_id) REFERENCES users(id);

-- ============================================================
--    straight at the users table. Run these once after pulling
--    this update (back up your data first if it matters to you).
-- ============================================================

-- Add the new user_id columns (nullable at first so we can backfill)
ALTER TABLE sale ADD COLUMN IF NOT EXISTS user_id BIGINT NULL;
ALTER TABLE service_record ADD COLUMN IF NOT EXISTS user_id BIGINT NULL;
ALTER TABLE service_record ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED';

-- Backfill user_id from the old customer -> user link, where it exists
UPDATE sale s
JOIN customer c ON s.customer_id = c.id
SET s.user_id = c.user_id
WHERE c.user_id IS NOT NULL;

UPDATE service_record sr
JOIN customer c ON sr.customer_id = c.id
SET sr.user_id = c.user_id
WHERE c.user_id IS NOT NULL;

-- Any sale/service_record rows that could NOT be backfilled (customer was never
-- linked to a login account) will have user_id still NULL — review and either
-- delete those orphaned rows or link them manually before continuing, e.g.:
-- SELECT * FROM sale WHERE user_id IS NULL;
-- SELECT * FROM service_record WHERE user_id IS NULL;

-- Once every row has a user_id, make the column required and drop the old ones:
-- ALTER TABLE sale MODIFY user_id BIGINT NOT NULL;
-- ALTER TABLE sale DROP FOREIGN KEY <fk_name_if_any>;
-- ALTER TABLE sale DROP COLUMN customer_id;
-- ALTER TABLE sale ADD CONSTRAINT fk_sale_user FOREIGN KEY (user_id) REFERENCES users(id);

-- ALTER TABLE service_record MODIFY user_id BIGINT NOT NULL;
-- ALTER TABLE service_record DROP FOREIGN KEY <fk_name_if_any>;
-- ALTER TABLE service_record DROP COLUMN customer_id;
-- ALTER TABLE service_record ADD CONSTRAINT fk_service_user FOREIGN KEY (user_id) REFERENCES users(id);

-- Finally, drop the customer table itself (only after the above is done and verified):
-- DROP TABLE customer;
