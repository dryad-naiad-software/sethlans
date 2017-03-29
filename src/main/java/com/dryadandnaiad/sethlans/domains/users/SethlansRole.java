/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC
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

package com.dryadandnaiad.sethlans.domains.users;

import com.dryadandnaiad.sethlans.domains.AbstractEntityClass;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 3/18/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Entity
public class SethlansRole extends AbstractEntityClass {
    private String role;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable
    // ~ defaults to @JoinTable(name = "USER_ROLE", joinColumns = @JoinColumn(name = "role_id"),
    //     inverseJoinColumns = @joinColumn(name = "user_id"))
    private List<SethlansUser> sethlansUsers = new ArrayList<>();

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<SethlansUser> getSethlansUsers() {
        return sethlansUsers;
    }

    public void setSethlansUsers(List<SethlansUser> sethlansUsers) {
        this.sethlansUsers = sethlansUsers;
    }

    public void addUser(SethlansUser sethlansUser) {
        if (!this.sethlansUsers.contains(sethlansUser)) {
            this.sethlansUsers.add(sethlansUser);
        }

        if (!sethlansUser.getSethlansRoles().contains(this)) {
            sethlansUser.getSethlansRoles().add(this);
        }
    }

    public void removeUser(SethlansUser sethlansUser) {
        this.sethlansUsers.remove(sethlansUser);
        sethlansUser.getSethlansRoles().remove(this);
    }
}
