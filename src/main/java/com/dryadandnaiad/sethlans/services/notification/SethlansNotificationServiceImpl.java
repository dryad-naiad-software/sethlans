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

package com.dryadandnaiad.sethlans.services.notification;

import com.dryadandnaiad.sethlans.domains.database.events.SethlansNotification;
import com.dryadandnaiad.sethlans.domains.database.user.SethlansUser;
import com.dryadandnaiad.sethlans.enums.NotificationScope;
import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.database.NotificationDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansUserDatabaseService;
import com.dryadandnaiad.sethlans.services.mail.SethlansEmailService;
import com.dryadandnaiad.sethlans.utils.SethlansConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 8/31/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class SethlansNotificationServiceImpl implements SethlansNotificationService {
    private NotificationDatabaseService notificationDatabaseService;
    private SethlansEmailService sethlansEmailService;
    private SethlansUserDatabaseService sethlansUserDatabaseService;

    private static final Logger LOG = LoggerFactory.getLogger(SethlansNotificationServiceImpl.class);

    @Override
    public void sendNotification(SethlansNotification notification) {
        LOG.debug("Received notification, saving to database");
        notificationDatabaseService.saveOrUpdate(notification);
        boolean mailServerOn = Boolean.parseBoolean(SethlansConfigUtils.getProperty(SethlansConfigKeys.MAIL_SERVER_CONFIGURED));
        if (mailServerOn) {
            sethlansEmailService.sendNotificationEmail();
        }
    }

    @Override
    public boolean newNotificationsPresent(String username) {
        for (SethlansNotification sethlansNotification : getNotifications(username)) {
            if (!sethlansNotification.isAcknowledged()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean acknowledgeNotification(String username, Long id) {
        for (SethlansNotification sethlansNotification : getNotifications(username)) {
            if (sethlansNotification.getId().equals(id)) {
                sethlansNotification.setAcknowledged(true);
                notificationDatabaseService.saveOrUpdate(sethlansNotification);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean acknowledgeAllNotifications(String username) {
        for (SethlansNotification sethlansNotification : getNotifications(username)) {
            sethlansNotification.setAcknowledged(true);
            notificationDatabaseService.saveOrUpdate(sethlansNotification);
        }
        return true;
    }

    @Override
    public boolean clearAllNotifications(String username) {
        for (SethlansNotification sethlansNotification : getNotifications(username)) {
            notificationDatabaseService.delete(sethlansNotification);
        }
        return true;
    }

    @Override
    public List<SethlansNotification> getNotifications(String username) {
        List<SethlansNotification> listToSend = new ArrayList<>();
        SethlansUser sethlansUser = sethlansUserDatabaseService.findByUserName(username);
        boolean isAdministrator = sethlansUser.getRoles().contains(Role.ADMINISTRATOR) || sethlansUser.getRoles().contains(Role.SUPER_ADMINISTRATOR);
        for (SethlansNotification sethlansNotification : notificationDatabaseService.listAll()) {
            if (isAdministrator && sethlansNotification.getScope().equals(NotificationScope.ADMIN)) {
                listToSend.add(sethlansNotification);
                continue;
            }
            if (sethlansNotification.getUsername() != null) {
                if (sethlansNotification.getScope().equals(NotificationScope.USER) && sethlansNotification.getUsername().equals(sethlansUser.getUsername())) {
                    listToSend.add(sethlansNotification);
                }
            }
        }

        return listToSend;
    }

    @Override
    public boolean notificationsPresent(String username) {
        return getNotifications(username).size() > 0;
    }

    @Autowired
    public void setNotificationDatabaseService(NotificationDatabaseService notificationDatabaseService) {
        this.notificationDatabaseService = notificationDatabaseService;
    }

    @Autowired
    public void setSethlansEmailService(SethlansEmailService sethlansEmailService) {
        this.sethlansEmailService = sethlansEmailService;
    }

    @Autowired
    public void setSethlansUserDatabaseService(SethlansUserDatabaseService sethlansUserDatabaseService) {
        this.sethlansUserDatabaseService = sethlansUserDatabaseService;
    }
}
