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
import com.dryadandnaiad.sethlans.events.SethlansEvent;
import com.dryadandnaiad.sethlans.services.database.NotificationDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created Mario Estrella on 3/12/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController implements ApplicationListener<SethlansEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationController.class);
    private Map<String, String> notificationMessage = new LinkedHashMap<>();
    private NotificationDatabaseService notificationDatabaseService;


    @Override
    public void onApplicationEvent(SethlansEvent event) {
        boolean activeNotification = event.isActiveNotification();
        if (activeNotification) {
            notificationMessage.put(event.getKey(), event.getMessage());
            notificationDatabaseService.saveOrUpdate(event.getSethlansNotification());
        } else {
            notificationMessage.remove(event.getKey());
            notificationDatabaseService.delete(event.getSethlansNotification());
        }

    }

    @Autowired
    public void setNotificationDatabaseService(NotificationDatabaseService notificationDatabaseService) {
        this.notificationDatabaseService = notificationDatabaseService;
    }

    @GetMapping(value = "/notifications_present")
    public boolean notificationsPresent() {
        if (notificationMessage.size() == 0) {
            restoreNotifications();
        }
        return notificationMessage.size() > 0;

    }

    @GetMapping(value = "/get_notifications")
    public List<String> getNotificationMessage() {
        return new ArrayList<>(notificationMessage.values());
    }


    private void restoreNotifications() {
        try {
            List<SethlansNotification> sethlansNotifications = notificationDatabaseService.listAll();
            if (sethlansNotifications.size() != 0) {
                for (SethlansNotification sethlansNotification : sethlansNotifications) {
                    notificationMessage.put(sethlansNotification.getKey(), sethlansNotification.getMessage());
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }



    }
}
