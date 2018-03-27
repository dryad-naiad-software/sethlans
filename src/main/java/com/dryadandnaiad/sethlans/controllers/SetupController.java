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

import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.domains.database.user.SethlansUser;
import com.dryadandnaiad.sethlans.forms.setup.SetupForm;
import com.dryadandnaiad.sethlans.forms.setup.subclasses.SetupNode;
import com.dryadandnaiad.sethlans.services.config.SaveSetupConfigService;
import com.dryadandnaiad.sethlans.services.config.UpdateComputeService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansUserDatabaseService;
import com.dryadandnaiad.sethlans.services.network.NodeActivationService;
import com.dryadandnaiad.sethlans.services.network.NodeDiscoveryService;
import com.dryadandnaiad.sethlans.services.system.SethlansManagerService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created Mario Estrella on 2/11/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@RequestMapping("/api/setup")
public class SetupController {
    private static final Logger LOG = LoggerFactory.getLogger(SetupController.class);
    private SaveSetupConfigService saveSetupConfigService;
    private SethlansUserDatabaseService sethlansUserDatabaseService;
    private UpdateComputeService updateComputeService;
    private SethlansManagerService sethlansManagerService;
    private NodeDiscoveryService nodeDiscoveryService;
    private SethlansNodeDatabaseService sethlansNodeDatabaseService;
    private NodeActivationService nodeActivationService;
    private SethlansServerDatabaseService sethlansServerDatabaseService;


    @PostMapping("/submit")
    public boolean submit(@RequestBody SetupForm setupForm) {
        LOG.debug("Submitting Setup Form...");
        if (setupForm != null) {
            LOG.debug(setupForm.toString());
            saveSetupConfigService.saveSetupSettings(setupForm);
            return true;
        } else {
            return false;
        }
    }

    @PostMapping("/update_compute")
    public boolean submit(@RequestBody SetupNode setupNode) {
        LOG.debug("Processing Compute Setting Update");
        if (setupNode != null) {
            LOG.debug(setupNode.toString());
            boolean updateComplete = updateComputeService.saveComputeSettings(setupNode);
            sethlansManagerService.restart();
            return updateComplete;
        } else {
            return false;
        }
    }

    @GetMapping("/server_acknowledge/{id}")
    public boolean acknowledgeNode(@PathVariable Long id) {
        SethlansServer sethlansServer = sethlansServerDatabaseService.getById(id);
        sethlansServer.setPendingAcknowledgementResponse(true);
        LOG.debug(sethlansServer.toString());
        sethlansServerDatabaseService.saveOrUpdate(sethlansServer);
        nodeActivationService.sendActivationResponse(sethlansServer, SethlansUtils.getCurrentNodeInfo());
        return true;
    }

    @PostMapping("/multi_node_add")
    public boolean addMultiNodes(@RequestBody String[] nodeIPArray) {
        for (String node : nodeIPArray) {
            String[] nodeInfo = StringUtils.split(node, ",");
            addNode(nodeInfo[0], nodeInfo[1]);
        }
        return true;
    }

    @GetMapping("/node_add")
    public boolean addNode(@RequestParam String ip, @RequestParam String port) {
        SethlansNode sethlansNode = nodeDiscoveryService.discoverUnicastNode(ip, port);
        List<SethlansNode> sethlansNodeList = sethlansNodeDatabaseService.listAll();
        if (!sethlansNodeList.isEmpty()) {
            LOG.debug("Nodes found in database, starting comparison.");
            if (sethlansNodeDatabaseService.checkForDuplicatesAndSave(sethlansNode)) {
                if (sethlansNode.isPendingActivation()) {
                    nodeActivationService.sendActivationRequest(sethlansNode, SethlansUtils.getCurrentServerInfo());
                    return true;

                }
            }
        } else {
            LOG.debug("No nodes present in database.");
            sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
            LOG.debug("Added: " + sethlansNode.getHostname() + " to database.");
            if (sethlansNode.isPendingActivation()) {
                nodeActivationService.sendActivationRequest(sethlansNode, SethlansUtils.getCurrentServerInfo());
                return true;
            }
        }

        return false;
    }

    @GetMapping("/node_delete/{id}")
    public boolean deleteNode(@PathVariable Long id) {
        sethlansNodeDatabaseService.delete(id);
        return true;
    }

    @GetMapping("/node_edit/{id}")
    public boolean updateNode(@PathVariable Long id, @RequestParam String ip, @RequestParam String port) {
        SethlansNode sethlansNodeToEdit = sethlansNodeDatabaseService.getById(id);
        SethlansNode newNode = nodeDiscoveryService.discoverUnicastNode(ip, port);
        newNode.setId(sethlansNodeToEdit.getId());
        newNode.setVersion(sethlansNodeToEdit.getVersion());
        newNode.setDateCreated(sethlansNodeToEdit.getDateCreated());
        newNode.setLastUpdated(sethlansNodeToEdit.getLastUpdated());
        sethlansNodeDatabaseService.saveOrUpdate(newNode);
        return true;
    }

    @GetMapping("/server_delete/{id}")
    public boolean deleteServer(@PathVariable Long id) {
        sethlansServerDatabaseService.delete(id);
        return true;
    }

    @PostMapping("/register")
    public boolean register(@RequestBody SethlansUser user) {
        if (user != null) {
            LOG.debug("Registering new user...");
            if (sethlansUserDatabaseService.checkifExists(user.getUsername())) {
                LOG.debug("User " + user.getUsername() + " already exists!");
                return false;
            }
            sethlansUserDatabaseService.saveOrUpdate(user);
            LOG.debug("Saving " + user.toString() + " to database.");
            return true;
        } else {
            return false;
        }
    }

    @Autowired
    public void setSaveSetupConfigService(SaveSetupConfigService saveSetupConfigService) {
        this.saveSetupConfigService = saveSetupConfigService;
    }

    @Autowired
    public void setSethlansUserDatabaseService(SethlansUserDatabaseService sethlansUserDatabaseService) {
        this.sethlansUserDatabaseService = sethlansUserDatabaseService;
    }

    @Autowired
    public void setUpdateComputeService(UpdateComputeService updateComputeService) {
        this.updateComputeService = updateComputeService;
    }

    @Autowired
    public void setSethlansManagerService(SethlansManagerService sethlansManagerService) {
        this.sethlansManagerService = sethlansManagerService;
    }

    @Autowired
    public void setNodeDiscoveryService(NodeDiscoveryService nodeDiscoveryService) {
        this.nodeDiscoveryService = nodeDiscoveryService;
    }

    @Autowired
    public void setSethlansNodeDatabaseService(SethlansNodeDatabaseService sethlansNodeDatabaseService) {
        this.sethlansNodeDatabaseService = sethlansNodeDatabaseService;
    }

    @Autowired
    public void setNodeActivationService(NodeActivationService nodeActivationService) {
        this.nodeActivationService = nodeActivationService;
    }

    @Autowired
    public void setSethlansServerDatabaseService(SethlansServerDatabaseService sethlansServerDatabaseService) {
        this.sethlansServerDatabaseService = sethlansServerDatabaseService;
    }
}
