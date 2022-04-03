package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.models.system.Notification;
import com.dryadandnaiad.sethlans.repositories.NotificationRepository;
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Profile({"SERVER", "DUAL"})
@RequestMapping("/api/v1/notifications")
@Slf4j
public class NotificationController {
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public NotificationController(UserRepository userRepository, NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @GetMapping("/get_notifications")
    public List<Notification> getNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var user = userRepository.findUserByUsername(auth.getName()).get();
        return notificationRepository.findNotificationsByUserID(user.getUserID());
    }

    @PostMapping("/mark_read")
    public ResponseEntity<Void> markAsRead(@RequestParam String notificationUUID) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var user = userRepository.findUserByUsername(auth.getName()).get();
        var notification = notificationRepository.findNotificationByNotificationID(notificationUUID);
        if (notification.getUserID().equals(user.getUserID())) {
            notification.setMessageRead(true);
            notificationRepository.save(notification);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

}
