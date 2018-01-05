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

import com.dryadandnaiad.sethlans.commands.SetupForm;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBinary;
import com.dryadandnaiad.sethlans.domains.database.users.SethlansRole;
import com.dryadandnaiad.sethlans.domains.database.users.SethlansUser;
import com.dryadandnaiad.sethlans.enums.BlenderBinaryOS;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.database.BlenderBinaryDatabaseService;
import com.dryadandnaiad.sethlans.services.database.RoleDatabaseService;
import com.dryadandnaiad.sethlans.services.database.UserDatabaseService;
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

import static com.dryadandnaiad.sethlans.utils.SethlansUtils.writeProperty;

/**
 * Created Mario Estrella on 3/18/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class SaveSetupSetupConfigServiceImpl implements SaveSetupConfigService {
    private static final Logger LOG = LoggerFactory.getLogger(SaveSetupSetupConfigServiceImpl.class);
    private UserDatabaseService userDatabaseService;
    private RoleDatabaseService roleDatabaseService;
    private BlenderBinaryDatabaseService blenderBinaryDatabaseService;

    @Autowired
    public void setUserDatabaseService(UserDatabaseService userDatabaseService) {
        this.userDatabaseService = userDatabaseService;
    }

    @Autowired
    public void setRoleDatabaseService(RoleDatabaseService roleDatabaseService) {
        this.roleDatabaseService = roleDatabaseService;
    }

    @Autowired
    public void setBlenderBinaryDatabaseService(BlenderBinaryDatabaseService blenderBinaryDatabaseService) {
        this.blenderBinaryDatabaseService = blenderBinaryDatabaseService;
    }

    @Override
    public void saveSethlansSettings(SetupForm setupForm) {
        SethlansRole admin = new SethlansRole();
        admin.setRole("ADMIN");
        roleDatabaseService.saveOrUpdate(admin);
        SethlansRole userRole = new SethlansRole();
        userRole.setRole("USER");
        roleDatabaseService.saveOrUpdate(userRole);


        SethlansUser user = new SethlansUser();
        user.setUsername(setupForm.getUsername());
        user.setPassword(setupForm.getPassword());
        user.addRole(admin);
        user.addRole(userRole);
        userDatabaseService.saveOrUpdate(user);

        writeProperty(SethlansConfigKeys.HTTPS_PORT, setupForm.getHttpsPort());
        writeProperty(SethlansConfigKeys.SETHLANS_IP, setupForm.getIpAddress());
        writeProperty(SethlansConfigKeys.LOGGING_FILE, setupForm.getLogDirectory() + "sethlans.log");
        writeProperty(SethlansConfigKeys.MODE, setupForm.getMode().toString());
        writeProperty(SethlansConfigKeys.BINARY_DIR, setupForm.getBinDirectory());
        writeProperty(SethlansConfigKeys.SCRIPTS_DIR, setupForm.getScriptsDirectory());
        writeProperty(SethlansConfigKeys.TEMP_DIR, setupForm.getTempDirectory());

        writeProperty("spring.jpa.hibernate.ddl-auto", "validate");

        File binDir = new File(setupForm.getBinDirectory());
        File scriptDir = new File(setupForm.getScriptsDirectory());
        File tempDir = new File(setupForm.getTempDirectory());

        if (!binDir.mkdirs()) {
            LOG.error("Unable to create directory " + binDir.toString());
            System.exit(1);
        }

        if (!scriptDir.mkdirs()) {
            LOG.error("Unable to create directory " + scriptDir.toString());
            System.exit(1);
        }

        if (!tempDir.mkdirs()) {
            LOG.error("Unable to create directory " + tempDir.toString());
            System.exit(1);
        }

    }

    @Override
    public void saveServerSettings(SetupForm setupForm) {
        for (BlenderBinaryOS os : setupForm.getBlenderBinaryOS()) {
            BlenderBinary blenderBinary = new BlenderBinary();
            blenderBinary.setBlenderVersion(setupForm.getBlenderVersion());
            blenderBinary.setBlenderBinaryOS(os.toString());
            blenderBinaryDatabaseService.saveOrUpdate(blenderBinary);
        }
        writeProperty(SethlansConfigKeys.PROJECT_DIR, setupForm.getProjectDirectory());
        writeProperty(SethlansConfigKeys.BLENDER_DIR, setupForm.getBlenderDirectory());
        writeProperty(SethlansConfigKeys.BENCHMARK_DIR, setupForm.getBenchmarkDirectory());
        writeProperty(SethlansConfigKeys.PRIMARY_BLENDER_VERSION, setupForm.getBlenderVersion());


        LOG.debug("Server Settings Saved");
        // Create directories
        File projectDir = new File(setupForm.getProjectDirectory());
        File blenderDir = new File(setupForm.getBlenderDirectory());
        File benchDir = new File(setupForm.getBenchmarkDirectory());

        if (!projectDir.mkdirs()) {
            LOG.error("Unable to create directory " + projectDir.toString());
            System.exit(1);
        }
        if (!blenderDir.mkdirs()) {
            LOG.error("Unable to create directory " + blenderDir.toString());
            System.exit(1);
        }

        if (!benchDir.mkdirs()) {
            LOG.error("Unable to create directory " + benchDir.toString());
            System.exit(1);
        }

        copyBenchmarks(setupForm, benchDir);
    }

    @Override
    public void saveNodeSettings(SetupForm setupForm) {
        writeProperty(SethlansConfigKeys.CACHE_DIR, setupForm.getWorkingDirectory());
        writeProperty(SethlansConfigKeys.COMPUTE_METHOD, setupForm.getSelectedMethod().toString());
        writeProperty(SethlansConfigKeys.TILE_SIZE_GPU, "256");
        writeProperty(SethlansConfigKeys.TILE_SIZE_CPU, "32");

        if (!setupForm.getSelectedGPUId().isEmpty()) {
            StringBuilder result = new StringBuilder();
            for (Integer id : setupForm.getSelectedGPUId()) {
                if (result.length() != 0) {
                    result.append(",");
                }
                result.append(setupForm.getAvailableGPUs().get(id).getCudaName());
            }
            writeProperty(SethlansConfigKeys.CUDA_DEVICE, result.toString());
        }

        if (!setupForm.getSelectedMethod().equals(ComputeType.GPU)) {
            writeProperty(SethlansConfigKeys.CPU_CORES, Integer.toString(setupForm.getCores()));
        }

        // Create directories
        File cacheDir = new File(setupForm.getWorkingDirectory());

        if (!cacheDir.mkdirs()) {
            LOG.error("Unable to create  directory " + cacheDir.toString());
            System.exit(1);

        }

        LOG.debug("Node Settings Saved");

    }

    @Override
    public void saveDualSettings(SetupForm setupForm) {
        saveServerSettings(setupForm);
        saveNodeSettings(setupForm);
    }

    @Override
    public void wizardCompleted(SetupForm setupForm) {
        writeProperty(SethlansConfigKeys.FIRST_TIME, "false");
        writeProperty("spring.profiles.active", setupForm.getMode().toString());
    }

    private void copyBenchmarks(SetupForm setupForm, File benchDir) {
        String benchmark = "archives/benchmarks/bmw27.txz";

        try {
            InputStream inputStream = new Resources(benchmark).getResource();
            LOG.debug("Copying Benchmarks...");
            Files.copy(inputStream, Paths.get(setupForm.getBenchmarkDirectory() + "bmw27.txz"));
            LOG.debug("Benchmarks copied successfully.");
        } catch (NoSuchFileException e) {
            LOG.error(e.getMessage());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        SethlansUtils.archiveExtract("bmw27.txz", benchDir);
    }

}
