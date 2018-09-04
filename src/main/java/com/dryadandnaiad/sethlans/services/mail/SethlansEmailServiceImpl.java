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

package com.dryadandnaiad.sethlans.services.mail;

import com.dryadandnaiad.sethlans.domains.database.events.SethlansNotification;
import com.dryadandnaiad.sethlans.domains.database.user.SethlansUser;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.database.SethlansUserDatabaseService;
import com.dryadandnaiad.sethlans.utils.SethlansConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 8/30/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class SethlansEmailServiceImpl implements SethlansEmailService {
    private static final Logger LOG = LoggerFactory.getLogger(SethlansEmailServiceImpl.class);
    private SethlansUserDatabaseService sethlansUserDatabaseService;
    private JavaMailSender emailSender;

    @Override
    @Async
    public void sendWelcomeEmailToAdmin() {
        try {
            Thread.sleep(5000);
            if (Boolean.parseBoolean(SethlansConfigUtils.getProperty(SethlansConfigKeys.MAIL_SERVER_CONFIGURED))) {
                if (sethlansUserDatabaseService.numberOfSuperAdministrators() == 1 && sethlansUserDatabaseService.tableSize() == 1) {
                    SethlansUser sethlansUser = sethlansUserDatabaseService.listAll().get(0);
                    if (!sethlansUser.isWelcomeEmailSent()) {
                        sethlansUser.setWelcomeEmailSent(sendWelcomeEmail(sethlansUser));
                        sethlansUserDatabaseService.saveOrUpdate(sethlansUser);
                    }
                }
            }
        } catch (InterruptedException e) {
            LOG.debug("Sethlans Email Service is being restarted.");
        }

    }

    @Override
    public boolean sendWelcomeEmail(SethlansUser sethlansUser) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(sethlansUser.getEmail());
        message.setFrom(SethlansConfigUtils.getProperty(SethlansConfigKeys.MAIL_REPLYTO));
        message.setReplyTo(SethlansConfigUtils.getProperty(SethlansConfigKeys.MAIL_REPLYTO));
        message.setSubject("Welcome to Sethlans, " + sethlansUser.getUsername() + "!");
        String welcomeMessage = "Hello " + sethlansUser.getUsername() + "\n\n" +
                "You are receiving this email because you have registered with a Sethlans installation located at: "
                + SethlansConfigUtils.getProperty(SethlansConfigKeys.SETHLANS_URL);

        if (sethlansUser.isPromptPasswordChange() || !sethlansUser.isSecurityQuestionsSet()) {
            welcomeMessage = welcomeMessage + "\n\nOnce logging in please perform the following actions:\n";
        }

        if (sethlansUser.isPromptPasswordChange()) {
            welcomeMessage = welcomeMessage + "\n- Change your password.";
        }

        if (!sethlansUser.isSecurityQuestionsSet()) {
            welcomeMessage = welcomeMessage + "\n- Select your password reset questions and enter your responses.";
        }

        message.setText(welcomeMessage);
        LOG.debug("Sending welcome email to " + sethlansUser.getUsername());
        emailSender.send(message);
        return true;
    }

    @Override
    public void sendNotificationEmail(SethlansNotification sethlansNotification) {
        SethlansUser sethlansUser = sethlansUserDatabaseService.findByUserName(sethlansNotification.getUsername());
        switch (sethlansNotification.getNotificationType()) {
            case NODE:
                sendNodeNotification(sethlansNotification);
                break;
            case VIDEO:
                if (sethlansUser.isVideoEncodingEmailNotifications()) {
                    sendVideoNotification(sethlansNotification, sethlansUser);
                }
                break;
            case SYSTEM:
                sendSystemNotification(sethlansNotification);
                break;
            case PROJECT:
                if (sethlansUser.isProjectEmailNotifications()) {
                    sendProjectNotification(sethlansNotification, sethlansUser);
                }
                break;
            case BLENDER_DOWNLOAD:
                sendBlenderDownloadNotification(sethlansNotification);
                break;
            default:
                LOG.error("Notification Type: " + sethlansNotification.getNotificationType() + " is not a supported mailable type");

        }

    }

    private void sendProjectNotification(SethlansNotification sethlansNotification, SethlansUser sethlansUser) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(sethlansUser.getEmail());
        message.setFrom(SethlansConfigUtils.getProperty(SethlansConfigKeys.MAIL_REPLYTO));
        message.setReplyTo(SethlansConfigUtils.getProperty(SethlansConfigKeys.MAIL_REPLYTO));
        message.setSubject("Sethlans Project Notification: " + sethlansNotification.getSubject());
        message.setText(notificationText(sethlansNotification));
        emailSender.send(message);
    }


    private void sendVideoNotification(SethlansNotification sethlansNotification, SethlansUser sethlansUser) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(sethlansUser.getEmail());
        message.setFrom(SethlansConfigUtils.getProperty(SethlansConfigKeys.MAIL_REPLYTO));
        message.setReplyTo(SethlansConfigUtils.getProperty(SethlansConfigKeys.MAIL_REPLYTO));
        message.setSubject("Sethlans Video Encoding Notification: " + sethlansNotification.getSubject());
        message.setText(notificationText(sethlansNotification));
        emailSender.send(message);
    }

    private void sendBlenderDownloadNotification(SethlansNotification sethlansNotification) {
        SimpleMailMessage message = new SimpleMailMessage();
        List<SethlansUser> sethlansUsers = sethlansUserDatabaseService.excludeUsers();
        List<String> emailAddresses = new ArrayList<>();
        for (SethlansUser sethlansUser : sethlansUsers) {
            if (sethlansUser.isBlenderDownloadEmailNotifications()) {
                emailAddresses.add(sethlansUser.getEmail());
            }
        }
        message.setTo(emailAddresses.toArray(new String[0]));
        message.setFrom(SethlansConfigUtils.getProperty(SethlansConfigKeys.MAIL_REPLYTO));
        message.setReplyTo(SethlansConfigUtils.getProperty(SethlansConfigKeys.MAIL_REPLYTO));
        message.setSubject("Sethlans Blender Download Notification: " + sethlansNotification.getSubject());
        message.setText(notificationText(sethlansNotification));
        emailSender.send(message);
    }

    private void sendNodeNotification(SethlansNotification sethlansNotification) {
        List<SethlansUser> sethlansUsers = sethlansUserDatabaseService.excludeUsers();
        List<String> emailAddresses = new ArrayList<>();
        for (SethlansUser sethlansUser : sethlansUsers) {
            if (sethlansUser.isNodeEmailNotifications()) {
                emailAddresses.add(sethlansUser.getEmail());
            }
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailAddresses.toArray(new String[0]));
        message.setFrom(SethlansConfigUtils.getProperty(SethlansConfigKeys.MAIL_REPLYTO));
        message.setReplyTo(SethlansConfigUtils.getProperty(SethlansConfigKeys.MAIL_REPLYTO));
        message.setSubject("Sethlans Node Notification: " + sethlansNotification.getSubject());
        message.setText(notificationText(sethlansNotification));
        emailSender.send(message);
    }

    private void sendSystemNotification(SethlansNotification sethlansNotification) {
        List<SethlansUser> sethlansUsers = sethlansUserDatabaseService.excludeUsers();
        List<String> emailAddresses = new ArrayList<>();
        for (SethlansUser sethlansUser : sethlansUsers) {
            if (sethlansUser.isSystemEmailNotifications()) {
                emailAddresses.add(sethlansUser.getEmail());
            }
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(emailAddresses.toArray(new String[0]));
        message.setFrom(SethlansConfigUtils.getProperty(SethlansConfigKeys.MAIL_REPLYTO));
        message.setReplyTo(SethlansConfigUtils.getProperty(SethlansConfigKeys.MAIL_REPLYTO));
        message.setSubject("Sethlans System Notification: " + sethlansNotification.getSubject());
        message.setText(notificationText(sethlansNotification));
        emailSender.send(message);
    }

    private String notificationText(SethlansNotification sethlansNotification) {
        String sethlansURL = SethlansConfigUtils.getProperty(SethlansConfigKeys.SETHLANS_URL);
        String textToSend = "Hello, " + "\n\n" +
                "The following notification has been sent by the Sethlans installation at: " + sethlansURL + "\n\n" +
                "\"" + sethlansNotification.getMessage() + "\"";
        if (sethlansNotification.isLinkPresent()) {
            String urlToSend = "\n\nThe results can be seen at the following URL: \n\n"
                    + sethlansURL + sethlansNotification.getMessageLink();
            textToSend = textToSend + urlToSend;
        }

        return textToSend;

    }

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public void setEmailSender(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }

    @Autowired
    public void setSethlansUserDatabaseService(SethlansUserDatabaseService sethlansUserDatabaseService) {
        this.sethlansUserDatabaseService = sethlansUserDatabaseService;
    }
}
