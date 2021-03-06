/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC
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
 *
 */

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.domains.database.events.SethlansNotification;
import com.dryadandnaiad.sethlans.services.notification.SethlansNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created Mario Estrella on 8/31/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationController.class);
    private SethlansNotificationService sethlansNotificationService;

    @GetMapping(value = "/new_notifications_present")
    public boolean newNotificationsPresent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return sethlansNotificationService.newNotificationsPresent(auth.getName());
    }

    @GetMapping(value = "/acknowledge_all_notifications")
    public boolean acknowledgeAllNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return sethlansNotificationService.acknowledgeAllNotifications(auth.getName());
    }

    @GetMapping(value = "/clear_all_notifications")
    public boolean clearAllNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return sethlansNotificationService.clearAllNotifications(auth.getName());
    }

    @GetMapping(value = "/clear_notification/{id}")
    public boolean clearNotification(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return sethlansNotificationService.clearNotification(auth.getName(), id);
    }

    @GetMapping(value = "/acknowledge_notification/{id}")
    public boolean acknowledgeNotification(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return sethlansNotificationService.acknowledgeNotification(auth.getName(), id);
    }

    @GetMapping(value = "/notifications_present")
    public boolean notificationsPresent() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        return sethlansNotificationService.notificationsPresent(auth.getName());
    }

    @GetMapping(value = "/number_of_new_notifications")
    public int getNumberOfNewNotifications() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return 0;
        }
        return sethlansNotificationService.numberofNewNotifications(auth.getName());
    }

    @GetMapping(value = "/get_notifications")
    public List<SethlansNotification> getNotificationMessages() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return null;
        }
        return sethlansNotificationService.getNotifications(auth.getName());
    }

    @Autowired
    public void setSethlansNotificationService(SethlansNotificationService sethlansNotificationService) {
        this.sethlansNotificationService = sethlansNotificationService;
    }
}
