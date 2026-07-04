package com.automobile.project.controller;

import com.automobile.project.dto.RegisterRequest;
import com.automobile.project.entity.Role;
import com.automobile.project.entity.User;
import com.automobile.project.repository.RoleRepository;
import com.automobile.project.repository.UserRepository;
import com.automobile.project.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        String roleUpper = request.getRole().toUpperCase();
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // CUSTOMER -> approved immediately, gets ROLE_CUSTOMER
        // STAFF / ADMIN -> goes to PENDING, no role assigned yet
        if (roleUpper.equals("CUSTOMER")) {
            Role role = roleRepository.findByName("ROLE_CUSTOMER")
                    .orElseThrow(() -> new RuntimeException("ROLE_CUSTOMER not found"));
            Set<Role> roles = new HashSet<>();
            roles.add(role);
            user.setRoles(roles);
            user.setStatus("APPROVED");
        } else if (roleUpper.equals("STAFF") || roleUpper.equals("ADMIN")) {
            Role guestRole = roleRepository.findByName("ROLE_CUSTOMER")
                    .orElseThrow(() -> new RuntimeException("ROLE_CUSTOMER not found"));
            Set<Role> roles = new HashSet<>();
            roles.add(guestRole);
            user.setRoles(roles);
            user.setStatus("PENDING");
            user.setRequestedRole("ROLE_" + roleUpper);
        } else {
            return ResponseEntity.badRequest().body("Invalid role. Only CUSTOMER, STAFF, or ADMIN allowed here.");
        }

        userRepository.save(user);
        Map<String, String> res = new HashMap<>();
        res.put("message", roleUpper.equals("CUSTOMER")
                ? "Registered successfully! You can now login."
                : "Registration request submitted. Please wait for Manager approval.");
        res.put("status", user.getStatus());
        return ResponseEntity.ok(res);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody RegisterRequest request) {

        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }

        if ("PENDING".equals(user.getStatus())) {
            return ResponseEntity.status(403).body(
                    "Your account is pending approval. Please wait for Manager to approve.");
        }

        if ("REJECTED".equals(user.getStatus())) {
            return ResponseEntity.status(403).body(
                    "Your registration was rejected. Please contact the Manager.");
        }

        String token = jwtUtil.generateToken(user.getUsername());

        String roleName = user.getRoles().stream()
                .findFirst()
                .map(r -> r.getName().replace("ROLE_", ""))
                .orElse("CUSTOMER");

        Map<String, String> res = new HashMap<>();
        res.put("token", token);
        res.put("username", user.getUsername());
        res.put("role", roleName);
        return ResponseEntity.ok(res);
    }

    /**
     * Forgot password — public endpoint (no JWT needed).
     * Resets the user's password to a generated temporary password
     * and returns it in the response (no email needed).
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username is required."));
        }

        Optional<User> userOpt = userRepository.findByUsername(username.trim());
        if (userOpt.isEmpty()) {
            // Do not reveal whether the username exists
            return ResponseEntity.ok(Map.of(
                "found", false,
                "message", "If this username is registered, a temporary password has been set."
            ));
        }

        User user = userOpt.get();

        boolean isCustomer = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_CUSTOMER"));
        if (!isCustomer) {
            // Staff/Admin/Manager cannot self-reset — prevents anyone who guesses
            // a staff username from taking over that account. Manager must reset
            // their password from the Users page instead.
            return ResponseEntity.ok(Map.of(
                "found", false,
                "message", "If this username is registered, a temporary password has been set."
            ));
        }

        // Generate a simple readable temporary password
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        String tempPassword = sb.toString();

        user.setPassword(passwordEncoder.encode(tempPassword));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
            "found", true,
            "tempPassword", tempPassword,
            "message", "Temporary password generated. Login now and change your password."
        ));
    }
}
