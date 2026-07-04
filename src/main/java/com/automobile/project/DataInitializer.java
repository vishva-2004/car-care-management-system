package com.automobile.project;

import com.automobile.project.entity.Role;
import com.automobile.project.entity.User;
import com.automobile.project.repository.RoleRepository;
import com.automobile.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Value("${app.manager.username:manager}")
    private String managerUsername;

    @Value("${app.manager.password:manager123}")
    private String managerPassword;

    @Override
    public void run(String... args) {
        // Create all roles if they don't exist
        List<String> roles = List.of("ROLE_MANAGER", "ROLE_ADMIN", "ROLE_STAFF", "ROLE_CUSTOMER");
        for (String roleName : roles) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role r = new Role();
                r.setName(roleName);
                roleRepository.save(r);
                System.out.println("[Car Care] Created role: " + roleName);
            }
        }

        // Create default manager if no manager exists
        boolean managerExists = userRepository.findAll().stream()
                .anyMatch(u -> u.getRoles().stream()
                        .anyMatch(r -> r.getName().equals("ROLE_MANAGER")));

        if (!managerExists) {
            Role managerRole = roleRepository.findByName("ROLE_MANAGER").get();
            User manager = new User();
            manager.setUsername(managerUsername);
            manager.setPassword(passwordEncoder.encode(managerPassword));
            manager.setRoles(Set.of(managerRole));
            manager.setStatus("APPROVED");
            userRepository.save(manager);
            System.out.println("╔══════════════════════════════════════════╗");
            System.out.println("║         Car Care - Default Manager        ║");
            System.out.println("║  Username : " + managerUsername + "                          ║");
            System.out.println("║  Password : " + managerPassword + "                      ║");
            System.out.println("║  Change in application.properties         ║");
            System.out.println("╚══════════════════════════════════════════╝");
        }
    }
}
