package com.automobile.project.controller;

import com.automobile.project.entity.Vehicle;
import com.automobile.project.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vechicle")
public class VehicleController {


    @Autowired
    private VehicleService service;



    @PostMapping
    public Vehicle add(@Valid @RequestBody Vehicle vehicle){
        return service.save(vehicle);
    }

    @GetMapping
    public List<Vehicle> getAll(){return service.getAll();}

    @DeleteMapping("/{input}")
    public String deleteByIdOrName(@PathVariable String input){
        return service.deleteByIdOrModelName(input);
    }



}
