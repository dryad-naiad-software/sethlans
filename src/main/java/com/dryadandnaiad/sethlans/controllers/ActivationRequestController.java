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

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.enums.NotificationOrigin;
import com.dryadandnaiad.sethlans.events.SethlansEvent;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.network.NodeStatusUpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created Mario Estrella on 12/4/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@Profile({"NODE", "DUAL"})
public class ActivationRequestController implements ApplicationEventPublisherAware {
    private static final Logger LOG = LoggerFactory.getLogger(ActivationRequestController.class);
    private SethlansServerDatabaseService sethlansServerDatabaseService;
    private ApplicationEventPublisher applicationEventPublisher;
    private NodeStatusUpdateService nodeStatusUpdateService;

    @RequestMapping(value = "/api/nodeactivate/request", method = RequestMethod.POST)
    public void nodeActivationRequest(@RequestParam String serverhostname, @RequestParam String ipAddress,
                                      @RequestParam String port, @RequestParam String connection_uuid) {
        LOG.debug("Received node activation request");
        if (sethlansServerDatabaseService.getByConnectionUUID(connection_uuid) != null) {
            LOG.debug("Server UUID is already present on node. Skipping Activation");
        } else {
            SethlansServer sethlansServer = new SethlansServer();
            sethlansServer.setHostname(serverhostname);
            sethlansServer.setIpAddress(ipAddress);
            sethlansServer.setNetworkPort(port);
            sethlansServer.setConnection_uuid(connection_uuid);
            sethlansServerDatabaseService.saveOrUpdate(sethlansServer);
            LOG.debug(sethlansServer.toString());
            LOG.debug("Processed node activation request");
            String notification = "New Server Request: " + serverhostname;
            this.applicationEventPublisher.publishEvent(new SethlansEvent(this, connection_uuid + "-" + NotificationOrigin.ACTIVATION_REQUEST.toString(), notification, true));
        }
    }

    @RequestMapping(value = "/api/nodeactivate/removal", method = RequestMethod.POST)
    public void serverDeletionRequest(@RequestParam String connection_uuid) {
        LOG.debug("Received server deletion request");
        if (sethlansServerDatabaseService.getByConnectionUUID(connection_uuid) != null) {
            LOG.debug("Server UUID found, deleting entry.");
            sethlansServerDatabaseService.deleteByConnectionUUID(connection_uuid);
        } else {
            LOG.debug("Server not found in database.");
        }

    }

    @RequestMapping(value = "/api/nodeactivate/acknowledge", method = RequestMethod.POST)
    public void nodeActivationAcknowledge(@RequestParam String connection_uuid) {
        if (sethlansServerDatabaseService.getByConnectionUUID(connection_uuid) != null) {
            SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(connection_uuid);
            LOG.debug("Received Server Acknowledgement for " + sethlansServer.getHostname());
            sethlansServer.setPendingAcknowledgementResponse(false);
            sethlansServer.setAcknowledged(true);
            sethlansServerDatabaseService.saveOrUpdate(sethlansServer);
            try {
                Thread.sleep(5000);
                nodeStatusUpdateService.nodeUpdatePullRequest();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else {
            LOG.debug("No such server present in database");
        }


    }

    @Autowired
    public void setSethlansServerDatabaseService(SethlansServerDatabaseService sethlansServerDatabaseService) {
        this.sethlansServerDatabaseService = sethlansServerDatabaseService;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Autowired
    public void setNodeStatusUpdateService(NodeStatusUpdateService nodeStatusUpdateService) {
        this.nodeStatusUpdateService = nodeStatusUpdateService;
    }
}
