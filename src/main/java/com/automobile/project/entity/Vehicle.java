package com.automobile.project.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
public class Vehicle  {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @NotBlank(message = "Brand name is mandatory")
    private String brand;

    @Column(nullable = false, unique = true)
    private String model;

    @Column(nullable = false)
    private Double price;

    @NotBlank
    private String fuelType;

    @Column(nullable = false)
    private String availability;

    @NotNull
    private Integer readyStock;

}
