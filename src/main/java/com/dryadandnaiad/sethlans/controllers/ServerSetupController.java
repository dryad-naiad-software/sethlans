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

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBinary;
import com.dryadandnaiad.sethlans.domains.database.events.SethlansNotification;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.enums.BlenderBinaryOS;
import com.dryadandnaiad.sethlans.enums.NotificationScope;
import com.dryadandnaiad.sethlans.enums.NotificationType;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.database.BlenderBinaryDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.network.NodeActivationService;
import com.dryadandnaiad.sethlans.services.network.NodeDiscoveryService;
import com.dryadandnaiad.sethlans.services.notification.SethlansNotificationService;
import com.dryadandnaiad.sethlans.services.queue.QueueService;
import com.dryadandnaiad.sethlans.utils.SethlansQueryUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.dryadandnaiad.sethlans.utils.SethlansConfigUtils.getProperty;

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
    private BlenderBinaryDatabaseService blenderBinaryDatabaseService;
    private SethlansNodeDatabaseService sethlansNodeDatabaseService;
    private SethlansNotificationService sethlansNotificationService;
    private QueueService queueService;

    private static final Logger LOG = LoggerFactory.getLogger(ServerSetupController.class);
    @Value("${sethlans.configDir}")
    private String configDir;


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
        String accessKey = getProperty(SethlansConfigKeys.ACCESS_KEY);
        SethlansNode sethlansNode = nodeDiscoveryService.discoverUnicastNode(ip, port);
        List<SethlansNode> sethlansNodeList = sethlansNodeDatabaseService.listAll();
        if (!sethlansNodeList.isEmpty()) {
            LOG.debug("Nodes found in database, starting comparison.");
            if (sethlansNodeDatabaseService.checkForDuplicatesAndSave(sethlansNode)) {
                LOG.debug("Added: " + sethlansNode.getHostname() + " to database.");
                nodeAddNotification(sethlansNode);
                if (sethlansNode.isPendingActivation()) {
                    nodeActivationService.sendActivationRequestToNode(sethlansNode, SethlansQueryUtils.getCurrentServerInfo(), accessKey);
                    return sethlansNode.getConnection_uuid();

                }
            }
        } else {
            LOG.debug("No nodes present in database.");
            sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
            LOG.debug("Added: " + sethlansNode.getHostname() + " to database.");
            nodeAddNotification(sethlansNode);
            if (sethlansNode.isPendingActivation()) {
                nodeActivationService.sendActivationRequestToNode(sethlansNode, SethlansQueryUtils.getCurrentServerInfo(), accessKey);
                return sethlansNode.getConnection_uuid();
            }
        }

        return null;
    }

    private void nodeAddNotification(SethlansNode sethlansNode) {
        String message = "Added node " + sethlansNode.getHostname();
        SethlansNotification sethlansNotification = new SethlansNotification(NotificationType.NODE, message, NotificationScope.ADMIN);
        sethlansNotification.setMessageLink("/admin/nodes");
        sethlansNotification.setLinkPresent(true);
        sethlansNotificationService.sendNotification(sethlansNotification);
    }

    @GetMapping("/node_delete/{id}")
    public boolean deleteNode(@PathVariable Long id) {
        queueService.addNodeToDeleteQueue(id);
        return true;
    }

    @PostMapping(value = "/add_blender_version")
    public boolean addNewBlenderVersion(@RequestParam String version) {
        List<SethlansNode> sethlansNodeList = sethlansNodeDatabaseService.listAll();
        Set<BlenderBinaryOS> blenderBinaryOSSet = new HashSet<>();
        if (sethlansNodeList.size() > 0) {
            for (SethlansNode sethlansNode : sethlansNodeList) {
                blenderBinaryOSSet.add(sethlansNode.getSethlansNodeOS());
            }
        } else {
            blenderBinaryOSSet.add(BlenderBinaryOS.valueOf(SethlansQueryUtils.getOS()));
        }

        for (BlenderBinaryOS blenderBinaryOS : blenderBinaryOSSet) {
            BlenderBinary blenderBinary = new BlenderBinary();
            blenderBinary.setBlenderVersion(version);
            blenderBinary.setBlenderBinaryOS(blenderBinaryOS.toString());
            blenderBinaryDatabaseService.saveOrUpdate(blenderBinary);
        }
        return true;
    }

    @GetMapping("/node_replace/{id}")
    public boolean updateNode(@PathVariable Long id) {
        String accessKey = getProperty(SethlansConfigKeys.ACCESS_KEY);
        SethlansNode sethlansNodeToReplace = sethlansNodeDatabaseService.getById(id);
        queueService.addNodeToDeleteQueue(id);
        SethlansNode newNode = nodeDiscoveryService.discoverUnicastNode(sethlansNodeToReplace.getIpAddress(), sethlansNodeToReplace.getNetworkPort());
        sethlansNodeDatabaseService.saveOrUpdate(newNode);
        nodeActivationService.sendActivationRequestToNode(newNode, SethlansQueryUtils.getCurrentServerInfo(), accessKey);
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

    @Autowired
    public void setBlenderBinaryDatabaseService(BlenderBinaryDatabaseService blenderBinaryDatabaseService) {
        this.blenderBinaryDatabaseService = blenderBinaryDatabaseService;
    }

    @Autowired
    public void setSethlansNotificationService(SethlansNotificationService sethlansNotificationService) {
        this.sethlansNotificationService = sethlansNotificationService;
    }
}
