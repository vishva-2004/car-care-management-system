package com.automobile.project.service;

import com.automobile.project.entity.Vehicle;
import com.automobile.project.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VehicleService {


    @Autowired
    private VehicleRepository repository;


 //========================================================================================



    public Vehicle save(Vehicle vehicle){

   if(repository.existsByModel (vehicle.getModel())) throw new RuntimeException("model name alredy exists");

   if(vehicle.getPrice() < 100000) throw new RuntimeException("invalid price");

   if(vehicle.getAvailability().equalsIgnoreCase("sold")
           || vehicle.getAvailability().equalsIgnoreCase("available")
           || vehicle.getAvailability().equalsIgnoreCase("limited")) {

       if(vehicle.getReadyStock() <= 0){

           vehicle.setAvailability("sold");
       }

   }

   else throw new RuntimeException("invalid stock availablity");


   return repository.save(vehicle);
    }


 //===========================================================================================



    public List<Vehicle> getAll(){
        return repository.findAll();
    }

 //===========================================================================================




    public String deleteByIdOrModelName(String ModelNameOrId){


    if(ModelNameOrId.matches("\\d+")){

        Long id = Long.parseLong(ModelNameOrId);

       if(repository.existsById(id)){

           repository.deleteById(id);

           return "model deleted sucessfully";

       }else {return "Vehicle not found with model name";}
    }


    //-----------------------------------------------------

        else {

            Optional<Vehicle> vehicle = repository.findByModel(ModelNameOrId);
            if(vehicle.isPresent()){
                repository.delete(vehicle.get());
                return "model deleted sucessfully";

            }else {return "Vehicle not found with model name";}
    }

    }
}
