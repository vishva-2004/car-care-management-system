package com.automobile.project.controller;

import com.automobile.project.entity.*;
import com.automobile.project.repository.*;
import com.automobile.project.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/booking")
public class VehicleBookingController {

    @Autowired private VehicleBookingRepository bookingRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SaleRepository saleRepository;
    @Autowired private NotificationService notificationService;

    @PostMapping("/request")
    public ResponseEntity<?> requestBooking(@RequestBody Map<String, Object> body, Authentication auth) {
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // One car per customer rule
        List<Sale> existing = saleRepository.findByUserId(user.getId());
        if (!existing.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "message", "You already own a vehicle. Each customer account can hold one vehicle at a time."
            ));
        }

        Long vehicleId = Long.parseLong(body.get("vehicleId").toString());
        String notes = body.getOrDefault("notes", "").toString();

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        if (!"AVAILABLE".equalsIgnoreCase(vehicle.getAvailability()) && !"LIMITED".equalsIgnoreCase(vehicle.getAvailability())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vehicle is not available for booking."));
        }

        if (bookingRepository.existsByVehicleIdAndUserIdAndStatus(vehicleId, user.getId(), "PENDING")) {
            return ResponseEntity.badRequest().body(Map.of("message", "You already have a pending booking for this vehicle."));
        }

        VehicleBooking booking = new VehicleBooking();
        booking.setVehicle(vehicle);
        booking.setUser(user);
        booking.setNotes(notes);
        booking.setStatus("PENDING");
        bookingRepository.save(booking);

        // Notify all managers and admins
        notificationService.notifyAllManagers("📋 New booking request: " + username
                + " wants to book " + vehicle.getBrand() + " " + vehicle.getModel()
                + ". Go to Allocate Vehicles to process it.");

        return ResponseEntity.ok(Map.of("message", "Booking request submitted! The manager will review and allocate your car."));
    }

    @GetMapping("/my")
    public ResponseEntity<?> myBookings(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<VehicleBooking> bookings = bookingRepository.findByUserId(user.getId());
        List<Map<String, Object>> result = new ArrayList<>();
        for (VehicleBooking b : bookings) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", b.getId());
            m.put("vehicleId", b.getVehicle().getId());
            m.put("brand", b.getVehicle().getBrand());
            m.put("model", b.getVehicle().getModel());
            m.put("price", b.getVehicle().getPrice());
            m.put("fuelType", b.getVehicle().getFuelType());
            m.put("status", b.getStatus());
            m.put("bookingDate", b.getBookingDate());
            m.put("notes", b.getNotes());
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingBookings() {
        List<VehicleBooking> pending = bookingRepository.findByStatus("PENDING");
        List<Map<String, Object>> result = new ArrayList<>();
        for (VehicleBooking b : pending) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", b.getId());
            m.put("username", b.getUser().getUsername());
            m.put("userId", b.getUser().getId());
            m.put("vehicleId", b.getVehicle().getId());
            m.put("brand", b.getVehicle().getBrand());
            m.put("model", b.getVehicle().getModel());
            m.put("price", b.getVehicle().getPrice());
            m.put("bookingDate", b.getBookingDate());
            m.put("notes", b.getNotes());
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<?> rejectBooking(@PathVariable Long id) {
        VehicleBooking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        booking.setStatus("REJECTED");
        bookingRepository.save(booking);

        notificationService.notifyUser(booking.getUser().getId(),
                "Your booking request for " + booking.getVehicle().getBrand()
                + " " + booking.getVehicle().getModel()
                + " was not approved this time. You can browse other vehicles and submit a new request.");

        return ResponseEntity.ok(Map.of("message", "Booking rejected."));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllBookings() {
        List<VehicleBooking> all = bookingRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (VehicleBooking b : all) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", b.getId());
            m.put("username", b.getUser().getUsername());
            m.put("brand", b.getVehicle().getBrand());
            m.put("model", b.getVehicle().getModel());
            m.put("price", b.getVehicle().getPrice());
            m.put("status", b.getStatus());
            m.put("bookingDate", b.getBookingDate());
            m.put("notes", b.getNotes());
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }
}
