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

package com.dryadandnaiad.sethlans.domains.database.events;

import com.dryadandnaiad.sethlans.domains.database.AbstractEntityClass;
import com.dryadandnaiad.sethlans.enums.NotificationScope;
import com.dryadandnaiad.sethlans.enums.NotificationType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

/**
 * Created Mario Estrella on 8/30/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class SethlansNotification extends AbstractEntityClass {
    private NotificationType notificationType;
    private String message;
    private Long messageDate;
    private String messageLink;
    private boolean linkPresent;
    private boolean acknowledged;
    private NotificationScope scope;
    private String username;


    public SethlansNotification(NotificationType notificationType, String message, String username) {
        this.notificationType = notificationType;
        this.message = message + ".";
        this.messageDate = System.currentTimeMillis();
        this.acknowledged = false;
        this.linkPresent = false;
        this.scope = NotificationScope.USER;
        this.username = username;
    }

    public SethlansNotification(NotificationType notificationType, String message) {
        this.notificationType = notificationType;
        this.message = message + ".";
        this.messageDate = System.currentTimeMillis();
        this.acknowledged = false;
        this.linkPresent = false;
        this.scope = NotificationScope.GLOBAL;
    }

    public SethlansNotification(NotificationType notificationType, String message, NotificationScope scope) {
        this.notificationType = notificationType;
        this.message = message + ".";
        this.scope = scope;
        this.messageDate = System.currentTimeMillis();
        this.acknowledged = false;
        this.linkPresent = false;
    }
}
