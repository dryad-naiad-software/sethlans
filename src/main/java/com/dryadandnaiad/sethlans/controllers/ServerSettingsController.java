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

import com.dryadandnaiad.sethlans.commands.NodeAddForm;
import com.dryadandnaiad.sethlans.commands.ScanForm;
import com.dryadandnaiad.sethlans.converters.SethlansNodeToNodeAddForm;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.enums.NodeAddProgress;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import com.dryadandnaiad.sethlans.services.network.NodeActivationService;
import com.dryadandnaiad.sethlans.services.network.NodeDiscoveryService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created Mario Estrella on 9/20/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Controller
@Profile({"SERVER", "DUAL"})
public class ServerSettingsController extends AbstractSethlansController {
    private static final Logger LOG = LoggerFactory.getLogger(ServerSettingsController.class);

    private NodeDiscoveryService nodeDiscoveryService;
    private SethlansNodeDatabaseService sethlansNodeDatabaseService;
    private SethlansNodeToNodeAddForm sethlansNodeToNodeAddForm;
    private NodeActivationService nodeActivationService;
    private SethlansServer sethlansServer;
    private SethlansAPIConnectionService sethlansAPIConnectionService;
    private SethlansNode sethlansNodeToAdd;

    @Value("${server.port}")
    private String sethlansPort;

    @RequestMapping("/settings/nodes")
    public String getNodePage(Model model) {
        List<SethlansNode> sethlansNodeList = sethlansNodeDatabaseService.listAll();
        model.addAttribute("settings_option", "nodes");
        model.addAttribute("nodes", sethlansNodeList);
        nodeDiscoveryService.resetNodeList();
        Thread backgroundNodeRefresh = new Thread(() -> {
            for (SethlansNode node : sethlansNodeList) {
                LOG.debug("Initiating background refresh of node list");
                String url = "https://" + SethlansUtils.getIP() + ":" + sethlansPort + "/api/update/node_status_update";
                String param = "/?connection_uuid=" + node.getConnection_uuid();
                sethlansAPIConnectionService.sendToRemoteGET(url, param);
            }
        });
        backgroundNodeRefresh.start();

        return "settings/settings";
    }


    @RequestMapping("/settings/nodes/add")
    public String getNodeAddPage(Model model) {
        sethlansNodeToAdd = null;
        model.addAttribute("settings_option", "nodes_add");
        model.addAttribute("nodeAddForm", new NodeAddForm());
        return "settings/settings";
    }

    @RequestMapping("/settings/nodes/delete/{id}")
    public String deleteNode(@PathVariable Integer id, Model model) {
        model.addAttribute("settings_option", "nodes");
        sethlansNodeDatabaseService.delete(id);
        return "redirect:/settings/nodes/";
    }

    @RequestMapping("/settings/nodes/enable/{id}")
    public String enableNode(@PathVariable Integer id, Model model) {
        model.addAttribute("settings_option", "nodes");
        SethlansNode sethlansNode = sethlansNodeDatabaseService.getById(id);
        sethlansNode.setActive(true);
        LOG.debug(sethlansNode.toString());
        sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
        return "redirect:/settings/nodes/";
    }


    @RequestMapping("/settings/nodes/update/{id}")
    public String updateNode(@PathVariable Integer id, Model model) {
        model.addAttribute("settings_option", "nodes_update_nodeinfo");
        SethlansNode sethlansNode = sethlansNodeDatabaseService.getById(id);
        NodeAddForm nodeUpdateForm = sethlansNodeToNodeAddForm.convert(sethlansNode);
        model.addAttribute("sethlansNode", sethlansNode);
        model.addAttribute("nodeUpdateForm", nodeUpdateForm);
        return "settings/settings";
    }

