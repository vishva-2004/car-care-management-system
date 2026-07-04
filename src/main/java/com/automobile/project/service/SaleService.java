package com.automobile.project.service;

import com.automobile.project.entity.*;
import com.automobile.project.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Service
public class SaleService {

    @Autowired private SaleRepository saleRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private VehicleBookingRepository bookingRepository;
    @Autowired private NotificationService notificationService;

    public Sale allocate(Long vehicleId, Long userId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found with ID: " + vehicleId));

        if (vehicle.getReadyStock() == null || vehicle.getReadyStock() <= 0)
            throw new RuntimeException("Vehicle out of stock");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Check one-car rule
        if (!saleRepository.findByUserId(userId).isEmpty())
            throw new RuntimeException("This customer already owns a vehicle. Each customer can hold one vehicle.");

        vehicle.setReadyStock(vehicle.getReadyStock() - 1);
        if (vehicle.getReadyStock() == 0) vehicle.setAvailability("Sold");
        else if (vehicle.getReadyStock() <= 3) vehicle.setAvailability("LIMITED");
        vehicleRepository.save(vehicle);

        Sale sale = new Sale();
        sale.setVehicle(vehicle);
        sale.setUser(user);
        sale.setSaleDate(java.time.LocalDate.now());
        Sale savedSale = saleRepository.save(sale);

        // Auto-approve matching pending booking
        Optional<VehicleBooking> pendingBooking = bookingRepository
                .findByVehicleIdAndUserIdAndStatus(vehicle.getId(), user.getId(), "PENDING")
                .stream().findFirst();
        pendingBooking.ifPresent(booking -> {
            booking.setStatus("APPROVED");
            bookingRepository.save(booking);
        });

        notificationService.notifyUser(user.getId(),
                "🎉 Congratulations! Your " + vehicle.getBrand() + " " + vehicle.getModel()
                + " has been allocated to your account. You can see it in My Vehicles.");

        return savedSale;
    }
}
