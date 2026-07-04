package com.automobile.project.service;

import com.automobile.project.entity.ServiceRecord;
import com.automobile.project.entity.User;
import com.automobile.project.entity.Vehicle;
import com.automobile.project.repository.ServiceRecordRepository;
import com.automobile.project.repository.UserRepository;
import com.automobile.project.repository.VehicleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServiceRecordService {

    private final ServiceRecordRepository serviceRecordRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    public ServiceRecordService(ServiceRecordRepository serviceRecordRepository,
                                VehicleRepository vehicleRepository,
                                UserRepository userRepository) {
        this.serviceRecordRepository = serviceRecordRepository;
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    /**
     * Staff/Manager/Admin directly logging a completed service (existing behaviour).
     */
    public ServiceRecord save(ServiceRecord record){

        Vehicle vehicle = vehicleRepository.findById(record.getVehicle().getId())
                .orElseThrow(() -> new RuntimeException(
                        "Vehicle not found with ID: " + record.getVehicle().getId()));

        User user = userRepository.findById(record.getUser().getId())
                .orElseThrow(() -> new RuntimeException(
                        "User not found with ID: " + record.getUser().getId()));

        if(record.getServiceDate() == null){
            record.setServiceDate(java.time.LocalDate.now());
        }

        record.setVehicle(vehicle);
        record.setUser(user);
        record.setStatus("COMPLETED");

        return serviceRecordRepository.save(record);
    }
}
