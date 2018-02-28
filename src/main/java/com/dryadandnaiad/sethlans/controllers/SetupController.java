package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.domains.database.user.SethlansUser;
import com.dryadandnaiad.sethlans.forms.SetupForm;
import com.dryadandnaiad.sethlans.services.config.SaveSetupConfigService;
import com.dryadandnaiad.sethlans.services.database.SethlansUserDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private SaveSetupConfigService saveSetupConfigService;
    private SethlansUserDatabaseService sethlansUserDatabaseService;


    @PostMapping("/submit")
    public boolean submit(@RequestBody SetupForm setupForm) {
        LOG.debug("Submitting Setup Form...");
        if (setupForm != null) {
            LOG.debug(setupForm.toString());
            saveSetupConfigService.saveSetupSettings(setupForm);
            return true;
        } else {
            return false;
        }
    }

    @PostMapping("/register")
    public boolean register(@RequestBody SethlansUser user) {
        if (user != null) {
            LOG.debug("Registering new user...");
            if (sethlansUserDatabaseService.checkifExists(user.getUsername())) {
                LOG.debug("User " + user.getUsername() + " already exists!");
                return false;
            }
            sethlansUserDatabaseService.saveOrUpdate(user);
            LOG.debug("Saving " + user.toString() + " to database.");
            return true;
        } else {
            return false;
        }
    }

    @Autowired
    public void setSaveSetupConfigService(SaveSetupConfigService saveSetupConfigService) {
        this.saveSetupConfigService = saveSetupConfigService;
    }

    @Autowired
    public void setSethlansUserDatabaseService(SethlansUserDatabaseService sethlansUserDatabaseService) {
        this.sethlansUserDatabaseService = sethlansUserDatabaseService;
    }
}
