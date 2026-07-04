package com.automobile.project.controller;

import com.automobile.project.entity.Role;
import com.automobile.project.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService service;

    // Create Role
    @PostMapping
    public Role save(@RequestBody Role role){
        return service.save(role);
    }

    // Get all roles
    @GetMapping
    public List<Role> getAll(){
        return service.getAll();
    }

    // Get by ID
    @GetMapping("/{id}")
    public Role getById(@PathVariable Long id){
        return service.getById(id);
    }
}