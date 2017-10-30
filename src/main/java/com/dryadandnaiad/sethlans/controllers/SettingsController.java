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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created Mario Estrella on 9/20/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Controller
public class SettingsController extends AbstractSethlansController {
    private static final Logger LOG = LoggerFactory.getLogger(SettingsController.class);

    @RequestMapping("/settings")
    public String getHomePage(Model model) {
        model.addAttribute("settings_option", "home");
        return "settings/settings";
    }

    @RequestMapping("/settings/users")
    public String getUserPage(Model model){
        model.addAttribute("settings_option", "users");
        return "settings/settings";
    }

    @RequestMapping("/settings/nodes")
    public String getNodePage(Model model){
        model.addAttribute("settings_option", "nodes");
        return "settings/settings";
    }

    @RequestMapping("/settings/nodes/add")
    public String getNodeAddPage(Model model){
        model.addAttribute("settings_option", "nodes_add");
        return "settings/settings";
    }

    @RequestMapping("/settings/nodes/scan")
    public String getNodeScanPage(Model model){
        model.addAttribute("settings_option", "nodes_scan");
        return "settings/settings";
    }

}
