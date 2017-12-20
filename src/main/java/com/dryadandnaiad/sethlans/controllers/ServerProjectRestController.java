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
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * Created Mario Estrella on 12/10/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@Profile({"SERVER", "DUAL"})
public class ServerProjectRestController {
    private static final Logger LOG = LoggerFactory.getLogger(ServerProjectRestController.class);

    @Value("${sethlans.benchmarkDir}")
    private String benchmarkDir;

    @Value("${sethlans.blenderDir}")
    private String blenderDir;

    private SethlansNodeDatabaseService sethlansNodeDatabaseService;

    @RequestMapping(value = "/api/project/blender_binary", method = RequestMethod.GET)
    public void downloadBlenderBinary(HttpServletResponse response, @RequestParam String connection_uuid,
                                      @RequestParam String version, @RequestParam String os) {
        if (sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.debug("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            File dir = new File(blenderDir + File.separator + "binaries" + File.separator + version);
            FileFilter fileFilter = new WildcardFileFilter(version + "-" + os.toLowerCase() + "." + "*");
            File[] files = dir.listFiles(fileFilter);
            if (files != null) {
                if (files.length > 1) {
                    LOG.error("More files than expected, only one archive per os + version expected");
                } else {
                    File blenderBinary = files[0];
                    serveFile(blenderBinary, response);
                }
            } else {
                LOG.error("No files found.");
            }
        }
    }

    @RequestMapping(value = "/api/benchmark/response", method = RequestMethod.POST)
    public void benchmarkResponse(@RequestParam String connection_uuid, @RequestParam int rating, @RequestParam String cuda_name, @RequestParam ComputeType compute_type) {
        SethlansNode sethlansNode = sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid);
        if (sethlansNode == null) {
            LOG.debug("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            LOG.debug("Receiving benchmark from Node: " + sethlansNode.getHostname());
            if (compute_type.equals(ComputeType.CPU)) {
                sethlansNode.setCpuRating(rating);
            }
            if (compute_type.equals(ComputeType.GPU)) {
                for (GPUDevice gpuDevice : sethlansNode.getSelectedGPUs()) {
                    if (gpuDevice.getCudaName().equals(cuda_name)) {
                        gpuDevice.setRating(rating);
                        LOG.debug(sethlansNode.toString());
                    }
                }
            }
            sethlansNode.setBenchmarkComplete(true);
            sethlansNodeDatabaseService.saveOrUpdate(sethlansNode);
        }


    }

    @RequestMapping(value = "/api/benchmark_files/bmw_cpu", method = RequestMethod.GET)
    public void downloadCPUBenchmark(HttpServletResponse response, @RequestParam String connection_uuid) {
        if (sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.debug("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            File bmw27_cpu = new File(benchmarkDir + File.separator + "bmw27_cpu.blend");
            serveFile(bmw27_cpu, response);

        }

    }

    @RequestMapping(value = "/api/benchmark_files/bmw_gpu", method = RequestMethod.GET)
    public void downloadGPUBenchmark(HttpServletResponse response, @RequestParam String connection_uuid) {
        if (sethlansNodeDatabaseService.getByConnectionUUID(connection_uuid) == null) {
            LOG.debug("The uuid sent: " + connection_uuid + " is not present in the database");
        } else {
            File bmw27_gpu = new File(benchmarkDir + File.separator + "bmw27_gpu.blend");
            serveFile(bmw27_gpu, response);
        }
    }

    @RequestMapping(value = "/api/project/status", method = RequestMethod.GET)
    public void projectStatus(@RequestParam String node_uuid, @RequestParam String project_uuid) {

    }

    @RequestMapping(value = "/api/project/blend_file/", method = RequestMethod.GET)
    public void downloadBlendfile(HttpServletResponse response, @RequestParam String connection_uuid, @RequestParam String project_uuid) {

    }

    private void serveFile(File file, HttpServletResponse response) {
        try {
            String mimeType = "application/octet-stream";
            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
            response.setContentLength((int) file.length());
            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            FileCopyUtils.copy(inputStream, response.getOutputStream());

        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    @Autowired
    public void setSethlansNodeDatabaseService(SethlansNodeDatabaseService sethlansNodeDatabaseService) {
        this.sethlansNodeDatabaseService = sethlansNodeDatabaseService;
    }
}
