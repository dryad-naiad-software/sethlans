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

import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.services.database.SethlansServerService;
import com.dryadandnaiad.sethlans.services.network.NodeActivationService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created Mario Estrella on 12/6/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Controller
@Profile({"NODE", "DUAL"})
public class NodeSettingsController extends AbstractSethlansController {
    private static final Logger LOG = LoggerFactory.getLogger(NodeSettingsController.class);

    @Value("${server.port}")
    private String sethlansPort;

    private SethlansServerService sethlansServerService;
    private NodeActivationService nodeActivationService;
    private SethlansNode sethlansNode;

    @RequestMapping("/settings/servers")
    public String getServersPage(Model model) {
        model.addAttribute("settings_option", "servers");
        model.addAttribute("servers", sethlansServerService.listAll());
        return "settings/settings";
    }

    @RequestMapping("/settings/servers/delete/{id}")
    public String deleteNode(@PathVariable Integer id, Model model) {
        model.addAttribute("settings_option", "servers");
        sethlansServerService.delete(id);
        return "redirect:/settings/servers/";
    }

    @RequestMapping("/settings/servers/acknowledge/{id}")
    public String enableNode(@PathVariable Integer id, Model model) {
        model.addAttribute("settings_option", "server");
        SethlansServer sethlansServer = sethlansServerService.getById(id);
        sethlansServer.setPendingAcknowledgementResponse(true);
        LOG.debug(sethlansServer.toString());
        sethlansServerService.saveOrUpdate(sethlansServer);
        setSethlansNode();
        nodeActivationService.sendActivationResponse(sethlansServer, sethlansNode);
        return "redirect:/settings/servers/";
    }

    @Autowired
    public void setSethlansServerService(SethlansServerService sethlansServerService) {
        this.sethlansServerService = sethlansServerService;
    }

    @Autowired
    public void setNodeActivationService(NodeActivationService nodeActivationService) {
        this.nodeActivationService = nodeActivationService;
    }

    public void setSethlansNode() {
        sethlansNode = new SethlansNode();
        sethlansNode.setHostname(SethlansUtils.getHostname());
        sethlansNode.setNetworkPort(sethlansPort);
        sethlansNode.setIpAddress(SethlansUtils.getIP());
    }
}
