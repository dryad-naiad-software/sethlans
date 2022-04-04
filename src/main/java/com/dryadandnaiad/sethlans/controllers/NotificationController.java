/*
 * Copyright (c) 2022 Dryad and Naiad Software LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

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
    public ResponseEntity<Void> markAsRead(@RequestParam String notificationID) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        var user = userRepository.findUserByUsername(auth.getName()).get();
        var notification = notificationRepository.findNotificationByNotificationID(notificationID);
        if (notification.getUserID().equals(user.getUserID())) {
            notification.setMessageRead(true);
            notificationRepository.save(notification);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

}
