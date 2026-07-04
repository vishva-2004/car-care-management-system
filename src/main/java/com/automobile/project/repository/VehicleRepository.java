package com.automobile.project.repository;

import com.automobile.project.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    boolean existsByModel (String model);

    Optional<Vehicle> findByModel(String model);

    void deleteByModel(String model);
}
