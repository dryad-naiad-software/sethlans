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
    private String password;
    private boolean isActive;
    @Transient
    private boolean passwordUpdated;

    @Override
    public String toString() {
        return "SethlansUser{" +
                "username='" + username + '\'' +
                ", roles=" + roles +
                ", isActive=" + isActive +
                ", passwordUpdated=" + passwordUpdated +
                '}';
    }
}
