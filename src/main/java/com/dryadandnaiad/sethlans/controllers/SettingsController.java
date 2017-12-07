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

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created Mario Estrella on 12/4/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Controller
public class SettingsController extends AbstractSethlansController {

    @RequestMapping("/settings")
    public String getHomePage(Model model) {
        model.addAttribute("settings_option", "home");
        return "settings/settings";
    }

    @RequestMapping("/settings/users")
    public String getUserPage(Model model) {
        model.addAttribute("settings_option", "users");
        return "settings/settings";
    }

    @RequestMapping("/logs")
    public String getLogPage(Model model) {
        model.addAttribute("settings_option", "logs");
        return "settings/settings";
    }
}
