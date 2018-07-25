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

import com.dryadandnaiad.sethlans.domains.database.server.ServerAccessKey;
import com.dryadandnaiad.sethlans.repositories.ServerAccessKeyRepository;
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
public class ServerAccessKeyDatabaseServiceImpl implements ServerAccessKeyDatabaseService {
    private ServerAccessKeyRepository serverAccessKeyRepository;


    @Override
    public List<ServerAccessKey> listAll() {
        return new ArrayList<>(serverAccessKeyRepository.findAll());
    }

    @Override
    public ServerAccessKey getById(Long id) {
        return serverAccessKeyRepository.findOne(id);
    }

    @Override
    public ServerAccessKey getByUUID(String uuid) {
        List<ServerAccessKey> accessKeyList = listAll();
        for (ServerAccessKey serverAccessKey : accessKeyList) {
            if (serverAccessKey.getAccessKey().equals(uuid)) {
                return serverAccessKey;
            }
        }
        return null;
    }

    @Override
    public ServerAccessKey saveOrUpdate(ServerAccessKey domainObject) {
        return serverAccessKeyRepository.save(domainObject);
    }

    @Override
    public void delete(Long id) {
        serverAccessKeyRepository.delete(id);
    }

    @Override
    public void delete(String uuid) {
        List<ServerAccessKey> serverAccessKeyList = listAll();
        for (ServerAccessKey serverAccessKey : serverAccessKeyList) {
            if (serverAccessKey.getAccessKey().equals(uuid)) {
                serverAccessKeyRepository.delete(serverAccessKey);
            }
        }
    }

    @Autowired
    public void setServerAccessKeyRepository(ServerAccessKeyRepository serverAccessKeyRepository) {
        this.serverAccessKeyRepository = serverAccessKeyRepository;
    }
}
