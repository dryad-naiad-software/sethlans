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

import java.util.List;

/**
 * Created Mario Estrella on 8/31/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public interface SethlansNotificationService {
    void sendNotification(SethlansNotification notification);

    boolean newNotificationsPresent(String username);

    int numberofNewNotifications(String username);

    boolean acknowledgeNotification(String username, Long id);

    boolean acknowledgeAllNotifications(String username);

    boolean clearNotification(String username, Long id);

    boolean clearAllNotifications(String username);

    List<SethlansNotification> getNotifications(String username);

    boolean notificationsPresent(String username);
}
