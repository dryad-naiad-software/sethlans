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

import com.dryadandnaiad.sethlans.domains.database.events.SethlansNotification;
import com.dryadandnaiad.sethlans.domains.database.server.AccessKey;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.NotificationType;
import com.dryadandnaiad.sethlans.forms.setup.subclasses.SetupNode;
import com.dryadandnaiad.sethlans.services.config.UpdateComputeService;
import com.dryadandnaiad.sethlans.services.database.AccessKeyDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.notification.SethlansNotificationService;
import com.dryadandnaiad.sethlans.services.system.SethlansManagerService;
import com.dryadandnaiad.sethlans.utils.SethlansNodeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

/**
 * Created Mario Estrella on 4/2/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: Sethlans
 */
@RestController
@Profile({"NODE", "DUAL"})
@RequestMapping("/api/setup")
public class NodeSetupController {
    private SethlansServerDatabaseService sethlansServerDatabaseService;
    private UpdateComputeService updateComputeService;
    private SethlansManagerService sethlansManagerService;
    private AccessKeyDatabaseService accessKeyDatabaseService;
    private SethlansNotificationService sethlansNotificationService;

    private static final Logger LOG = LoggerFactory.getLogger(NodeSetupController.class);

    @PostMapping(value = "/add_access_key")
    public boolean addAccessKey(@RequestParam String access_key) {
        Pattern uuid = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
        if (!uuid.matcher(access_key).matches()) {
            LOG.error("Key submitted in an invalid format. " + access_key);
            return false;
        }
        if (accessKeyDatabaseService.getByUUID(access_key) == null) {
            LOG.info("Adding server access key to database:" + access_key);
            String message = "New server access key added";
            SethlansNotification sethlansNotification = new SethlansNotification(NotificationType.SERVER, message);
            sethlansNotificationService.sendNotification(sethlansNotification);
            AccessKey accessKey = new AccessKey();
            accessKey.setAccessKey(access_key);
            accessKeyDatabaseService.saveOrUpdate(accessKey);
            return true;
        }
        return false;
    }

    @GetMapping("/access_key_delete/{id}")
    public void deleteKey(@PathVariable Long id) {
        accessKeyDatabaseService.delete(id);
    }

    @GetMapping("/server_delete/{id}")
    public void deleteServer(@PathVariable Long id) {
        sethlansServerDatabaseService.delete(id);
    }

    @PostMapping("/update_compute")
    public boolean submit(@RequestBody SetupNode setupNode) {
        LOG.info("Processing Compute Setting Update");
        if (setupNode != null) {
            LOG.debug(setupNode.toString());
            if (setupNode.getComputeMethod().equals(ComputeType.CPU_GPU)) {
                SethlansNodeUtils.cpuGPUNodeCheck(setupNode);
            }
            boolean updateComplete = updateComputeService.saveComputeSettings(setupNode);
            try {
                Thread.sleep(5000);
                sethlansManagerService.restart();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return updateComplete;
        } else {
            return false;
        }
    }


    @Autowired
    public void setSethlansServerDatabaseService(SethlansServerDatabaseService sethlansServerDatabaseService) {
        this.sethlansServerDatabaseService = sethlansServerDatabaseService;
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
    public void setAccessKeyDatabaseService(AccessKeyDatabaseService accessKeyDatabaseService) {
        this.accessKeyDatabaseService = accessKeyDatabaseService;
    }

    @Autowired
    public void setSethlansNotificationService(SethlansNotificationService sethlansNotificationService) {
        this.sethlansNotificationService = sethlansNotificationService;
    }
}
