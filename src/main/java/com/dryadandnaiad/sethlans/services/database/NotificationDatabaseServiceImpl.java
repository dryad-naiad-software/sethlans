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

package com.dryadandnaiad.sethlans.services.database;

import com.dryadandnaiad.sethlans.domains.database.events.SethlansNotification;
import com.dryadandnaiad.sethlans.enums.NotificationScope;
import com.dryadandnaiad.sethlans.repositories.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanCreationException;
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
    public long tableSize() {
        return notificationRepository.count();
    }

    @Override
    public List<SethlansNotification> listAll() {
        try {
            return new ArrayList<>(notificationRepository.findAll());
        } catch (BeanCreationException e) {
            LOG.error("Bean creation failure. Likely during shutdown and DB is being accessed after it was closed. This can be ignored.");
            return new ArrayList<>();
        }
    }


    @Override
    public List<SethlansNotification> getAdminNotifications() {
        return notificationRepository.findSethlansNotificationsByScopeEquals(NotificationScope.ADMIN);
    }

    @Override
    public List<SethlansNotification> getUserNotifications(String username) {
        return notificationRepository.findSethlansNotificationsByUsernameEquals(username);
    }

    @Override
    public int numberOfNewUserNotifications(String username) {
        return notificationRepository.countSethlansNotificationByUsernameEqualsAndAcknowledgedIsFalse(username);
    }

    @Override
    public int numberofNewAdminNotifications() {
        return notificationRepository.countSethlansNotificationByScopeEqualsAndAcknowledgedIsFalse(NotificationScope.ADMIN);
    }

    @Override
    public boolean newAdminNotifications() {
        return notificationRepository.existsSethlansNotificationByAcknowledgedIsFalseAndScopeEquals(NotificationScope.ADMIN);
    }

    @Override
    public boolean newUserNotifications(String username) {
        return notificationRepository.existsSethlansNotificationByAcknowledgedIsFalseAndUsernameEquals(username);
    }

    @Override
    public SethlansNotification getById(Long id) {
        return notificationRepository.findOne(id);
    }

    @Override
    public SethlansNotification saveOrUpdate(SethlansNotification domainObject) {
        return notificationRepository.save(domainObject);
    }

    @Override
    public void delete(Long id) {
        SethlansNotification sethlansNotification = notificationRepository.findOne(id);
        notificationRepository.delete(sethlansNotification);
    }

    @Override
    public void delete(SethlansNotification notification) {
        notificationRepository.delete(notification);
    }

    @Autowired
    public void setNotificationRepository(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
}
