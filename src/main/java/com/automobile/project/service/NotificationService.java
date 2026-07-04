package com.automobile.project.service;

import com.automobile.project.entity.Notification;
import com.automobile.project.entity.User;
import com.automobile.project.repository.NotificationRepository;
import com.automobile.project.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserRepository userRepository;

    /** Send a notification to one specific user */
    public void notifyUser(Long userId, String message) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setMessage(message);
        notificationRepository.save(n);
    }

    /** Send notification to all MANAGER and ADMIN users */
    public void notifyAllManagers(String message) {
        List<User> all = userRepository.findAll();
        for (User u : all) {
            boolean isManagerOrAdmin = u.getRoles().stream()
                    .anyMatch(r -> r.getName().equals("ROLE_MANAGER") || r.getName().equals("ROLE_ADMIN"));
            if (isManagerOrAdmin && "APPROVED".equals(u.getStatus())) {
                notifyUser(u.getId(), message);
            }
        }
    }

    /** Send notification to all STAFF, MANAGER, and ADMIN users */
    public void notifyAllStaff(String message) {
        List<User> all = userRepository.findAll();
        for (User u : all) {
            boolean isStaff = u.getRoles().stream()
                    .anyMatch(r -> r.getName().equals("ROLE_STAFF")
                            || r.getName().equals("ROLE_MANAGER")
                            || r.getName().equals("ROLE_ADMIN"));
            if (isStaff && "APPROVED".equals(u.getStatus())) {
                notifyUser(u.getId(), message);
            }
        }
    }
}
