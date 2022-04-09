/*
 * Copyright (c) 2022 Dryad and Naiad Software LLC
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
 */

package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.repositories.RenderTaskRepository;
import com.dryadandnaiad.sethlans.utils.PropertiesUtils;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@RestController
@Profile({"NODE", "DUAL"})
@RequestMapping("/api/v1/node_task")
public class NodeTaskController {
    private final RenderTaskRepository renderTaskRepository;

    public NodeTaskController(RenderTaskRepository renderTaskRepository) {
        this.renderTaskRepository = renderTaskRepository;
    }

    @GetMapping(value = "/retrieve_image_file")
    public @ResponseBody
    byte[] getImageFile(@RequestParam("system-id") String systemID,
                        @RequestParam("task-id") String taskID) {
        var authorizedServer = PropertiesUtils.getAuthorizedServer();
        if (authorizedServer != null && authorizedServer.getSystemID().equals(systemID)) {
            var renderTask = renderTaskRepository.getRenderTaskByTaskID(taskID);
            try {
                if (renderTask.isSentToServer()) {
                    var fileToSend = renderTask.getTaskDir() + File.separator + renderTask.getTaskImageFile();
                    var inputStream = new BufferedInputStream(new
                            FileInputStream(fileToSend));
                    return IOUtils.toByteArray(inputStream);
                }
            } catch (IOException e) {
                log.error(e.getMessage());
                log.error(Throwables.getStackTraceAsString(e));
                return null;
            }

        }
        return null;
    }
}
