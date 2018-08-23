package com.dryadandnaiad.sethlans.forms.setup.subclasses;

import lombok.Data;

/**
 * Created Mario Estrella on 8/23/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: Sethlans
 */
@Data
public class MailSettings {
    private String mailHost;
    private String mailPort;
    private String username;
    private String password;
    private String replyToAddress;
    private boolean smtpAuth;
    private boolean startTLSEnable;
    private boolean startTLSRequired;
}
