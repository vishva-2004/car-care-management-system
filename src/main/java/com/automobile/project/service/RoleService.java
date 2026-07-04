package com.automobile.project.service;

import com.automobile.project.entity.Role;
import com.automobile.project.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    @Autowired
    private RoleRepository repository;

    // Save Role
    public Role save(Role role){

        if(repository.findByName(role.getName()).isPresent()){
            throw new RuntimeException("Role already exists: " + role.getName());
        }

        return repository.save(role);
    }

    // Get all roles
    public List<Role> getAll(){
        return repository.findAll();
    }

    // Get by ID
    public Role getById(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
    }
}