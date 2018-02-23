package com.dryadandnaiad.sethlans.services.database;

import com.dryadandnaiad.sethlans.domains.database.user.SethlansUser;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created Mario Estrella on 2/23/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class SethlansUserDatabaseServiceImpl implements SethlansUserDatabaseService {
    @Override
    public List<SethlansUser> listAll() {
        return null;
    }

    @Override
    public SethlansUser getById(Long id) {
        return null;
    }

    @Override
    public SethlansUser saveOrUpdate(SethlansUser domainObject) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }
}
