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

import com.dryadandnaiad.sethlans.commands.ComputeForm;
import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.domains.hardware.CPU;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.osnative.hardware.gpu.GPU;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.network.NodeActivationService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.dryadandnaiad.sethlans.utils.SethlansUtils.writeProperty;

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
    private String cores;

    @Value("${sethlans.cuda}")
    private String cuda;

    private List<String> cudaList;

    @Value("${sethlans.computeMethod}")
    private ComputeType currentCompute;

    private SethlansServerDatabaseService sethlansServerDatabaseService;
    private NodeActivationService nodeActivationService;
    private SethlansNode sethlansNode;
    private Validator computeFormValidator;

    @RequestMapping("/settings/servers")
    public String getServersPage(Model model) {
        model.addAttribute("settings_option", "servers");
        model.addAttribute("servers", sethlansServerDatabaseService.listAll());
        return "settings/settings";
    }

    @RequestMapping(value = "/settings/compute_method", method = RequestMethod.GET)
    public String getComputeMethodPage(Model model) {
        setCudaList();
        List<GPUDevice> availableGPUs = GPU.listDevices();
        ComputeForm computeForm = new ComputeForm();
        setComputeMethodItems(model, availableGPUs, computeForm);
        return "settings/settings";
    }

    private void setComputeMethodItems(Model model, List<GPUDevice> availableGPUs, ComputeForm computeForm) {
        Integer currentCores;
        currentCores = Integer.parseInt(cores);
        List<Integer> selectedGPUs = new ArrayList<>();
        List<String> selectedModels = new ArrayList<>();
        if (cudaList.size() > 0) {
            for (String cuda : cudaList) {
                Integer idNumber = Integer.parseInt(StringUtils.substringAfter(cuda, "_"));
                selectedGPUs.add(idNumber);
                selectedModels.add(availableGPUs.get(idNumber).getModel());
            }
        }
        computeForm.setSelectedGPUIds(selectedGPUs);
        computeForm.setSelectedCompute(currentCompute);
        if (currentCores != 0) {
            computeForm.setSelectedCores(currentCores);
        }
        LOG.debug(computeForm.toString());
        model.addAttribute("settings_option", "compute");
        model.addAttribute("compute_form", computeForm);
        model.addAttribute("current_cores", currentCores);
        model.addAttribute("current_compute", currentCompute);
        model.addAttribute("available_gpus", availableGPUs);
        model.addAttribute("selected_models", selectedModels);
    }

    @RequestMapping(value = "/settings/compute_method", method = RequestMethod.POST)
    public String processComputeMethod(final @Valid @ModelAttribute("compute_form") ComputeForm computeForm, Model model, BindingResult bindingResult) {
        setCudaList();
        computeFormValidator.validate(computeForm, bindingResult);
        List<GPUDevice> availableGPUs = GPU.listDevices();
        if (bindingResult.hasErrors()) {
            LOG.debug("Errors found");
            setComputeMethodItems(model, availableGPUs, computeForm);
            return "settings/settings";
        }
        LOG.debug("Saving compute form to config file " + computeForm.toString());
        writeProperty(SethlansConfigKeys.COMPUTE_METHOD, computeForm.getSelectedCompute().toString());
        if (!computeForm.getSelectedCompute().equals(ComputeType.GPU)) {
            writeProperty(SethlansConfigKeys.CPU_CORES, computeForm.getSelectedCores().toString());
        }
        if (!computeForm.getSelectedCompute().equals(ComputeType.CPU)) {
            if (!computeForm.getSelectedGPUIds().isEmpty()) {
                StringBuilder result = new StringBuilder();
                for (Integer id : computeForm.getSelectedGPUIds()) {
                    if (result.length() != 0) {
                        result.append(",");
                    }
                    result.append(availableGPUs.get(id).getCudaName());
                }
                writeProperty(SethlansConfigKeys.CUDA_DEVICE, result.toString());
            }
        }
        return "redirect:/restart";
    }

    @RequestMapping("/settings/servers/delete/{id}")
    public String deleteNode(@PathVariable Integer id, Model model) {
        model.addAttribute("settings_option", "servers");
        SethlansServer sethlansServer = sethlansServerDatabaseService.getById(id);
//        notificationDatabaseService.
//        sethlansServerDatabaseService.delete(id);
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

    private void setCudaList() {
        if (cuda.equals("") || cuda.equals("null")) {
            cudaList = new ArrayList<>();
        } else {
            cudaList = Arrays.asList(cuda.split(","));
        }

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

    @Autowired
    @Qualifier("computeFormValidator")
    public void setComputeFormValidator(Validator computeFormValidator) {
        this.computeFormValidator = computeFormValidator;
    }
}
