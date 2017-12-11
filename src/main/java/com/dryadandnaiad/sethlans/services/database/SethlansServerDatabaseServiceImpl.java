/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC.
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

import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.repositories.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 12/4/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class SethlansServerDatabaseServiceImpl implements SethlansServerDatabaseService {

    private ServerRepository serverRepository;

    @Override
    public List<SethlansServer> listAll() {
        List<SethlansServer> sethlansServers = new ArrayList<>();
        serverRepository.findAll().forEach(sethlansServers::add);
        return sethlansServers;
    }

    @Override
    public SethlansServer getById(Integer id) {
        return serverRepository.findOne(id);
    }

    @Override
    public SethlansServer saveOrUpdate(SethlansServer domainObject) {
        return serverRepository.save(domainObject);
    }

    @Override
    public void delete(Integer id) {
        SethlansServer sethlansServer = serverRepository.findOne(id);
        serverRepository.delete(sethlansServer);

    }

    @Autowired
    public void setServerRepository(ServerRepository serverRepository) {
        this.serverRepository = serverRepository;
    }

    @Override
    public SethlansServer getByUUID(String uuid) {
        List<SethlansServer> sethlansServers = new ArrayList<>();
        serverRepository.findAll().forEach(sethlansServers::add);
        for (SethlansServer sethlansServer : sethlansServers) {
            if (sethlansServer.getUuid().equals(uuid)) {
                return sethlansServer;
            }
        }
        return null;
    }
}
