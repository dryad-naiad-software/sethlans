package com.dryadandnaiad.sethlans.services.database;

import com.dryadandnaiad.sethlans.domains.database.user.SethlansUser;

/**
 * Created Mario Estrella on 2/23/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public interface SethlansUserDatabaseService extends CRUDService<SethlansUser> {
    boolean checkifExists(String username);

    void delete(SethlansUser sethlansUser);

    SethlansUser findByUserName(String username);
}
