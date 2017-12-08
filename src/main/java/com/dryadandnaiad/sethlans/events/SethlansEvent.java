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

package com.dryadandnaiad.sethlans.events;

import org.springframework.context.ApplicationEvent;

/**
 * Created Mario Estrella on 10/20/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class SethlansEvent extends ApplicationEvent {

    private static final long serialVersionUID = 1L;

    private String message;
    private String key;
    private boolean activeNotification;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source the object on which the event initially occurred (never {@code null})
     */
    public SethlansEvent(Object source, String key, String message, boolean activeNotification) {
        super(source);
        this.message = message;
        this.key = key;
        this.activeNotification = activeNotification;
    }

    public SethlansEvent(Object source, String key, boolean activeNotification) {
        super(source);
        this.key = key;
        this.activeNotification = activeNotification;
    }

    public String getMessage() {
        return message;
    }

    public boolean isActiveNotification() {
        return activeNotification;
    }

    public String getKey() {
        return key;
    }
}
