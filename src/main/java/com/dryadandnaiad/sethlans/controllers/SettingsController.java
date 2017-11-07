/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC
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
import com.dryadandnaiad.sethlans.converters.SethlansNodeToNodeAddForm;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.enums.NodeAddProgress;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeService;
import com.dryadandnaiad.sethlans.services.network.NodeDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import java.util.List;

/**
 * Created Mario Estrella on 9/20/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Controller
public class SettingsController extends AbstractSethlansController {
    private static final Logger LOG = LoggerFactory.getLogger(SettingsController.class);

    private NodeDiscoveryService nodeDiscoveryService;
    private SethlansNodeService sethlansNodeService;
    private SethlansNode sethlansNode;
    private SethlansNodeToNodeAddForm sethlansNodeToNodeAddForm;

    @RequestMapping("/settings")
    public String getHomePage(Model model) {
        model.addAttribute("settings_option", "home");
        return "settings/settings";
    }

    @RequestMapping("/settings/users")
    public String getUserPage(Model model){
        model.addAttribute("settings_option", "users");
        return "settings/settings";
    }

    @RequestMapping("/settings/nodes")
    public String getNodePage(Model model){
        model.addAttribute("settings_option", "nodes");
        model.addAttribute("nodes", sethlansNodeService.listAll());
        return "settings/settings";
    }

    @RequestMapping("/settings/nodes/add")
    public String getNodeAddPage(Model model){
        sethlansNode = null;
        model.addAttribute("settings_option", "nodes_add");
        model.addAttribute("nodeAddForm",new NodeAddForm());
        return "settings/settings";
    }

    @RequestMapping("/settings/nodes/delete/{id}")
    public String deleteNode(@PathVariable Integer id, Model model){
        model.addAttribute("settings_option", "nodes");
        sethlansNodeService.delete(id);
        return "redirect:/settings/nodes/";
    }

    @RequestMapping("/settings/nodes/enable/{id}")
    public String enableNode(@PathVariable Integer id, Model model){
        model.addAttribute("settings_option", "nodes");
        SethlansNode sethlansNode = sethlansNodeService.getById(id);
        sethlansNode.setActive(true);
        LOG.debug(sethlansNode.toString());
        sethlansNodeService.saveOrUpdate(sethlansNode);
        return "redirect:/settings/nodes/";
    }


    @RequestMapping("/settings/nodes/update/{id}")
    public String updateNode(@PathVariable Integer id, Model model){
        model.addAttribute("settings_option", "nodes_update_nodeinfo");
        sethlansNode = sethlansNodeService.getById(id);
        NodeAddForm nodeUpdateForm = sethlansNodeToNodeAddForm.convert(sethlansNode);
        model.addAttribute("sethlansNode",sethlansNode);
        model.addAttribute("nodeUpdateForm", nodeUpdateForm);
        return "settings/settings";
    }

    @RequestMapping(value = "/settings/nodes/update-form/", method = RequestMethod.POST)
    public String updateNodeSubmit(final @Valid @ModelAttribute("nodeUpdateForm") NodeAddForm nodeUpdateForm, Model model){
        model.addAttribute("settings_option", "nodes_update_node_summary");

        if(nodeUpdateForm.getProgress() == NodeAddProgress.NODE_UPDATE_SUMMARY){
            sethlansNode = nodeDiscoveryService.discoverUnicastNode(nodeUpdateForm.getIpAddress(), nodeUpdateForm.getPort());
            model.addAttribute("sethlansNode",sethlansNode);
            model.addAttribute("settings_option", "nodes_update_node_summary");
            return "settings/settings";
        }

        if(nodeUpdateForm.getProgress() == NodeAddProgress.NODE_UPDATE_FINISHED){
            sethlansNode.setId(nodeUpdateForm.getId());
            sethlansNode.setVersion(nodeUpdateForm.getVersion());
            sethlansNodeService.saveOrUpdate(sethlansNode);
            LOG.debug("Updated: " +" ID:" + sethlansNode.getId() + ", Hostname: " + sethlansNode.getHostname() + " to database.");
            sethlansNode = null;
            return "redirect:/settings/nodes/";
        }
        return  "settings/settings";
    }

    @RequestMapping("/settings/nodes/disable/{id}")
    public String disableNode(@PathVariable Integer id, Model model){
        model.addAttribute("settings_option", "nodes");
        SethlansNode sethlansNode = sethlansNodeService.getById(id);
        sethlansNode.setActive(false);
        LOG.debug(sethlansNode.toString());
        sethlansNodeService.saveOrUpdate(sethlansNode);
        return "redirect:/settings/nodes/";
    }


    @RequestMapping(value = "/settings/nodes/add", method = RequestMethod.POST)
    public String nodeAddForm(final @Valid @ModelAttribute("nodeAddForm") NodeAddForm nodeAddForm, Model model){
        if(nodeAddForm.getProgress() == NodeAddProgress.NODE_INFO) {
            LOG.debug(nodeAddForm.toString());
            sethlansNode = nodeDiscoveryService.discoverUnicastNode(nodeAddForm.getIpAddress(), nodeAddForm.getPort());
            if(sethlansNode != null) {
                LOG.debug(sethlansNode.toString());
                model.addAttribute("settings_option", "nodes_add_nodeinfo");
                model.addAttribute("sethlansNode",sethlansNode);
            } else {
                nodeAddForm.setProgress(NodeAddProgress.IP_SETTINGS);
                LOG.debug(nodeAddForm.toString());
                model.addAttribute("settings_option", "nodes_add");
                return "settings/settings";
            }
        }
        if(nodeAddForm.getProgress() == NodeAddProgress.NODE_ADD) {
            sethlansNodeService.saveOrUpdate(sethlansNode);
            LOG.debug("Added: " + sethlansNode.getHostname() + " to database.");
            sethlansNode = null;
            return "redirect:/settings/nodes/";
        }

        return "settings/settings";
    }

    @RequestMapping("/settings/nodes/scan")
    public String getNodeScanPage(Model model){
        model.addAttribute("settings_option", "nodes_scan");
        nodeDiscoveryService.multicastDiscovery();
        if(!nodeDiscoveryService.isListComplete()){
            LOG.debug("Scanning");
            // return page
        }
        else {
            List<SethlansNode> sethlansNodes = nodeDiscoveryService.discoverMulticastNodes();
            if(sethlansNodes.size() == 0){
                LOG.debug("No Nodes found");
                // return page
            }
            else{
                LOG.debug(sethlansNodes.toString());
            }

        }
        return "settings/settings";
    }

    @Autowired
    public void setNodeDiscoveryService(NodeDiscoveryService nodeDiscoveryService) {
        this.nodeDiscoveryService = nodeDiscoveryService;
    }

    @Autowired
    public void setSethlansNodeService(SethlansNodeService sethlansNodeService) {
        this.sethlansNodeService = sethlansNodeService;
    }

    @Autowired
    public void setSethlansNodeToNodeAddForm(SethlansNodeToNodeAddForm sethlansNodeToNodeAddForm) {
        this.sethlansNodeToNodeAddForm = sethlansNodeToNodeAddForm;
    }
}
