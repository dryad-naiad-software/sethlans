package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created Mario Estrella on 2/11/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
public class HomeController {
    @Value("${sethlans.firsttime}")
    private boolean firstTime;

    @GetMapping(value = {"/api/info/first_time"})
    public boolean isFirstTime() {
        return firstTime;
    }

    @GetMapping(value = {"/api/info/version"})
    public String getVersion() {
        return SethlansUtils.getVersion();
    }
}
