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
import com.dryadandnaiad.sethlans.services.database.SethlansUserDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Created Mario Estrella on 8/30/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class SethlansEmailServiceImpl implements SethlansEmailService {
    private SethlansUserDatabaseService sethlansUserDatabaseService;
    private JavaMailSender emailSender;

    @Override
    public boolean sendWelcomeEmail(SethlansUser sethlansUser) {
        return false;
    }

    @Override
    public void sendNotificationEmail(SethlansNotification sethlansNotification) {

    }

    private void sendProjectNotification() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("Sethlans Project Notification: ");

    }

    private void sendVideoNotification() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("Sethlans Video Encoding Notification: ");
    }

    private void sendAdminNotification() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("Sethlans Admin Notification: ");


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
