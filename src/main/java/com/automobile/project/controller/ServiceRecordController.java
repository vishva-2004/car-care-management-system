package com.automobile.project.controller;

import com.automobile.project.entity.*;
import com.automobile.project.repository.*;
import com.automobile.project.service.NotificationService;
import com.automobile.project.service.ServiceRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/serviceRecords")
public class ServiceRecordController {

    @Autowired private ServiceRecordService service;
    @Autowired private ServiceRecordRepository serviceRecordRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SaleRepository saleRepository;
    @Autowired private NotificationService notificationService;

    // Staff/Manager/Admin: log a completed service directly
    @PostMapping
    public ServiceRecord add(@RequestBody ServiceRecord record) {
        return service.save(record);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(toDtoList(serviceRecordRepository.findAll()));
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPending() {
        return ResponseEntity.ok(toDtoList(serviceRecordRepository.findByStatus("PENDING")));
    }

    @GetMapping("/approved")
    public ResponseEntity<?> getApproved() {
        return ResponseEntity.ok(toDtoList(serviceRecordRepository.findByStatus("APPROVED")));
    }

    // Customer: request a doorstep service
    @PostMapping("/request")
    public ResponseEntity<?> requestService(@RequestBody Map<String, Object> body, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long vehicleId = Long.parseLong(body.get("vehicleId").toString());
        String description = body.getOrDefault("description", "").toString();

        boolean owns = saleRepository.findByUserId(user.getId()).stream()
                .anyMatch(s -> s.getVehicle().getId().equals(vehicleId));
        if (!owns) {
            return ResponseEntity.badRequest().body(Map.of("message", "You can only request service for a vehicle you own."));
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));

        ServiceRecord record = new ServiceRecord();
        record.setVehicle(vehicle);
        record.setUser(user);
        record.setDescription(description.isBlank() ? "Service requested by customer" : description);
        record.setServiceDate(java.time.LocalDate.now());
        record.setStatus("PENDING");
        serviceRecordRepository.save(record);

        // Notify ALL staff, admin, manager
        notificationService.notifyAllStaff("🔧 New service request from " + user.getUsername()
                + " for their " + vehicle.getBrand() + " " + vehicle.getModel()
                + ": \"" + record.getDescription() + "\". Doorstep pickup required.");

        return ResponseEntity.ok(Map.of("message", "Service request submitted! Our staff will contact you for doorstep pickup."));
    }

    // Customer: view their own service records
    @GetMapping("/my")
    public ResponseEntity<?> myRecords(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(toDtoList(
                serviceRecordRepository.findByUserIdOrderByServiceDateDesc(user.getId())));
    }

    // Manager/Admin: approve a pending service request
    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approve(@PathVariable Long id) {
        ServiceRecord record = serviceRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service record not found"));
        record.setStatus("APPROVED");
        serviceRecordRepository.save(record);

        notificationService.notifyUser(record.getUser().getId(),
                "✅ Your service request for " + record.getVehicle().getBrand() + " "
                + record.getVehicle().getModel()
                + " has been approved. Our staff will come to your location for pickup.");

        return ResponseEntity.ok(Map.of("message", "Service request approved."));
    }

    // Manager/Admin: reject with reason
    @PostMapping("/reject/{id}")
    public ResponseEntity<?> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        ServiceRecord record = serviceRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service record not found"));
        record.setStatus("REJECTED");
        serviceRecordRepository.save(record);

        String reason = (body != null && body.get("reason") != null && !body.get("reason").isBlank())
                ? body.get("reason")
                : "Service could not be scheduled at this time.";

        notificationService.notifyUser(record.getUser().getId(),
                "❌ Your service request for " + record.getVehicle().getBrand() + " "
                + record.getVehicle().getModel() + " was not approved. Reason: " + reason);

        return ResponseEntity.ok(Map.of("message", "Service request rejected."));
    }

    // Staff/Manager/Admin: mark completed with notes on what was done
    @PostMapping("/complete/{id}")
    public ResponseEntity<?> complete(@PathVariable Long id, @RequestBody Map<String, String> body) {
        ServiceRecord record = serviceRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Service record not found"));
        String notes = body.get("description");
        if (notes != null && !notes.isBlank()) {
            record.setDescription(notes);
        }
        record.setServiceDate(java.time.LocalDate.now());
        record.setStatus("COMPLETED");
        serviceRecordRepository.save(record);

        notificationService.notifyUser(record.getUser().getId(),
                "🔧 Service completed for your " + record.getVehicle().getBrand() + " "
                + record.getVehicle().getModel() + ": " + record.getDescription()
                + ". Your car is ready and will be delivered to your doorstep.");

        return ResponseEntity.ok(Map.of("message", "Service marked as completed."));
    }

    private List<Map<String, Object>> toDtoList(List<ServiceRecord> records) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ServiceRecord r : records) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("description", r.getDescription());
            m.put("serviceDate", r.getServiceDate());
            m.put("status", r.getStatus());
            m.put("vehicleId", r.getVehicle().getId());
            m.put("brand", r.getVehicle().getBrand());
            m.put("model", r.getVehicle().getModel());
            m.put("username", r.getUser().getUsername());
            result.add(m);
        }
        return result;
    }
}
