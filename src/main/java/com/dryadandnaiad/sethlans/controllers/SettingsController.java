/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC
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
import com.dryadandnaiad.sethlans.services.database.UserService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created Mario Estrella on 9/20/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Controller
public class SettingsController {

    private UserService userService;

    @Value("${sethlans.mode}")
    private SethlansMode mode;

    @RequestMapping("/settings")
    public String getPage() {
        return "settings/settings_list";
    }

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
        return userService.getById(1).getUsername();
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

}
