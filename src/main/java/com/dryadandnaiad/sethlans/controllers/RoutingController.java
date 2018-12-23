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

package com.dryadandnaiad.sethlans.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Created Mario Estrella on 2/24/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Controller
public class RoutingController {

    @GetMapping(value = "/login")
    public String redirectLogin() {
        return "forward:/";
    }

    @GetMapping(value = "/")
    public String getRoot() {
        return "index.html";
    }

    @GetMapping(value = {
            "/settings",
            "/get_started",
            "/register",
            "/projects",
            "/restart",
            "/shutdown",
            "/forgot_pass",
            "/projects/**",
            "/user_settings",
            "/admin/metrics",
            "/admin/user_management",
            "/admin/user_management/**",
            "/admin/sethlans_settings",
            "/admin/nodes",
            "/admin/nodes/add",
            "/admin/nodes/edit/**",
            "/admin/nodes/scan",
            "/admin/render_history",
            "/admin/servers",
            "/admin/compute_settings",
            "/admin/blender_version_admin",
            "/admin/logs"})
    public String getUrl() {
        return "forward:/";
    }

    @PostMapping(value = "/login")
    public String loginPost() {
        return "forward:/";
    }


}
