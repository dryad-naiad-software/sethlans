/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
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

package com.dryadandnaiad.sethlans.services.database;

import com.dryadandnaiad.sethlans.domains.database.user.SethlansUser;
import com.dryadandnaiad.sethlans.enums.Role;
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
    public SethlansUser excludeSuperUsersById(Long id) {
        if (!sethlansUserRepository.findOne(id).getRoles().contains(Role.SUPER_ADMINISTRATOR)) {
            return sethlansUserRepository.findOne(id);
        } else {
            return null;
        }

    }

    @Override
    public boolean checkifExists(String username) {
        return findByUserName(username) != null;

    }

    @Override
    public SethlansUser saveOrUpdate(SethlansUser domainObject) {
        LOG.debug("Saving/Updating user");

        if (domainObject.isPasswordUpdated()) {
            LOG.debug("Encrypting password");
            domainObject.setPassword(bCryptPasswordEncoder.encode(domainObject.getPassword()));
            // after user is updated, set password updated to false in order to prevent encoding an encoded password.
            domainObject.setPasswordUpdated(false);
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

    @Override
    public SethlansUser findByUserName(String username) {
        return sethlansUserRepository.findByUsername(username);

    }

    @Override
    public List<SethlansUser> excludeSuperAdministrators() {
        List<SethlansUser> sethlansUsers = new ArrayList<>();
        for (SethlansUser sethlansUser : listAll()) {
            if (!sethlansUser.getRoles().contains(Role.SUPER_ADMINISTRATOR)) {
                sethlansUsers.add(sethlansUser);
            }
        }
        return sethlansUsers;
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
