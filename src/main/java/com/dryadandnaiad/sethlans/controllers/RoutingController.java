package com.dryadandnaiad.sethlans.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created Mario Estrella on 2/24/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Controller
public class RoutingController {

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String redirectLogin() {
        return "forward:/";
    }

    @RequestMapping(value = "/")
    public String getRoot() {
        return "index.html";
    }

    @RequestMapping(value = {
            "/settings",
            "/register",
            "/user_settings",
            "/admin/metrics",
            "/admin/user_management",
            "/admin/sethlans_settings",
            "/admin/compute_settings",
            "/admin/blender_version_admin",
            "/admin/logs"})
    public String getUrl() {
        return "forward:/";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String loginPost() {
        return "forward:/";
    }


}
