package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 4/2/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: Sethlans
 */

@RestController
@Profile({"SERVER", "DUAL", "SETUP"})
@RequestMapping("/api/info")
public class ServerInfoController {
    private SethlansNodeDatabaseService sethlansNodeDatabaseService;


    @GetMapping(value = {"/total_nodes"})
    public int getTotalNodes() {
        return sethlansNodeDatabaseService.listAll().size();
    }

    @GetMapping(value = {"/inactive_nodes"})
    public int getInactiveNodes() {
        return sethlansNodeDatabaseService.inactiveNodeList().size();
    }

    @GetMapping(value = {"/disabled_nodes"})
    public int getDisabledNodes() {
        return sethlansNodeDatabaseService.disabledNodeList().size();
    }

    @GetMapping(value = {"/active_nodes"})
    public int getActiveNodes() {
        return sethlansNodeDatabaseService.activeNodeList().size();
    }

    @GetMapping(value = {"/active_nodes_cpu"})
    public int getActiveCPUNodes() {
        return sethlansNodeDatabaseService.activeCPUNodes().size();
    }

    @GetMapping(value = {"/active_nodes_gpu"})
    public int getActiveGPUNodes() {
        return sethlansNodeDatabaseService.activeGPUNodes().size();
    }

    @GetMapping(value = {"/active_nodes_cpu_gpu"})
    public int getActiveCPUGPUNodes() {
        return sethlansNodeDatabaseService.activeCPUGPUNodes().size();
    }

    @GetMapping(value = {"/active_nodes_value_array"})
    public List<Integer> getNumberOfActiveNodesArray() {
        List<Integer> numberOfActiveNodesArray = new ArrayList<>();
        numberOfActiveNodesArray.add(getActiveCPUNodes());
        numberOfActiveNodesArray.add(getActiveGPUNodes());
        numberOfActiveNodesArray.add(getActiveCPUGPUNodes());
        return numberOfActiveNodesArray;
    }


    @Autowired
    public void setSethlansNodeDatabaseService(SethlansNodeDatabaseService sethlansNodeDatabaseService) {
        this.sethlansNodeDatabaseService = sethlansNodeDatabaseService;
    }
}
