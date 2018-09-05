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

package com.dryadandnaiad.sethlans.domains.info;

import com.dryadandnaiad.sethlans.domains.database.user.SethlansUserChallenge;
import com.dryadandnaiad.sethlans.enums.Role;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created Mario Estrella on 2/26/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Data
public class UserInfo {
    private String username;
    private List<Role> roles;
    private List<SethlansUserChallenge> userChallengeList;
    private boolean active;
    private String email;
    private long id;
    private Date dateCreated;
    private Date lastUpdated;
    private boolean systemEmailNotifications;
    private boolean nodeEmailNotifications;
    private boolean projectEmailNotifications;
    private boolean videoEncodingEmailNotifications;
}
