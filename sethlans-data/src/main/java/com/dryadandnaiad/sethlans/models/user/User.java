/*
 * Copyright (c) 2020. Dryad and Naiad Software LLC.
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.dryadandnaiad.sethlans.models.user;

import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.models.BaseEntity;
import com.dryadandnaiad.sethlans.models.blender.project.Project;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

/**
 * Created by Mario Estrella on 4/1/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Entity
public class User extends BaseEntity {
    private String username;
    private String password;
    private boolean active;
    private String email;
    private boolean welcomeEmailSent;
    private boolean securityQuestionsSet;
    private boolean promptPasswordChange;
    private boolean systemEmailNotifications;
    private boolean nodeEmailNotifications;
    private boolean projectEmailNotifications;
    private boolean videoEncodingEmailNotifications;
    private boolean passwordUpdated;
    @ElementCollection
    private List<String> tokens;
    @ElementCollection
    private List<UserChallenge> challengeList;
    @ElementCollection
    @Enumerated(value = EnumType.STRING)
    private Set<Role> roles;
    @OneToMany
    private List<Project> projects;

}
