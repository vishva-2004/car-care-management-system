package com.automobile.project.controller;

import com.automobile.project.entity.Role;
import com.automobile.project.entity.User;
import com.automobile.project.entity.Sale;
import com.automobile.project.entity.ServiceRecord;
import com.automobile.project.repository.RoleRepository;
import com.automobile.project.repository.UserRepository;
import com.automobile.project.repository.SaleRepository;
import com.automobile.project.repository.ServiceRecordRepository;
import com.automobile.project.repository.NotificationRepository;
import com.automobile.project.repository.VehicleBookingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private SaleRepository saleRepository;
    @Autowired private ServiceRecordRepository serviceRecordRepository;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private VehicleBookingRepository bookingRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping("/me")
    public ResponseEntity<?> getMe(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Map<String, Object> m = new HashMap<>();
        m.put("id", user.getId());
        m.put("username", user.getUsername());
        m.put("status", user.getStatus());
        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName().replace("ROLE_", "")).toList();
        m.put("roles", roles);
        m.put("role", roles.isEmpty() ? "" : roles.get(0));
        return ResponseEntity.ok(m);
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : users) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("status", u.getStatus());
            m.put("requestedRole", u.getRequestedRole());
            m.put("roles", u.getRoles().stream()
                    .map(r -> r.getName().replace("ROLE_", "")).toList());
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    // Returns only users with the CUSTOMER role and APPROVED status — used for
    // the manager's "allocate vehicle" picker.
    @GetMapping("/customers")
    public ResponseEntity<?> getCustomerUsers() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : users) {
            boolean isCustomer = u.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_CUSTOMER"));
            if (isCustomer && "APPROVED".equals(u.getStatus())) {
                Map<String, Object> m = new HashMap<>();
                m.put("id", u.getId());
                m.put("username", u.getUsername());
                result.add(m);
            }
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingUsers() {
        List<User> pending = userRepository.findByStatus("PENDING");
        List<Map<String, Object>> result = new ArrayList<>();
        for (User u : pending) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("requestedRole", u.getRequestedRole());
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/approve/{id}")
    public ResponseEntity<?> approveUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!"PENDING".equals(user.getStatus()))
            return ResponseEntity.badRequest().body("User is not pending approval");

        String requestedRole = user.getRequestedRole();
        if (requestedRole == null || requestedRole.isBlank()) {
            requestedRole = "ROLE_STAFF";
        } else if (!requestedRole.startsWith("ROLE_")) {
            requestedRole = "ROLE_" + requestedRole;
        }

        Optional<Role> roleOpt = roleRepository.findByName(requestedRole);
        if (roleOpt.isEmpty()) {
            roleOpt = roleRepository.findByName(requestedRole.replace("ROLE_", ""));
        }
        if (roleOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(
                "Role not found: " + requestedRole + ". Make sure roles are initialized in the database.");
        }

        Set<Role> updatedRoles = new HashSet<>();
        updatedRoles.add(roleOpt.get());
        user.setRoles(updatedRoles);
        user.setStatus("APPROVED");
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message",
                "User " + user.getUsername() + " approved as " + requestedRole.replace("ROLE_", "")));
    }

    @PostMapping("/reject/{id}")
    public ResponseEntity<?> rejectUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setStatus("REJECTED");
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "User " + user.getUsername() + " rejected."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            // Delete related records first to avoid FK constraint errors
            notificationRepository.deleteAll(notificationRepository.findByUserIdOrderByCreatedAtDesc(id));
            bookingRepository.deleteAll(bookingRepository.findByUserId(id));
            List<Sale> sales = saleRepository.findByUserId(id);
            if (!sales.isEmpty()) {
                // Delete service records linked to this user's sales first
                serviceRecordRepository.deleteAll(serviceRecordRepository.findByUserId(id));
                saleRepository.deleteAll(sales);
            }
            userRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Delete failed: " + e.getMessage()));
        }
    }

    @GetMapping("/{input}")
    public ResponseEntity<?> getUser(@PathVariable String input) {
        User user;
        if (input.matches("\\d+")) {
            user = userRepository.findById(Long.parseLong(input))
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } else {
            user = userRepository.findByUsername(input)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        Map<String, Object> m = new HashMap<>();
        m.put("id", user.getId());
        m.put("username", user.getUsername());
        m.put("status", user.getStatus());
        m.put("roles", user.getRoles().stream()
                .map(r -> r.getName().replace("ROLE_", "")).toList());
        return ResponseEntity.ok(m);
    }

    // Manager only — resets any user's password (admin/staff/customer).
    @PostMapping("/reset-password/{id}")
    public ResponseEntity<?> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "New password is required."));
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message",
                "Password for " + user.getUsername() + " has been reset successfully."));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, Authentication auth) {
        String currentPassword = body.get("currentPassword");
        String newPassword     = body.get("newPassword");
        String confirmPassword = body.get("confirmPassword");

        if (currentPassword == null || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "All fields are required."));
        }
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "New password must be at least 6 characters."));
        }
        if (confirmPassword != null && !newPassword.equals(confirmPassword)) {
            return ResponseEntity.badRequest().body(Map.of("message", "New password and confirm password do not match."));
        }

        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Current password is incorrect."));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message",
                "Password changed successfully! Please log out and log in again with your new password."));
    }

    // Customer dashboard: vehicles owned (via Sale) + service history, directly off the User.
    @GetMapping("/my-vehicles")
    public ResponseEntity<?> getMyVehicles(Authentication auth) {
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Sale> sales = saleRepository.findByUser(user);
        List<Map<String, Object>> vehicleList = new ArrayList<>();

        for (Sale s : sales) {
            Map<String, Object> m = new HashMap<>();
            m.put("saleId",    s.getId());
            m.put("saleDate",  s.getSaleDate());
            m.put("vehicleId", s.getVehicle().getId());
            m.put("brand",     s.getVehicle().getBrand());
            m.put("model",     s.getVehicle().getModel());
            m.put("fuelType",  s.getVehicle().getFuelType());
            m.put("price",     s.getVehicle().getPrice());

            List<ServiceRecord> records = serviceRecordRepository
                    .findByUserIdOrderByServiceDateDesc(user.getId())
                    .stream()
                    .filter(r -> r.getVehicle().getId().equals(s.getVehicle().getId()))
                    .filter(r -> "COMPLETED".equals(r.getStatus()))
                    .toList();

            if (!records.isEmpty()) {
                ServiceRecord last = records.get(0);
                m.put("lastServiceDate", last.getServiceDate());
                m.put("lastServiceDesc", last.getDescription());
                m.put("nextServiceDate", last.getServiceDate().plusMonths(3));
            } else {
                m.put("lastServiceDate", null);
                m.put("lastServiceDesc", "No service recorded yet");
                m.put("nextServiceDate", null);
            }
            vehicleList.add(m);
        }

        return ResponseEntity.ok(Map.of(
                "linked", true,
                "customerName", user.getUsername(),
                "vehicles", vehicleList
        ));
    }

    @PostMapping("/add-manager")
    public ResponseEntity<?> addManager(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if (username == null || password == null)
            return ResponseEntity.badRequest().body("Username and password required");
        if (userRepository.findByUsername(username).isPresent())
            return ResponseEntity.badRequest().body("Username already exists");

        Role managerRole = roleRepository.findByName("ROLE_MANAGER")
                .orElseThrow(() -> new RuntimeException("ROLE_MANAGER not found in database"));
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        Set<Role> roles = new HashSet<>();
        roles.add(managerRole);
        user.setRoles(roles);
        user.setStatus("APPROVED");
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Manager " + username + " created successfully"));
    }
}
