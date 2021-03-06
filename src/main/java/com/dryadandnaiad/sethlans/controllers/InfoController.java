/*
 * Copyright (c) 2019 Dryad and Naiad Software LLC
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

import com.dryadandnaiad.sethlans.domains.hardware.CPU;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.utils.BlenderUtils;
import com.dryadandnaiad.sethlans.utils.SethlansQueryUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static com.dryadandnaiad.sethlans.utils.SethlansConfigUtils.getProperty;

/**
 * Created Mario Estrella on 2/11/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@RequestMapping("/api/info")
public class InfoController {
    private List<String> blenderVersions = BlenderUtils.listVersions();
    private static final Logger LOG = LoggerFactory.getLogger(InfoController.class);

    @Value("${sethlans.configDir}")
    private String configDir;

    @Value("${sethlans.firsttime}")
    private boolean firstTime;

    @Value("${sethlans.computeMethod}")
    private ComputeType computeType;


    @GetMapping(value = {"/first_time"})
    public boolean isFirstTime() {
        return firstTime;
    }

    @GetMapping(value = {"/version"})
    public Map getVersion() {
        return Collections.singletonMap("version", SethlansQueryUtils.getVersion() + "-beta2");
    }

    @GetMapping(value = {"/blender_versions"})
    public Map getBlenderVersions() {
        return Collections.singletonMap("blenderVersions", blenderVersions);
    }

    @GetMapping(value = {"/root_directory"})
    public Map getRootDirectory() {
        if (firstTime) {
            return Collections.singletonMap("root_dir", System.getProperty("user.home") + File.separator + ".sethlans");
        } else {
            return Collections.singletonMap("root_dir", getProperty(SethlansConfigKeys.ROOT_DIR));
        }
    }

    @GetMapping(value = {"/java_version"})
    public Map getJavaVersion() {
        return Collections.singletonMap("java_version", System.getProperty("java.version"));
    }

    @GetMapping(value = {"/get_started"})
    public boolean runGetStarted() {
        if (firstTime) {
            return false;
        }
        return Boolean.parseBoolean(getProperty(SethlansConfigKeys.GETTING_STARTED));
    }

    @GetMapping(value = {"/available_roles"})
    public EnumSet<Role> getRoles() {
        return EnumSet.allOf(Role.class);
    }

    @GetMapping(value = {"/total_memory"})
    public String totalMemory() {
        CPU cpu = new CPU();
        return cpu.getTotalMemory();
    }

    @GetMapping(value = {"/cpu_name"})
    public String cpuInfo() {
        CPU cpu = new CPU();
        return cpu.getName();
    }

    @GetMapping(value = {"/sethlans_port"})
    public Map getHttpsPort() {
        if (firstTime) {
            return Collections.singletonMap("port", "7443");
        } else {
            return Collections.singletonMap("port", SethlansQueryUtils.getPort());
        }
    }

    @GetMapping(value = {"/sethlans_mode"})
    public Map getSethlansMode() {
        if (firstTime) {
            return Collections.singletonMap("mode", "SETUP");
        }
        return Collections.singletonMap("mode", SethlansQueryUtils.getMode().toString());

    }

    @GetMapping(value = {"/sethlans_ip"})
    public Map getSethlansIPAddress() {
        return Collections.singletonMap("ip", SethlansQueryUtils.getIP());
    }

    @GetMapping(value = {"/sethlans_hostname"})
    public Map getSethlansHostname() {
        return Collections.singletonMap("hostname", SethlansQueryUtils.getHostname());
    }

    @GetMapping(value = {"/app_url"})
    public Map getAppURL() {
        return Collections.singletonMap("app_url", "https://" + SethlansQueryUtils.getHostname().toLowerCase() + ":" + SethlansQueryUtils.getPort());
    }

    @GetMapping(value = {"/challenge_question_list"})
    public List<String> getChallengeList() {
        return SethlansQueryUtils.getChallengeQuestionList();
    }


}
