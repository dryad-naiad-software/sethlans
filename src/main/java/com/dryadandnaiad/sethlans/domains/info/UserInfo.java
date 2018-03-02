package com.dryadandnaiad.sethlans.domains.info;

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
    private boolean isActive;
    private String email;
    private long id;
    private Date dateCreated;
    private Date lastUpdated;
}
