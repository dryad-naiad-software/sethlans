package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.repositories.RenderTaskRepository;
import com.dryadandnaiad.sethlans.repositories.ServerRepository;
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
    private final ServerRepository serverRepository;
    private final RenderTaskRepository renderTaskRepository;

    public NodeTaskController(ServerRepository serverRepository, RenderTaskRepository renderTaskRepository) {
        this.serverRepository = serverRepository;
        this.renderTaskRepository = renderTaskRepository;
    }

    @GetMapping(value = "/retrieve_image_file")
    public @ResponseBody
    byte[] getImageFile(@RequestParam("system-id") String systemID,
                        @RequestParam("task-id") String taskID) {
        if (serverRepository.findBySystemID(systemID).isPresent()) {
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
