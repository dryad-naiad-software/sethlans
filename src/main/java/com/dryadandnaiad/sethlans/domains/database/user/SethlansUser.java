package com.dryadandnaiad.sethlans.domains.database.user;

import com.dryadandnaiad.sethlans.domains.database.AbstractEntityClass;
import com.dryadandnaiad.sethlans.enums.Roles;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
    private List<Roles> roles;
    private String password;
    private boolean isActive;
}
