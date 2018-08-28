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

package com.dryadandnaiad.sethlans.domains.database.user;

import com.dryadandnaiad.sethlans.domains.database.AbstractEntityClass;
import com.dryadandnaiad.sethlans.enums.Role;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Transient;
import java.util.List;

/**
 * Created Mario Estrella on 2/16/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class SethlansUser extends AbstractEntityClass {
    private String username;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Role> roles;
    @ElementCollection
    private List<SethlansUserChallenge> challengeList;
    private String password;
    private boolean active;
    private String email;
    private boolean welcomeEmailSent;
    private boolean securityQuestionsSet;
    private boolean promptPasswordChange;
    @Transient
    private boolean passwordUpdated;

    @Override
    public String toString() {
        return "SethlansUser{" +
                "username=" + username +
                ", roles=" + roles +
                ", active=" + active +
                ", email=" + email +
                ", challengeList=" + challengeList +
                ", welcomeEmailSent=" + welcomeEmailSent +
                ", passwordUpdated=" + passwordUpdated +
                '}';
    }
}
