package com.automobile.project.repository;

import com.automobile.project.entity.VehicleBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleBookingRepository extends JpaRepository<VehicleBooking, Long> {
    List<VehicleBooking> findByStatus(String status);
    List<VehicleBooking> findByUserId(Long userId);
    boolean existsByVehicleIdAndUserIdAndStatus(Long vehicleId, Long userId, String status);
    List<VehicleBooking> findByVehicleIdAndUserIdAndStatus(Long vehicleId, Long userId, String status);
}
