package com.automobile.project.service;

import com.automobile.project.entity.User;
import com.automobile.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

    public User save(User user){
        return repository.save(user);
    }



    public User findByUserNameOrId(String findByUserNameOrId){

        if(findByUserNameOrId.matches("\\d+")){

            Long id = Long.parseLong(findByUserNameOrId);

            Optional<User> user = repository.findById(id);

            if(user.isPresent()){

                return user.get();

            }else throw new RuntimeException("User not found with ID ");

        }

        else {

            Optional<User> user = repository.findByUsername(findByUserNameOrId);

            if(user.isPresent()){
                return user.get();

            }else throw new RuntimeException("user not found with user name");
        }
    }
}