    @RequestMapping(value = "/settings/nodes/update-form/", method = RequestMethod.POST)
    public String updateNodeSubmit(final @Valid @ModelAttribute("nodeUpdateForm") NodeAddForm nodeUpdateForm, @ModelAttribute("sethlansNode") SethlansNode sethlansNode, Model model) {
        model.addAttribute("settings_option", "nodes_update_node_summary");

        if (nodeUpdateForm.getProgress() == NodeAddProgress.NODE_UPDATE_SUMMARY) {
            sethlansNode = nodeDiscoveryService.discoverUnicastNode(nodeUpdateForm.getIpAddress(), nodeUpdateForm.getPort());
            model.addAttribute("sethlansNode", sethlansNode);
            model.addAttribute("settings_option", "nodes_update_node_summary");
            return "settings/settings";
        }

        if (nodeUpdateForm.getProgress() == NodeAddProgress.NODE_UPDATE_FINISHED) {
            sethlansNode.setId(nodeUpdateForm.getId());
            sethlansNode.setVersion(nodeUpdateForm.getVersion());
            sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
            LOG.debug("Updated: " + " ID:" + sethlansNode.getId() + ", Hostname: " + sethlansNode.getHostname() + " to database.");
            sethlansNode = null;
            return "redirect:/settings/nodes/";
        }
        return "settings/settings";
    }

    @RequestMapping("/settings/nodes/disable/{id}")
    public String disableNode(@PathVariable Integer id, Model model) {
        model.addAttribute("settings_option", "nodes");
        SethlansNode sethlansNode = sethlansNodeDatabaseService.getById(id);
        sethlansNode.setActive(false);
        LOG.debug(sethlansNode.toString());
        sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
        return "redirect:/settings/nodes/";
    }


    @RequestMapping(value = "/settings/nodes/add", method = RequestMethod.POST)
    public String nodeAddForm(final @Valid @ModelAttribute("nodeAddForm") NodeAddForm nodeAddForm, Model model) {
        if (nodeAddForm.getProgress() == NodeAddProgress.NODE_INFO) {
            LOG.debug(nodeAddForm.toString());
            sethlansNodeToAdd = nodeDiscoveryService.discoverUnicastNode(nodeAddForm.getIpAddress(), nodeAddForm.getPort());
            if (sethlansNodeToAdd != null) {
                LOG.debug(sethlansNodeToAdd.toString());
                model.addAttribute("settings_option", "nodes_add_nodeinfo");
                model.addAttribute("sethlansNode", sethlansNodeToAdd);
            } else {
                nodeAddForm.setProgress(NodeAddProgress.IP_SETTINGS);
                LOG.debug(nodeAddForm.toString());
                model.addAttribute("settings_option", "nodes_add");
                return "settings/settings";
            }
        }
        if (nodeAddForm.getProgress() == NodeAddProgress.NODE_ADD) {
            Set<SethlansNode> sethlansNodes = new HashSet<>();
            List<SethlansNode> sethlansNodesDatabase = sethlansNodeDatabaseService.listAll();
            if (!sethlansNodesDatabase.isEmpty()) {
                if (sethlansNodeDatabaseService.isNodeAlreadyInDatabase(sethlansNodeToAdd)) {
                    LOG.debug(sethlansNodeToAdd.getHostname() + " is already in the database.");
                } else {
                    sethlansNodes.add(sethlansNodeToAdd);
                }
                for (SethlansNode node : sethlansNodes) {
                    sethlansNodeDatabaseService.saveOrUpdate(node);
                    LOG.debug("Added: " + node.getHostname() + " to database.");
                    if (node.isPendingActivation()) {
                        setSethlansServer();
                        nodeActivationService.sendActivationRequest(node, sethlansServer);
                    }
                }

            } else {
                LOG.debug(sethlansNodeToAdd.toString());
                sethlansNodeDatabaseService.saveOrUpdate(sethlansNodeToAdd);
                LOG.debug("Added: " + sethlansNodeToAdd.getHostname() + " to database.");
                if (sethlansNodeToAdd.isPendingActivation()) {
                    setSethlansServer();
                    nodeActivationService.sendActivationRequest(sethlansNodeToAdd, sethlansServer);
                }
            }


            return "redirect:/settings/nodes/";
        }

        return "settings/settings";
    }

