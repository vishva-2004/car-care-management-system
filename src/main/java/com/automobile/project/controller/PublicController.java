package com.automobile.project.controller;

import com.automobile.project.entity.Vehicle;
import com.automobile.project.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/public")
public class PublicController {

    @Autowired private VehicleRepository vehicleRepository;

    @GetMapping("/vehicles")
    public ResponseEntity<?> getAvailableVehicles() {
        List<Vehicle> all = vehicleRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Vehicle v : all) {
            if ("sold".equalsIgnoreCase(v.getAvailability())) continue;
            Map<String, Object> m = new HashMap<>();
            m.put("id", v.getId());
            m.put("brand", v.getBrand());
            m.put("model", v.getModel());
            m.put("price", v.getPrice());
            m.put("fuelType", v.getFuelType());
            m.put("availability", v.getAvailability());
            m.put("readyStock", v.getReadyStock());
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }
}
