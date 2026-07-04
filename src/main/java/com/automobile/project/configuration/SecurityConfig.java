package com.automobile.project.configuration;

import com.automobile.project.security.JwtFilter;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ── Fully public: auth + public API + all HTML pages + static assets ──
                .requestMatchers(
                    "/auth/**",
                    "/public/**",
                    "/login", "/register", "/home", "/forgot-password",
                    "/dashboard", "/customer-dashboard",
                    "/vehicles", "/sales",
                    "/service-records", "/users",
                    "/", "/css/**", "/js/**", "/images/**"
                ).permitAll()

                // ── Vehicle API ──
                .requestMatchers(HttpMethod.GET,    "/vechicle/**").authenticated()
                .requestMatchers(HttpMethod.POST,   "/vechicle/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/vechicle/**").hasAnyRole("MANAGER", "ADMIN")

                // ── Booking API ──
                .requestMatchers("/booking/request").authenticated()
                .requestMatchers("/booking/my").authenticated()
                .requestMatchers("/booking/pending").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/booking/reject/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/booking/all").hasAnyRole("MANAGER", "ADMIN")

                // ── Sale / Allocation API ──
                .requestMatchers("/sale/allocate").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/sale/**").hasAnyRole("MANAGER", "ADMIN")

                // ── Service Records API ──
                .requestMatchers("/serviceRecords/request").authenticated()
                .requestMatchers("/serviceRecords/my").authenticated()
                .requestMatchers("/serviceRecords/pending").hasAnyRole("MANAGER", "ADMIN", "STAFF")
                .requestMatchers("/serviceRecords/approve/**").hasAnyRole("MANAGER", "ADMIN", "STAFF")
                .requestMatchers("/serviceRecords/reject/**").hasAnyRole("MANAGER", "ADMIN", "STAFF")
                .requestMatchers("/serviceRecords/complete/**").hasAnyRole("MANAGER", "ADMIN", "STAFF")
                .requestMatchers(HttpMethod.POST, "/serviceRecords").hasAnyRole("MANAGER", "ADMIN", "STAFF")
                .requestMatchers(HttpMethod.GET,  "/serviceRecords/**").authenticated()

                // ── User Management API — MANAGER and ADMIN ──
                .requestMatchers("/user/me").authenticated()
                .requestMatchers("/user/pending").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/user/approve/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/user/reject/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/user/add-manager").hasRole("MANAGER")
                .requestMatchers("/user/reset-password/**").hasRole("MANAGER")
                .requestMatchers("/user/all").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers("/user/customers").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/user/**").hasAnyRole("MANAGER", "ADMIN")

                // ── Customer's own data ──
                .requestMatchers("/user/my-vehicles").authenticated()
                .requestMatchers("/user/**").authenticated()

                // ── Notifications ──
                .requestMatchers("/notification/**").authenticated()

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
