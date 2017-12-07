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

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.services.database.SethlansServerService;
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
public class NodeActivationRequestController {
    private static final Logger LOG = LoggerFactory.getLogger(NodeActivationRequestController.class);
    private SethlansServerService sethlansServerService;

    @RequestMapping(value = "/api/nodeactivate/request", method = RequestMethod.POST)
    public void nodeActivationRequest(@RequestParam String serverhostname, @RequestParam String ipAddress, @RequestParam String port, @RequestParam String uuid) {
        SethlansServer sethlansServer = new SethlansServer();
        sethlansServer.setHostname(serverhostname);
        sethlansServer.setIpAddress(ipAddress);
        sethlansServer.setNetworkPort(port);
        sethlansServer.setAcknowledgeUUID(uuid);
        sethlansServerService.saveOrUpdate(sethlansServer);
        LOG.debug(sethlansServer.toString());
        LOG.debug("Received node activation request");
    }

    @Autowired
    public void setSethlansServerService(SethlansServerService sethlansServerService) {
        this.sethlansServerService = sethlansServerService;
    }
}
