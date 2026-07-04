package com.automobile.project.controller;

import com.automobile.project.entity.Sale;
import com.automobile.project.repository.SaleRepository;
import com.automobile.project.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/sale")
public class SaleController {

    @Autowired private SaleService service;
    @Autowired private SaleRepository saleRepository;

    /**
     * Allocate a vehicle to a customer's account.
     * Body: { "vehicleId": 1, "userId": 5 }
     */
    @PostMapping("/allocate")
    public Sale allocate(@RequestBody Map<String, Long> body) {
        Long vehicleId = body.get("vehicleId");
        Long userId    = body.get("userId");
        if (vehicleId == null || userId == null)
            throw new RuntimeException("vehicleId and userId are required");
        return service.allocate(vehicleId, userId);
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        List<Sale> all = saleRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Sale s : all) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("vehicleId", s.getVehicle().getId());
            m.put("vehicleBrand", s.getVehicle().getBrand());
            m.put("vehicleModel", s.getVehicle().getModel());
            m.put("userId", s.getUser().getId());
            m.put("username", s.getUser().getUsername());
            m.put("saleDate", s.getSaleDate());
            result.add(m);
        }
        return ResponseEntity.ok(result);
    }
}
