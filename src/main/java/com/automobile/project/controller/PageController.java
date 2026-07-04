package com.automobile.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String root() { return "redirect:/home"; }

    @GetMapping("/home")
    public String home() { return "home"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String register() { return "register"; }

    @GetMapping("/forgot-password")
    public String forgotPassword() { return "forgot-password"; }

    @GetMapping("/dashboard")
    public String dashboard() { return "dashboard"; }

    @GetMapping("/customer-dashboard")
    public String customerDashboard() { return "customer-dashboard"; }

    @GetMapping("/vehicles")
    public String vehicles() { return "vehicles"; }

    @GetMapping("/sales")
    public String sales() { return "sales"; }

    @GetMapping("/service-records")
    public String serviceRecords() { return "service-records"; }

    @GetMapping("/users")
    public String users() { return "users"; }
}
