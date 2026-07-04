package com.automobile.project.repository;

import com.automobile.project.entity.Sale;
import com.automobile.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findByUser(User user);
    List<Sale> findByUserId(Long userId);
}
