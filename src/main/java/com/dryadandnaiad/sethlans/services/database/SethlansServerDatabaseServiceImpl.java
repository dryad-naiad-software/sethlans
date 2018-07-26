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

import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.repositories.ServerRepository;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
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
    private SethlansAPIConnectionService sethlansAPIConnectionService;


    @Override
    public List<SethlansServer> listAll() {
        return new ArrayList<>(serverRepository.findAll());
    }

    @Override
    public List<SethlansServer> listActive() {
        List<SethlansServer> listToReturn = new ArrayList<>();
        for (SethlansServer sethlansServer : listAll()) {
            if (sethlansServer.isAcknowledged()) {
                listToReturn.add(sethlansServer);
            }
        }
        return listToReturn;
    }

    @Override
    public SethlansServer getById(Long id) {
        return serverRepository.findOne(id);
    }

    @Override
    public SethlansServer saveOrUpdate(SethlansServer domainObject) {
        return serverRepository.save(domainObject);
    }

    @Override
    public void delete(Long id) {
        SethlansServer sethlansServer = serverRepository.findOne(id);
        String connectionURL = "https://" + sethlansServer.getIpAddress() + ":" + sethlansServer.getNetworkPort() + "/api/nodeactivate/server_removal";
        String params = "connection_uuid=" + sethlansServer.getConnection_uuid();
        sethlansAPIConnectionService.sendToRemotePOST(connectionURL, params);
        serverRepository.delete(sethlansServer);
    }

    @Override
    public void deleteByConnectionUUID(String uuid) {
        SethlansServer sethlansServer = getByConnectionUUID(uuid);
        serverRepository.delete(sethlansServer);

    }

    @Override
    public SethlansServer getByConnectionUUID(String uuid) {
        List<SethlansServer> sethlansServers = listAll();
        for (SethlansServer sethlansServer : sethlansServers) {
            if (sethlansServer.getConnection_uuid().equals(uuid)) {
                return sethlansServer;
            }
        }
        return null;
    }

    @Autowired
    public void setSethlansAPIConnectionService(SethlansAPIConnectionService sethlansAPIConnectionService) {
        this.sethlansAPIConnectionService = sethlansAPIConnectionService;
    }

    @Autowired
    public void setServerRepository(ServerRepository serverRepository) {
        this.serverRepository = serverRepository;
    }

}
