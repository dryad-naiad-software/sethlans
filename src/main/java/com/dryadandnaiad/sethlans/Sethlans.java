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

package com.dryadandnaiad.sethlans;

import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.utils.SethlansState;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.dryadandnaiad.sethlans.utils.SethlansConfigUtils.getProperty;


/**
 * Created Mario Estrella on 3/9/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@SpringBootApplication

public class Sethlans {
    private static final Logger LOG = LoggerFactory.getLogger(Sethlans.class);

    public static void main(String[] args) {
        new Sethlans().doMain(args);
    }

    public void doMain(String[] args) {
        List<String> arrayArgs = new ArrayList<>();

        // Setting up initial config directory
        File configDirectory = new File(System.getProperty("user.home") + File.separator + ".sethlans_install" + File.separator + "config" + File.separator);
        File configFile = new File(configDirectory + "sethlans_install.properties");
        if (!new File(configDirectory + "sethlans_install.properties").exists()) {
            configDirectory.mkdirs();
            arrayArgs.add("--spring.config.name=sethlans");
            arrayArgs.add("--spring.config.location=" + configDirectory.toString() + File.separator);
        } else {
            configDirectory = new File(getProperty(SethlansConfigKeys.CONFIG_DIR, configFile));
            arrayArgs.add("--spring.config.name=sethlans");
            arrayArgs.add("--spring.config.location=" + configDirectory.toString() + File.separator);
        }

        String[] springArgs = new String[arrayArgs.size()];
        springArgs = arrayArgs.toArray(springArgs);
        startSpring(springArgs);
    }


    private void startSpring(String[] springArgs) {
        File configDirectory = new File(System.getProperty("user.home") + File.separator + ".sethlans_install" + File.separator + "config" + File.separator);
        File installFile = new File(configDirectory + File.separator + "sethlans_install.properties");
        SethlansState sethlansState = SethlansState.getInstance();
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                if (sethlansState.sethlansActive) {
                    Thread.sleep(1000);
                } else {
                    Thread.sleep(1000);
                    if (installFile.exists()) {
                        System.out.println("Running config change");
                        List<String> arrayArgs = new ArrayList<>();
                        File newConfigDirectory = new File(getProperty(SethlansConfigKeys.CONFIG_DIR, installFile));
                        arrayArgs.add("--spring.config.name=sethlans");
                        arrayArgs.add("--spring.config.location=" + newConfigDirectory.toString() + File.separator);
                        springArgs = new String[arrayArgs.size()];
                        springArgs = arrayArgs.toArray(springArgs);
                    }
                    SpringApplicationBuilder builder = new SpringApplicationBuilder(Sethlans.class);
                    builder.headless(false);
                    sethlansState.sethlansActive = builder.run(springArgs).isActive();
                }
            } catch (InterruptedException e) {
                LOG.error(Throwables.getStackTraceAsString(e));
            }
        }
    }
}

