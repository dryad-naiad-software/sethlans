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

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.domains.database.events.SethlansNotification;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.enums.NotificationScope;
import com.dryadandnaiad.sethlans.enums.NotificationType;
import com.dryadandnaiad.sethlans.services.database.AccessKeyDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.services.notification.SethlansNotificationService;
import com.dryadandnaiad.sethlans.utils.SethlansQueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/nodeactivate/")
public class ActivationRequestController {
    /**
     * This is the Rest Controller on the Node that receives the requests for node activations
     */
    private static final Logger LOG = LoggerFactory.getLogger(ActivationRequestController.class);
    private SethlansServerDatabaseService sethlansServerDatabaseService;
    private AccessKeyDatabaseService accessKeyDatabaseService;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private SethlansNotificationService sethlansNotificationService;



    @RequestMapping(value = "/request", method = RequestMethod.POST)
    public boolean nodeActivationRequest(@RequestParam String serverhostname, @RequestParam String ipAddress,
                                         @RequestParam String port, @RequestParam String connection_uuid, @RequestParam String access_key) {
        LOG.debug("Received node activation request");
        if (accessKeyDatabaseService.getByUUID(access_key) == null) {
            LOG.info("Access key provided is not authorized on this node.");
            return false;
        }
        if (sethlansServerDatabaseService.getByConnectionUUID(connection_uuid) != null) {
            LOG.debug("Server UUID is already present on node. Skipping Activation");
            return false;
        } else {
            SethlansServer sethlansServer = new SethlansServer();
            sethlansServer.setHostname(serverhostname);
            sethlansServer.setIpAddress(ipAddress);
            sethlansServer.setNetworkPort(port);
            sethlansServer.setConnection_uuid(connection_uuid);
            sethlansServer.setNodeUpdated(false);
            sethlansServer.setPendingAcknowledgementResponse(true);
            sethlansServerDatabaseService.saveOrUpdate(sethlansServer);
            String message = "Added " + serverhostname + " as a server";
            SethlansNotification sethlansNotification = new SethlansNotification(NotificationType.SERVER, message, NotificationScope.ADMIN);
            sethlansNotification.setLinkPresent(true);
            sethlansNotification.setMessageLink("/admin/servers");
            sethlansNotificationService.sendNotification(sethlansNotification);
            LOG.debug(sethlansServer.toString());
            LOG.debug("Processed node activation request");
            sendActivationResponseToServer(sethlansServer, SethlansQueryUtils.getCurrentNodeInfo());
            return true;
        }
    }

    private void sendActivationResponseToServer(SethlansServer sethlansServer, SethlansNode sethlansNode) {
        LOG.debug("Sending Activation Response to Server");
        String ip = sethlansServer.getIpAddress();
        String port = sethlansServer.getNetworkPort();
        String responseURL = "https://" + ip + ":" + port + "/api/nodeactivate/response";
        String params = "nodehostname=" + sethlansNode.getHostname() + "&ipAddress=" + sethlansNode.getIpAddress()
                + "&port=" + sethlansNode.getNetworkPort() + "&connection_uuid=" + sethlansServer.getConnection_uuid();
        sethlansAPIConnectionService.sendToRemotePOST(responseURL, params);
    }

    @RequestMapping(value = "/removal", method = RequestMethod.POST)
    public void serverDeletionRequest(@RequestParam String connection_uuid) {
        LOG.debug("Received server deletion request");
        if (sethlansServerDatabaseService.getByConnectionUUID(connection_uuid) != null) {
            LOG.debug("Server UUID found, deleting entry.");
            sethlansServerDatabaseService.deleteByConnectionUUID(connection_uuid);
        } else {
            LOG.debug("Server not found in database.");
        }

    }

    @RequestMapping(value = "/acknowledge", method = RequestMethod.POST)
    public void nodeActivationAcknowledge(@RequestParam String connection_uuid) {
        if (sethlansServerDatabaseService.getByConnectionUUID(connection_uuid) != null) {
            SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(connection_uuid);
            LOG.debug("Received Server Acknowledgement for " + sethlansServer.getHostname());
            sethlansServer.setPendingAcknowledgementResponse(false);
            sethlansServer.setAcknowledged(true);
            sethlansServerDatabaseService.saveOrUpdate(sethlansServer);
        } else {
            LOG.debug("No such server present in database");
        }


    }

    @Autowired
    public void setSethlansAPIConnectionService(SethlansAPIConnectionService sethlansAPIConnectionService) {
        this.sethlansAPIConnectionService = sethlansAPIConnectionService;
    }

    @Autowired
    public void setAccessKeyDatabaseService(AccessKeyDatabaseService accessKeyDatabaseService) {
        this.accessKeyDatabaseService = accessKeyDatabaseService;
    }

    @Autowired
    public void setSethlansServerDatabaseService(SethlansServerDatabaseService sethlansServerDatabaseService) {
        this.sethlansServerDatabaseService = sethlansServerDatabaseService;
    }

    @Autowired
    public void setSethlansNotificationService(SethlansNotificationService sethlansNotificationService) {
        this.sethlansNotificationService = sethlansNotificationService;
    }
}
