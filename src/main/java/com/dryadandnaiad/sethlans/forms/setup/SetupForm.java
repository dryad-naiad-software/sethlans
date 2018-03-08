package com.dryadandnaiad.sethlans.forms.setup;

import com.dryadandnaiad.sethlans.domains.database.user.SethlansUser;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.forms.setup.subclasses.SetupNode;
import com.dryadandnaiad.sethlans.forms.setup.subclasses.SetupServer;
import lombok.Data;

/**
 * Created Mario Estrella on 2/23/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Data
public class SetupForm {
    private SethlansMode mode;
    private SethlansUser user;
    private String ipAddress;
    private String port;
    private String rootDirectory;
    private SetupServer server;
    private SetupNode node;


}
