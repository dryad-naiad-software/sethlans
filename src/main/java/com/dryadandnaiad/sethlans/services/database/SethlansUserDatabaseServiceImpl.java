package com.dryadandnaiad.sethlans.services.database;

import com.dryadandnaiad.sethlans.domains.database.user.SethlansUser;
import com.dryadandnaiad.sethlans.repositories.SethlansUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 2/23/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class SethlansUserDatabaseServiceImpl implements SethlansUserDatabaseService {
    private SethlansUserRepository sethlansUserRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private static final Logger LOG = LoggerFactory.getLogger(SethlansUserDatabaseServiceImpl.class);


    @Override
    public List<SethlansUser> listAll() {
        return new ArrayList<>(sethlansUserRepository.findAll());
    }

    @Override
    public SethlansUser getById(Long id) {
        return sethlansUserRepository.findOne(id);
    }

    @Override
    public SethlansUser saveOrUpdate(SethlansUser domainObject) {
        LOG.debug("Saving/Updating user");

        if (domainObject.isPasswordUpdated()) {
            LOG.debug("Encrypting password");
            domainObject.setPassword(bCryptPasswordEncoder.encode(domainObject.getPassword()));
        }
        return sethlansUserRepository.save(domainObject);
    }

    @Override
    public void delete(Long id) {
        sethlansUserRepository.delete(id);
    }

    @Override
    public void delete(SethlansUser sethlansUser) {
        sethlansUserRepository.delete(sethlansUser);
    }

    @Autowired
    public void setSethlansUserRepository(SethlansUserRepository sethlansUserRepository) {
        this.sethlansUserRepository = sethlansUserRepository;
    }

    @Autowired
    public void setbCryptPasswordEncoder(BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }
}
