/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC
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
import com.dryadandnaiad.sethlans.domains.SethlansUser;
import com.dryadandnaiad.sethlans.domains.security.SethlansRole;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.RoleService;
import com.dryadandnaiad.sethlans.services.SaveSetupConfigService;
import com.dryadandnaiad.sethlans.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

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
    private final String path = System.getProperty("user.home") + File.separator + ".sethlans";
    private final File configDirectory = new File(path + File.separator + "config");
    private final File configFile = new File(configDirectory + File.separator + "sethlans.properties");
    private Properties sethlansProperties;


    private UserService userService;
    private RoleService roleService;


    public SaveSetupSetupConfigServiceImpl() {
        this.sethlansProperties = new Properties();
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setRoleService(RoleService roleService) {
        this.roleService = roleService;
    }

    @Override
    public void saveSethlansSettings(SetupForm setupForm) {
        SethlansRole admin = new SethlansRole();
        admin.setRole("ADMIN");
        roleService.saveOrUpdate(admin);
        SethlansRole userRole = new SethlansRole();
        userRole.setRole("USER");
        roleService.saveOrUpdate(userRole);


        SethlansUser user = new SethlansUser();
        user.setUsername(setupForm.getUsername());
        user.setPassword(setupForm.getPassword());
        user.addRole(admin);
        user.addRole(userRole);
        userService.saveOrUpdate(user);

        writeProperty(SethlansConfigKeys.HTTPS_PORT, setupForm.getHttpsPort());
        writeProperty(SethlansConfigKeys.LOGGING_FILE, setupForm.getLogDirectory());
        writeProperty(SethlansConfigKeys.MODE, setupForm.getMode().toString());
        writeProperty("spring.jpa.hibernate.ddl-auto", "validate");
    }

    @Override
    public void saveServerSettings(SetupForm setupForm) {
        writeProperty(SethlansConfigKeys.PROJECT_DIR, setupForm.getProjectDirectory());
        writeProperty(SethlansConfigKeys.BLENDER_DIR, setupForm.getBlenderDirectory());
        writeProperty(SethlansConfigKeys.TEMP_DIR, setupForm.getTempDirectory());
     }

    @Override
    public void saveNodeSettings(SetupForm setupForm) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(configFile);
            sethlansProperties.setProperty(SethlansConfigKeys.CACHE_DIR.toString(), setupForm.getWorkingDirectory());
            sethlansProperties.setProperty(SethlansConfigKeys.COMPUTE_METHOD.toString(), setupForm.getSelectedMethod().toString());
            if (!setupForm.getSelectedGPUId().isEmpty()) {
                StringBuilder result = new StringBuilder();
                for (Integer id : setupForm.getSelectedGPUId()) {
                    if (result.length() != 0) {
                        result.append(",");
                    }
                    result.append(setupForm.getAvailableGPUs().get(id).getCudaName());
                }

                sethlansProperties.setProperty(SethlansConfigKeys.CUDA_DEVICE.toString(), result.toString());
            }
            if (!setupForm.getSelectedMethod().equals(ComputeType.GPU)) {
                sethlansProperties.setProperty(SethlansConfigKeys.CPU_CORES.toString(), Integer.toString(setupForm.getCores()));
            }


            //Save Properties to File
            sethlansProperties.store(fileOutputStream, "");
            LOG.debug("Node Settings Saved");

            // Create directories
            File cacheDir = new File(setupForm.getWorkingDirectory());

            if (!cacheDir.mkdirs()) {
                LOG.error("Unable to create project directory " + cacheDir.toString());
                // TODO Placeholders for now will need to replace System.exit with a friendly message to GUI and restart the setup wizard.
                System.exit(1);

            }

        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    @Override
    public void saveDualSettings(SetupForm setupForm) {
        saveServerSettings(setupForm);
        saveNodeSettings(setupForm);
    }

    @Override
    public void wizardCompleted() {
        writeProperty(SethlansConfigKeys.FIRST_TIME, "false");
    }

}
