/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC.
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

package com.dryadandnaiad.sethlans.services.database;

import com.dryadandnaiad.sethlans.domains.database.events.SethlansNotification;
import com.dryadandnaiad.sethlans.repositories.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 12/11/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class NotificationDatabaseServiceImpl implements NotificationDatabaseService {

    private NotificationRepository notificationRepository;
    private static final Logger LOG = LoggerFactory.getLogger(NotificationDatabaseServiceImpl.class);

    @Override
    public List<SethlansNotification> listAll() {
        List<SethlansNotification> sethlansNotifications = new ArrayList<>();
        notificationRepository.findAll().forEach(sethlansNotifications::add);
        return sethlansNotifications;
    }

    @Override
    public SethlansNotification getById(Integer id) {
        return notificationRepository.findOne(id);
    }

    @Override
    public SethlansNotification getByKey(String key) {
        List<SethlansNotification> sethlansNotificationList = listAll();
        for (SethlansNotification sethlansNotification : sethlansNotificationList) {
            if (sethlansNotification.getKey().equals(key)) {
                return sethlansNotification;
            }

        }
        return null;
    }

    @Override
    public SethlansNotification saveOrUpdate(SethlansNotification domainObject) {
        if (domainObject.getKey().isEmpty() || domainObject.getMessage().isEmpty() || domainObject.getOrigin().isEmpty()) {
            LOG.debug("null objects are not allowed");
        } else {
            return notificationRepository.save(domainObject);
        }
        return null;
    }

    @Override
    public void delete(Integer id) {
        SethlansNotification sethlansNotification = notificationRepository.findOne(id);
        notificationRepository.delete(sethlansNotification);
    }

    @Override
    public void delete(SethlansNotification notification) {
        List<SethlansNotification> sethlansNotifications = listAll();
        for (SethlansNotification sethlansNotification : sethlansNotifications) {
            if (sethlansNotification.getKey().equals(notification.getKey())) {
                notificationRepository.delete(sethlansNotification);
                LOG.debug("Deleted: " + sethlansNotification);
            }
        }

    }

    @Autowired
    public void setNotificationRepository(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
}
