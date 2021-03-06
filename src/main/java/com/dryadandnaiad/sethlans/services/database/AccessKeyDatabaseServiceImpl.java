/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC
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

import com.dryadandnaiad.sethlans.domains.database.server.AccessKey;
import com.dryadandnaiad.sethlans.repositories.AccessKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 7/24/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class AccessKeyDatabaseServiceImpl implements AccessKeyDatabaseService {
    private AccessKeyRepository accessKeyRepository;


    @Override
    public long tableSize() {
        return accessKeyRepository.count();
    }

    @Override
    public List<AccessKey> listAll() {
        return new ArrayList<>(accessKeyRepository.findAll());
    }

    @Override
    public AccessKey getById(Long id) {
        return accessKeyRepository.findOne(id);
    }

    @Override
    public AccessKey getByUUID(String uuid) {
        return accessKeyRepository.findAccessKeyByAccessKey(uuid);
    }

    @Override
    public AccessKey saveOrUpdate(AccessKey domainObject) {
        return accessKeyRepository.save(domainObject);
    }

    @Override
    public void delete(Long id) {
        accessKeyRepository.delete(id);
    }

    @Override
    public void delete(String uuid) {
        accessKeyRepository.delete(accessKeyRepository.findAccessKeyByAccessKey(uuid));
    }

    @Autowired
    public void setAccessKeyRepository(AccessKeyRepository accessKeyRepository) {
        this.accessKeyRepository = accessKeyRepository;
    }
}
