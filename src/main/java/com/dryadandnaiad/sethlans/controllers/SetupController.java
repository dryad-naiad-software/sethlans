package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.forms.SetupForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created Mario Estrella on 2/11/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@RequestMapping("/api/setup")
public class SetupController {
    private static final Logger LOG = LoggerFactory.getLogger(SetupController.class);

    @PostMapping("/submit")
    public boolean submit(@RequestBody SetupForm setupForm) {
        if (setupForm != null) {
            LOG.debug(setupForm.toString());
            return true;
        } else {
            return false;
        }
    }
}
