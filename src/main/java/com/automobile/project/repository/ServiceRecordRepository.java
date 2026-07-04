package com.automobile.project.repository;

import com.automobile.project.entity.ServiceRecord;
import com.automobile.project.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRecordRepository extends JpaRepository<ServiceRecord, Long> {
    List<ServiceRecord> findByUser(User user);
    List<ServiceRecord> findByUserId(Long userId);
    List<ServiceRecord> findByUserIdOrderByServiceDateDesc(Long userId);
    List<ServiceRecord> findByStatus(String status);
}
