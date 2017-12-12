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

import com.dryadandnaiad.sethlans.services.database.SethlansNodeDatabaseService;
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

    @RequestMapping(value = "/api/project/blender")
    public void downloadBlender(HttpServletResponse response, @RequestParam String uuid) {

    }

    @RequestMapping(value = "/api/project/blendfile/")
    public void downloadBlendfile(HttpServletResponse response, @RequestParam String uuid,
                                  @RequestParam String version, @RequestParam String os) {
        try {
            File bmw27_cpu = new File(blenderDir + File.separator + version + "bmw27_cpu.blend");
            String mimeType = "application/octet-stream";

            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + bmw27_cpu.getName() + "\"");
            response.setContentLength((int) bmw27_cpu.length());
            InputStream inputStream = new BufferedInputStream(new FileInputStream(bmw27_cpu));
            FileCopyUtils.copy(inputStream, response.getOutputStream());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    @RequestMapping(value = "/api/benchmarks/bmw_cpu", method = RequestMethod.GET)
    public void downloadCPUBenchmark(HttpServletResponse response, @RequestParam String uuid) {
        if (sethlansNodeDatabaseService.getByUUID(uuid) == null) {
            LOG.debug("The uuid sent: " + uuid + " is not present in the database");
        } else {
            try {
                File bmw27_cpu = new File(benchmarkDir + File.separator + "bmw27_cpu.blend");
                String mimeType = "application/octet-stream";

                response.setContentType(mimeType);
                response.setHeader("Content-Disposition", "attachment; filename=\"" + bmw27_cpu.getName() + "\"");
                response.setContentLength((int) bmw27_cpu.length());
                InputStream inputStream = new BufferedInputStream(new FileInputStream(bmw27_cpu));
                FileCopyUtils.copy(inputStream, response.getOutputStream());
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        }

    }

    @RequestMapping(value = "/api/benchmarks/bmw_gpu", method = RequestMethod.GET)
    public void downloadGPUBenchmark(HttpServletResponse response, @RequestParam String uuid) {
        if (sethlansNodeDatabaseService.getByUUID(uuid) == null) {
            LOG.debug("The uuid sent: " + uuid + " is not present in the database");
        } else {
            try {
                File bmw27_gpu = new File(benchmarkDir + File.separator + "bmw27_gpu.blend");
                String mimeType = "application/octet-stream";

                response.setContentType(mimeType);
                response.setHeader("Content-Disposition", "attachment; filename=\"" + bmw27_gpu.getName() + "\"");
                response.setContentLength((int) bmw27_gpu.length());
                InputStream inputStream = new BufferedInputStream(new FileInputStream(bmw27_gpu));
                FileCopyUtils.copy(inputStream, response.getOutputStream());
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        }
    }

    @Autowired
    public void setSethlansNodeDatabaseService(SethlansNodeDatabaseService sethlansNodeDatabaseService) {
        this.sethlansNodeDatabaseService = sethlansNodeDatabaseService;
    }
}
