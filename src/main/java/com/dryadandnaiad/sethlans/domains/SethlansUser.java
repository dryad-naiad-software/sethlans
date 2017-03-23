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

package com.dryadandnaiad.sethlans.domains;

import com.dryadandnaiad.sethlans.domains.security.SethlansRole;
import org.springframework.data.annotation.Transient;

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
public class SethlansUser extends AbstractEntityClass {

    private String username;

    @Transient
    private String password;

    private String encryptedPassword;
    private boolean enabled = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable
    // ~ defaults to @JoinTable(name = "USER_ROLE", joinColumns = @JoinColumn(name = "user_id"),
    //     inverseJoinColumns = @joinColumn(name = "role_id"))
    private List<SethlansRole> sethlansRoles = new ArrayList<>();

    public List<SethlansRole> getSethlansRoles() {
        return sethlansRoles;
    }

    public void setSethlansRoles(List<SethlansRole> sethlansRoles) {
        this.sethlansRoles = sethlansRoles;
    }

    public void addRole(SethlansRole sethlansRole) {
        if (!this.sethlansRoles.contains(sethlansRole)) {
            this.sethlansRoles.add(sethlansRole);
        }

        if (!sethlansRole.getSethlansUsers().contains(this)) {
            sethlansRole.getSethlansUsers().add(this);
        }
    }

    public void removeRole(SethlansRole sethlansRole) {
        this.sethlansRoles.remove(sethlansRole);
        sethlansRole.getSethlansUsers().remove(this);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
