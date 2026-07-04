package com.automobile.project.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "vehicle_booking")
public class VehicleBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The vehicle the customer wants to book
    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    // The user account that made the booking
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // PENDING, APPROVED, REJECTED
    @Column(nullable = false)
    private String status = "PENDING";

    @Column(nullable = false)
    private LocalDate bookingDate = LocalDate.now();

    private String notes;
}
