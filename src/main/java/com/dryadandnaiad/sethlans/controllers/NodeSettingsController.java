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
import com.dryadandnaiad.sethlans.domains.hardware.CPU;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.osnative.hardware.gpu.GPU;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.network.NodeActivationService;
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

import java.util.ArrayList;
import java.util.List;

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

    @Value("${sethlans.cores}")
    private int currentCores;

    @Value("${sethlans.computeMethod}")
    private ComputeType currentCompute;

    private SethlansServerDatabaseService sethlansServerDatabaseService;
    private NodeActivationService nodeActivationService;
    private SethlansNode sethlansNode;

    @RequestMapping("/settings/servers")
    public String getServersPage(Model model) {
        model.addAttribute("settings_option", "servers");
        model.addAttribute("servers", sethlansServerDatabaseService.listAll());
        return "settings/settings";
    }

    @RequestMapping(value = "/settings/compute_method", method = RequestMethod.GET)
    public String getComputeMethodPage(Model model) {
        List<GPUDevice> availableGPUs = GPU.listDevices();
        List<Integer> selectedGPUId = new ArrayList<>();
        model.addAttribute("settings_option", "compute");
        model.addAttribute("current_compute", currentCompute);
        model.addAttribute("current_cores", currentCores);
        model.addAttribute("available_gpus", availableGPUs);
        model.addAttribute("selected_gpus", selectedGPUId);
        return "settings/settings";
    }

    @RequestMapping(value = "/settings/compute_method", method = RequestMethod.POST)
    public String processComputeMethod(Model model) {
        model.addAttribute("settings_option", "compute");
        model.addAttribute("current_compute", currentCompute);
        return "settings/settings";
    }

    @RequestMapping("/settings/servers/delete/{id}")
    public String deleteNode(@PathVariable Integer id, Model model) {
        model.addAttribute("settings_option", "servers");
        sethlansServerDatabaseService.delete(id);
        return "redirect:/settings/servers/";
    }

    @RequestMapping("/settings/servers/acknowledge/{id}")
    public String enableNode(@PathVariable Integer id, Model model) {
        model.addAttribute("settings_option", "server");
        SethlansServer sethlansServer = sethlansServerDatabaseService.getById(id);
        sethlansServer.setPendingAcknowledgementResponse(true);
        LOG.debug(sethlansServer.toString());
        sethlansServerDatabaseService.saveOrUpdate(sethlansServer);
        setSethlansNode();
        nodeActivationService.sendActivationResponse(sethlansServer, sethlansNode);
        return "redirect:/settings/servers/";
    }

    @ModelAttribute("total_cores")
    public int getTotalCPUCores() {
        CPU cpu = new CPU();
        return cpu.getCores();
    }

    @ModelAttribute("available_methods")
    public List<ComputeType> getAvailableMethods() {
        return SethlansUtils.getAvailableMethods();

    }

    @Autowired
    public void setSethlansServerDatabaseService(SethlansServerDatabaseService sethlansServerDatabaseService) {
        this.sethlansServerDatabaseService = sethlansServerDatabaseService;
    }

    @Autowired
    public void setNodeActivationService(NodeActivationService nodeActivationService) {
        this.nodeActivationService = nodeActivationService;
    }

    private void setSethlansNode() {
        sethlansNode = new SethlansNode();
        sethlansNode.setHostname(SethlansUtils.getHostname());
        sethlansNode.setNetworkPort(sethlansPort);
        sethlansNode.setIpAddress(SethlansUtils.getIP());
    }
}
