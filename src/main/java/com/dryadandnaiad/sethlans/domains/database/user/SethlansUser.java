package com.dryadandnaiad.sethlans.domains.database.user;

import com.dryadandnaiad.sethlans.domains.database.AbstractEntityClass;
import com.dryadandnaiad.sethlans.enums.Role;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;
import javax.persistence.Enumerated;

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
    @Enumerated
    private Role role;
    private String password;
    private boolean isActive;
}
