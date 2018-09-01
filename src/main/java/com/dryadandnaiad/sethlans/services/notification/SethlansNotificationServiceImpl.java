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
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.database.NotificationDatabaseService;
import com.dryadandnaiad.sethlans.services.mail.SethlansEmailService;
import com.dryadandnaiad.sethlans.utils.SethlansConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public boolean newNotificationsPresent() {
        for (SethlansNotification sethlansNotification : notificationDatabaseService.listAll()) {
            if (!sethlansNotification.isAcknowledged()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<SethlansNotification> getNotifications() {
        return notificationDatabaseService.listAll();
    }

    @Override
    public boolean notificationsPresent() {
        return notificationDatabaseService.listAll().size() > 0;
    }

    @Autowired
    public void setNotificationDatabaseService(NotificationDatabaseService notificationDatabaseService) {
        this.notificationDatabaseService = notificationDatabaseService;
    }

    @Autowired
    public void setSethlansEmailService(SethlansEmailService sethlansEmailService) {
        this.sethlansEmailService = sethlansEmailService;
    }
}
