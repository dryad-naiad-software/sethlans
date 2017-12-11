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

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.events.SethlansEvent;
import com.dryadandnaiad.sethlans.services.database.UserDatabaseService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created Mario Estrella on 10/20/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
abstract public class AbstractSethlansController implements ApplicationListener<SethlansEvent> {
    private UserDatabaseService userDatabaseService;
    private Map<String, String> notificationMessage = new LinkedHashMap<>();
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSethlansController.class);

    @Value("${sethlans.mode}")
    private SethlansMode mode;

    @Value("${sethlans.firsttime}")
    private boolean firstTime;

    @ModelAttribute("version")
    public String getVersion() {
        return SethlansUtils.getVersion();
    }

    @ModelAttribute("sethlansmode")
    public String getMode() {
        return mode.toString();
    }

    @ModelAttribute("username")
    public String getUserName() {
        if(firstTime){
            return "username";
        }
        return userDatabaseService.getById(1).getUsername();

    }

    @ModelAttribute("isNewNotification")
    public boolean isNotification(){
        LOG.debug("Current notification list size: " + notificationMessage.size());
        if (notificationMessage.size() > 0) {
            return true;
        }
        return false;
    }

    @ModelAttribute("notificationMessages")
    public List<String> getNotificationMessage(){
        return new ArrayList<>(notificationMessage.values());
    }

    @Autowired
    public void setUserDatabaseService(UserDatabaseService userDatabaseService) {
        this.userDatabaseService = userDatabaseService;
    }


    @Override
    public void onApplicationEvent(SethlansEvent event) {
        boolean activeNotification = event.isActiveNotification();
        if (activeNotification) {
            notificationMessage.put(event.getKey(), event.getMessage());
        } else {
            notificationMessage.remove(event.getKey());
        }

    }
}
