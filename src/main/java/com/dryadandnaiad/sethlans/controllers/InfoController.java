/*
 * Copyright (c) 2020. Dryad and Naiad Software LLC.
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.enums.NodeType;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.models.hardware.GPU;
import com.dryadandnaiad.sethlans.models.query.NodeDashboard;
import com.dryadandnaiad.sethlans.models.system.Node;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * File created by Mario Estrella on 6/11/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/info")
public class InfoController {

    @GetMapping("/mode")
    public Map<String, SethlansMode> getMode() {
        var mode = new HashMap<String, SethlansMode>();
        mode.put("mode", PropertiesUtils.getMode());
        return mode;
    }

    @GetMapping("/node_dashboard")
    @Profile({"NODE", "DUAL"})
    public NodeDashboard getNodeDashboard() {
        var systemInfo = QueryUtils.getCurrentSystemInfo();
        var apiPresent = false;
        var apiKey = ConfigUtils.getProperty(ConfigKeys.SETHLANS_API_KEY);
        if (!apiKey.equals("")) {
            apiPresent = true;
        }
        var dashboard = NodeDashboard.builder()
                .nodeType(PropertiesUtils.getNodeType())
                .cpuName(systemInfo.getCpu().getName())
                .totalMemory(systemInfo.getCpu().getTotalMemory())
                .totalSlots(PropertiesUtils.getTotalNodeSlots())
                .freeSpace(QueryUtils.getClientFreeSpace())
                .totalSpace(QueryUtils.getClientTotalSpace())
                .usedSpace(QueryUtils.getClientUsedSpace())
                .apiKeyPresent(apiPresent)
                .build();
        var selectedGPUs = PropertiesUtils.getSelectedGPUs();
        if (dashboard.getNodeType() != NodeType.CPU) {
            dashboard.setGpuCombined(PropertiesUtils.isGPUCombined());
            dashboard.setSelectedGPUModels(new ArrayList<>());
            dashboard.setAvailableGPUModels(new ArrayList<>());
            for (GPU gpu : systemInfo.getGpuList()) {
                dashboard.getAvailableGPUModels().add(gpu.getModel());
            }
            for (GPU gpu : selectedGPUs) {
                dashboard.getSelectedGPUModels().add(gpu.getModel());
            }
        }
        if (dashboard.getNodeType() != NodeType.GPU) {
            dashboard.setSelectedCores(PropertiesUtils.getSelectedCores());
        }

        return dashboard;
    }

    @GetMapping("/node_info")
    @Profile({"NODE", "DUAL"})
    public Node getNodeInfo() {
        var systemInfo = QueryUtils.getCurrentSystemInfo();
        return Node.builder()
                .nodeType(PropertiesUtils.getNodeType())
                .os(QueryUtils.getOS())
                .ipAddress(systemInfo.getIpAddress())
                .systemID(ConfigUtils.getProperty(ConfigKeys.SYSTEM_ID))
                .hostname(systemInfo.getHostname())
                .networkPort(ConfigUtils.getProperty(ConfigKeys.HTTPS_PORT))
                .cpu(systemInfo.getCpu())
                .selectedGPUs(PropertiesUtils.getSelectedGPUs())
                .build();
    }

    @GetMapping("/is_first_time")
    public Map<String, Boolean> isFirstTime(){
        var firstTime = new HashMap<String, Boolean>();
        firstTime.put("first_time", PropertiesUtils.isFirstTime());
        return firstTime;
    }

    @GetMapping("/version")
    public Map<String, String> getVersion() {
        var version = new HashMap<String, String>();
        version.put("version", QueryUtils.getVersion());
        return version;
    }

    @GetMapping("/build_year")
    public Map<String, String> getBuildYear() {
        var buildYear = new HashMap<String, String>();
        buildYear.put("year", QueryUtils.getBuildYear());
        return buildYear;
    }

}
