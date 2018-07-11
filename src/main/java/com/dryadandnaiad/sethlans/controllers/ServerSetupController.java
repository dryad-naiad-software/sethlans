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
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.network.NodeActivationService;
import com.dryadandnaiad.sethlans.services.network.NodeDiscoveryService;
import com.dryadandnaiad.sethlans.services.queue.QueueService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 4/2/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: Sethlans
 */
@RestController
@Profile({"SERVER", "DUAL"})
@RequestMapping("/api/setup")
public class ServerSetupController {
    private NodeActivationService nodeActivationService;
    private NodeDiscoveryService nodeDiscoveryService;
    private SethlansNodeDatabaseService sethlansNodeDatabaseService;
    private QueueService queueService;
    private static final Logger LOG = LoggerFactory.getLogger(ServerSetupController.class);
    @Value("${sethlans.configDir}")
    private String configDir;


    @GetMapping("/auto_acknowledge/{connection_id}")
    public void autoAcknowledgeNode(@PathVariable String connection_id) {
        SethlansServer sethlansServer = SethlansUtils.getCurrentServerInfo(new File(configDir + SethlansUtils.CONFIG_FILENAME));
        sethlansServer.setConnection_uuid(connection_id);
        nodeActivationService.sendActivationResponse(sethlansServer, sethlansNodeDatabaseService.getByConnectionUUID(connection_id), true);
    }

    @PostMapping("/multi_auto_acknowledge")
    public void multiAutoAcknowledge(@RequestBody String[] connectionIdArray) {
        for (String connectionId : connectionIdArray) {
            autoAcknowledgeNode(connectionId);
        }
    }


    @PostMapping("/multi_node_add")
    public List<String> addMultiNodes(@RequestBody String[] nodeIPArray) {
        List<String> connectionIds = new ArrayList<>();
        for (String node : nodeIPArray) {
            String[] nodeInfo = StringUtils.split(node, ",");
            connectionIds.add(addNode(nodeInfo[0], nodeInfo[1]));
        }
        return connectionIds;
    }

    @GetMapping("/node_add")
    public String addNode(@RequestParam String ip, @RequestParam String port) {
        SethlansNode sethlansNode = nodeDiscoveryService.discoverUnicastNode(ip, port);
        List<SethlansNode> sethlansNodeList = sethlansNodeDatabaseService.listAll();
        if (!sethlansNodeList.isEmpty()) {
            LOG.debug("Nodes found in database, starting comparison.");
            if (sethlansNodeDatabaseService.checkForDuplicatesAndSave(sethlansNode)) {
                if (sethlansNode.isPendingActivation()) {
                    nodeActivationService.sendActivationRequest(sethlansNode, SethlansUtils.getCurrentServerInfo(new File(configDir + SethlansUtils.CONFIG_FILENAME)), true);
                    return sethlansNode.getConnection_uuid();

                }
            }
        } else {
            LOG.debug("No nodes present in database.");
            sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
            LOG.debug("Added: " + sethlansNode.getHostname() + " to database.");
            if (sethlansNode.isPendingActivation()) {
                nodeActivationService.sendActivationRequest(sethlansNode, SethlansUtils.getCurrentServerInfo(new File(configDir + SethlansUtils.CONFIG_FILENAME)), true);
                return sethlansNode.getConnection_uuid();
            }
        }

        return null;
    }

    @GetMapping("/node_delete/{id}")
    public boolean deleteNode(@PathVariable Long id) {
        queueService.addNodeToDeleteQueue(id);
        return true;
    }

    @GetMapping("/node_replace/{id}")
    public boolean updateNode(@PathVariable Long id) {
        SethlansNode sethlansNodeToReplace = sethlansNodeDatabaseService.getById(id);
        queueService.addNodeToDeleteQueue(id);
        SethlansNode newNode = nodeDiscoveryService.discoverUnicastNode(sethlansNodeToReplace.getIpAddress(), sethlansNodeToReplace.getNetworkPort());
        sethlansNodeDatabaseService.saveOrUpdate(newNode);
        nodeActivationService.sendActivationRequest(newNode, SethlansUtils.getCurrentServerInfo(new File(configDir + SethlansUtils.CONFIG_FILENAME)), true);
        autoAcknowledgeNode(newNode.getConnection_uuid());
        return true;
    }

    @GetMapping("/node_disable/{id}")
    public boolean disableNode(@PathVariable Long id) {
        queueService.addNodeToDisable(id);
        return true;
    }

    @GetMapping("/node_enable/{id}")
    public boolean enableNode(@PathVariable Long id) {
        SethlansNode sethlansNode = sethlansNodeDatabaseService.getById(id);
        sethlansNode.setDisabled(false);
        sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
        return true;
    }

    @Autowired
    public void setNodeActivationService(NodeActivationService nodeActivationService) {
        this.nodeActivationService = nodeActivationService;
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
    public void setQueueService(QueueService queueService) {
        this.queueService = queueService;
    }
}
