/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
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

package com.dryadandnaiad.sethlans.services.config;

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBinary;
import com.dryadandnaiad.sethlans.domains.database.user.SethlansUser;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.forms.setup.SetupForm;
import com.dryadandnaiad.sethlans.services.database.BlenderBinaryDatabaseService;
import com.dryadandnaiad.sethlans.services.database.SethlansUserDatabaseService;
import com.dryadandnaiad.sethlans.services.ffmpeg.FFmpegSetupService;
import com.dryadandnaiad.sethlans.services.python.PythonSetupService;
import com.dryadandnaiad.sethlans.services.system.SethlansManagerService;
import com.dryadandnaiad.sethlans.utils.Resources;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Arrays;

import static com.dryadandnaiad.sethlans.utils.SethlansUtils.getGPUDeviceString;
import static com.dryadandnaiad.sethlans.utils.SethlansUtils.writeProperty;

/**
 * Created Mario Estrella on 2/23/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class SaveSetupConfigServiceImpl implements SaveSetupConfigService {
    private BlenderBinaryDatabaseService blenderBinaryDatabaseService;
    private SethlansUserDatabaseService sethlansUserDatabaseService;
    private PythonSetupService pythonSetupService;
    private SethlansManagerService sethlansManagerService;
    private FFmpegSetupService fFmpegSetupService;
    private static final Logger LOG = LoggerFactory.getLogger(SaveSetupConfigServiceImpl.class);


    @Override
    public void saveSetupSettings(SetupForm setupForm) {

        // Set Sethlans Directories
        String scriptsDirectory = setupForm.getRootDirectory() + File.separator + "scripts" + File.separator;
        String projectDirectory = setupForm.getRootDirectory() + File.separator + "projects" + File.separator;
        String blenderDirectory = setupForm.getRootDirectory() + File.separator + "blenderzip" + File.separator;
        String tempDirectory = setupForm.getRootDirectory() + File.separator + "temp" + File.separator;
        String workingDirectory = setupForm.getRootDirectory() + File.separator + "render_cache" + File.separator;
        String blendfileDirectory = setupForm.getRootDirectory() + File.separator + "blendfile_cache" + File.separator;
        String logDirectory = setupForm.getRootDirectory() + File.separator + "logs" + File.separator;
        String binDirectory = setupForm.getRootDirectory() + File.separator + "bin" + File.separator;
        String benchmarkDirectory = setupForm.getRootDirectory() + File.separator + "benchmarks" + File.separator;

        // Set User
        SethlansUser administrator = setupForm.getUser();
        administrator.setUsername(administrator.getUsername().toLowerCase());
        administrator.setRoles(Arrays.asList(Role.SUPER_ADMINISTRATOR));
        administrator.setActive(true);
        administrator.setPasswordUpdated(true);
        sethlansUserDatabaseService.saveOrUpdate(administrator);

        //Write Properties
        writeProperty(SethlansConfigKeys.HTTPS_PORT, setupForm.getPort());
        writeProperty(SethlansConfigKeys.SETHLANS_IP, setupForm.getIpAddress());
        writeProperty(SethlansConfigKeys.LOGGING_DIR, logDirectory);
        writeProperty(SethlansConfigKeys.LOGGING_FILE, logDirectory + "sethlans.log");
        writeProperty(SethlansConfigKeys.MODE, setupForm.getMode().toString());
        writeProperty(SethlansConfigKeys.BINARY_DIR, binDirectory);
        writeProperty(SethlansConfigKeys.SCRIPTS_DIR, scriptsDirectory);
        writeProperty(SethlansConfigKeys.TEMP_DIR, tempDirectory);
        writeProperty(SethlansConfigKeys.ROOT_DIR, setupForm.getRootDirectory());
        writeProperty("spring.jpa.hibernate.ddl-auto", "validate");
        LOG.debug("Main Sethlans properties saved.");

        // Creating main Sethlan Directories
        LOG.debug("Creating main Sethlans directories.");
        createDirectories(new File(binDirectory));
        createDirectories(new File(scriptsDirectory));
        createDirectories(new File(tempDirectory));

        if (setupForm.getMode() == SethlansMode.SERVER || setupForm.getMode() == SethlansMode.DUAL) {
            BlenderBinary blenderBinary = new BlenderBinary();
            blenderBinary.setBlenderVersion(setupForm.getServer().getBlenderVersion());
            blenderBinary.setBlenderBinaryOS(SethlansUtils.getOS());
            blenderBinaryDatabaseService.saveOrUpdate(blenderBinary);

            writeProperty(SethlansConfigKeys.PROJECT_DIR, projectDirectory);
            writeProperty(SethlansConfigKeys.BLENDER_DIR, blenderDirectory);
            writeProperty(SethlansConfigKeys.BENCHMARK_DIR, benchmarkDirectory);
            writeProperty(SethlansConfigKeys.PRIMARY_BLENDER_VERSION, setupForm.getServer().getBlenderVersion());
            fFmpegSetupService.installFFmpeg(binDirectory);
            LOG.debug("Server Settings Saved");

            LOG.debug("Creating Sethlans server directories.");
            createDirectories(new File(projectDirectory));
            createDirectories(new File(blenderDirectory));
            createDirectories(new File(benchmarkDirectory));

            copyBenchmarks(benchmarkDirectory);
        }

        if (setupForm.getMode() == SethlansMode.NODE || setupForm.getMode() == SethlansMode.DUAL) {
            writeProperty(SethlansConfigKeys.CACHE_DIR, workingDirectory);
            writeProperty(SethlansConfigKeys.BLEND_FILE_CACHE_DIR, blendfileDirectory);
            writeProperty(SethlansConfigKeys.CACHED_BLENDER_BINARIES, "");
            writeProperty(SethlansConfigKeys.COMPUTE_METHOD, setupForm.getNode().getComputeMethod().toString());
            writeProperty(SethlansConfigKeys.TILE_SIZE_GPU, Integer.toString(setupForm.getNode().getTileSizeGPU()));
            writeProperty(SethlansConfigKeys.TILE_SIZE_CPU, Integer.toString(setupForm.getNode().getTileSizeCPU()));

            if (!setupForm.getNode().getComputeMethod().equals(ComputeType.CPU)) {
                writeProperty(SethlansConfigKeys.GPU_DEVICE, getGPUDeviceString(setupForm.getNode()));
            }

            if (!setupForm.getNode().getComputeMethod().equals(ComputeType.GPU)) {
                writeProperty(SethlansConfigKeys.CPU_CORES, Integer.toString(setupForm.getNode().getCores()));
            }

            // Create Node Directories
            LOG.debug("Creating Sethlans node directories.");
            createDirectories(new File(workingDirectory));
            createDirectories(new File(blendfileDirectory));
            LOG.debug("Node Settings Saved");
        }
        // Setup wizard complete
        writeProperty(SethlansConfigKeys.FIRST_TIME, "false");
        writeProperty("spring.profiles.active", setupForm.getMode().toString());
        LOG.debug("Downloading and Installing Python");
        pythonSetupService.installPython(binDirectory);
        pythonSetupService.setupScripts(scriptsDirectory);
        LOG.info("Setup complete complete. Restarting Sethlans");
        sethlansManagerService.restart();
    }

    private void createDirectories(File directory) {
        if (!directory.mkdirs()) {
            LOG.error("Unable to create directory" + directory);
        }

    }

    private void copyBenchmarks(String benchmarkDirectory) {
        String benchmark = "archives/benchmarks/bmw27.txz";

        try {
            InputStream inputStream = new Resources(benchmark).getResource();
            LOG.debug("Copying Benchmarks...");
            Files.copy(inputStream, Paths.get(benchmarkDirectory + "bmw27.txz"));
            LOG.debug("Benchmarks copied successfully.");
        } catch (NoSuchFileException e) {
            LOG.error(e.getMessage());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        SethlansUtils.archiveExtract("bmw27.txz", new File(benchmarkDirectory));
    }

    @Autowired
    public void setBlenderBinaryDatabaseService(BlenderBinaryDatabaseService blenderBinaryDatabaseService) {
        this.blenderBinaryDatabaseService = blenderBinaryDatabaseService;
    }

    @Autowired
    public void setSethlansUserDatabaseService(SethlansUserDatabaseService sethlansUserDatabaseService) {
        this.sethlansUserDatabaseService = sethlansUserDatabaseService;
    }

    @Autowired
    public void setPythonSetupService(PythonSetupService pythonSetupService) {
        this.pythonSetupService = pythonSetupService;
    }

    @Autowired
    public void setSethlansManagerService(SethlansManagerService sethlansManagerService) {
        this.sethlansManagerService = sethlansManagerService;
    }

    @Autowired
    public void setfFmpegSetupService(FFmpegSetupService fFmpegSetupService) {
        this.fFmpegSetupService = fFmpegSetupService;
    }
}