    @RequestMapping("/settings/nodes/scan")
    public String getNodeScanPage(Model model) {
        model.addAttribute("settings_option", "nodes_scan");
        nodeDiscoveryService.multicastDiscovery();
        LOG.debug("Scanning");
        return "settings/settings";
    }

    @RequestMapping("/settings/nodes/scan/summary")
    public String getNodeScanSummaryPage(Model model) {

        List<SethlansNode> sethlansNodes = nodeDiscoveryService.discoverMulticastNodes();
        if (sethlansNodes == null || sethlansNodes.size() == 0) {
            LOG.debug("No Nodes found");
            model.addAttribute("settings_option", "nodes_scan_empty");
            nodeDiscoveryService.resetNodeList();

        } else {
            LOG.debug(sethlansNodes.toString());
            model.addAttribute("settings_option", "nodes_scan_summary");
            model.addAttribute("scanForm", new ScanForm());
            model.addAttribute("sethlansNodes", sethlansNodes);
        }

        return "settings/settings";
    }

    @RequestMapping(value = "/settings/nodes/scan/summary", method = RequestMethod.POST)
    public String submitNodefromScan(final @Valid @ModelAttribute("scanForm") ScanForm scanForm) {
        List<SethlansNode> sethlansNodes = nodeDiscoveryService.discoverMulticastNodes();
        LOG.debug("Selected Nodes: " + sethlansNodes.toString());
        List<SethlansNode> sethlansNodesDatabase = sethlansNodeDatabaseService.listAll();
        for (Integer nodeId : scanForm.getSethlansNodeId()) {
            if (!sethlansNodesDatabase.isEmpty()) {
                for (SethlansNode node : sethlansNodesDatabase) {
                    if (sethlansNodeDatabaseService.isNodeAlreadyInDatabase(sethlansNodeToAdd)) {
                        LOG.debug(node.getHostname() + " is already in the database.");
                    } else {
                        sethlansNodeDatabaseService.saveOrUpdate(sethlansNodes.get(nodeId));
                        LOG.debug("Added: " + sethlansNodes.get(nodeId).getHostname() + " to database.");
                        if (sethlansNodes.get(nodeId).isPendingActivation()) {
                            setSethlansServer();
                            nodeActivationService.sendActivationRequest(sethlansNodes.get(nodeId), sethlansServer);
                        }

                    }
                }
            } else {
                sethlansNodeDatabaseService.saveOrUpdate(sethlansNodes.get(nodeId));
                LOG.debug("Added: " + sethlansNodes.get(nodeId).getHostname() + " to database.");
                if (sethlansNodes.get(nodeId).isPendingActivation()) {
                    setSethlansServer();
                    nodeActivationService.sendActivationRequest(sethlansNodes.get(nodeId), sethlansServer);
                }
            }
        }
        nodeDiscoveryService.resetNodeList();
        return "redirect:/settings/nodes/";
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
    public void setSethlansNodeToNodeAddForm(SethlansNodeToNodeAddForm sethlansNodeToNodeAddForm) {
        this.sethlansNodeToNodeAddForm = sethlansNodeToNodeAddForm;
    }

    @Autowired
    public void setSethlansAPIConnectionService(SethlansAPIConnectionService sethlansAPIConnectionService) {
        this.sethlansAPIConnectionService = sethlansAPIConnectionService;
    }

    private void setSethlansServer() {
        this.sethlansServer = new SethlansServer();
        this.sethlansServer.setNetworkPort(sethlansPort);
        this.sethlansServer.setHostname(SethlansUtils.getHostname());
        this.sethlansServer.setIpAddress(SethlansUtils.getIP());


    }
}
