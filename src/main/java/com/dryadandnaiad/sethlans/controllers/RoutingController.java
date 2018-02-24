package com.dryadandnaiad.sethlans.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created Mario Estrella on 2/24/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Controller
public class RoutingController {

    @RequestMapping("/")
    public String home() {
        return "index.html";
    }

    @RequestMapping("/login")
    public String login() {
        return "index.html";
    }
}
