package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.events.SethlansEvent;
import com.dryadandnaiad.sethlans.services.database.UserService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Created Mario Estrella on 10/20/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class AbstractSethlansController implements ApplicationListener<SethlansEvent> {
    private UserService userService;
    private boolean notification;
    private String notificationMessage;

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
        if (!firstTime) {
            return userService.getById(1).getUsername();
        }
        return "username";
    }

    @ModelAttribute("isNewNotification")
    public boolean isNotification(){
        return notification;
    }

    @ModelAttribute("notificationMessage")
    public String getNotificationMessage(){
        return notificationMessage;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }


    @Override
    public void onApplicationEvent(SethlansEvent event) {
        notification = event.isNewNotification();
        notificationMessage = event.getMessage();

    }
}
