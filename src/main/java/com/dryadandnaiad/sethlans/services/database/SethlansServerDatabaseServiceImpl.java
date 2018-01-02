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

import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.enums.NotificationOrigin;
import com.dryadandnaiad.sethlans.events.SethlansEvent;
import com.dryadandnaiad.sethlans.repositories.ServerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
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
public class SethlansServerDatabaseServiceImpl implements SethlansServerDatabaseService, ApplicationEventPublisherAware {

    private ServerRepository serverRepository;
    private ApplicationEventPublisher applicationEventPublisher;

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
        this.applicationEventPublisher.publishEvent(new SethlansEvent(this, sethlansServer.getConnection_uuid() + "-" + NotificationOrigin.ACTIVATION_REQUEST, false));
        serverRepository.delete(sethlansServer);

    }

    @Override
    public void deleteByConnectionUUID(String uuid) {
        SethlansServer sethlansServer = getByConnectionUUID(uuid);
        this.applicationEventPublisher.publishEvent(new SethlansEvent(this, sethlansServer.getConnection_uuid() + "-" + NotificationOrigin.ACTIVATION_REQUEST, false));
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
    public void setServerRepository(ServerRepository serverRepository) {
        this.serverRepository = serverRepository;
    }


    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}
