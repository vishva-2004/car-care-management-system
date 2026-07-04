package com.automobile.project.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Role {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;


}
