/*
 * Copyright (c) 2020. Dryad and Naiad Software LLC.
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.dryadandnaiad.sethlans;

import com.dryadandnaiad.sethlans.executor.SethlansState;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * File created by Mario Estrella on 4/1/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
@SpringBootApplication(exclude = EmbeddedMongoAutoConfiguration.class)
public class SethlansApplication {

    public static void main(String[] args) {
        ConfigUtils.getConfigFile();
        new SethlansApplication().doMain(args);
    }

    public void doMain(String[] args) {
        List<String> arrayArgs = new ArrayList<>();
        arrayArgs.add("--spring.config.name=sethlans");
        arrayArgs.add("--spring.config.additional-location=" + System.getProperty("user.home") + File.separator + ".sethlans" + File.separator + "config" + File.separator);
        String[] springArgs = new String[arrayArgs.size()];
        springArgs = arrayArgs.toArray(springArgs);
        startSpring(springArgs);

    }

    private void startSpring(String[] springArgs) {
        SethlansState sethlansState = SethlansState.getInstance();
        while (true) {
            try {
                if (sethlansState.sethlansActive) {
                    Thread.sleep(1000);
                } else {
                    Thread.sleep(1000);
                    SpringApplicationBuilder builder = new SpringApplicationBuilder(SethlansApplication.class);
                    builder.headless(false);
                    sethlansState.sethlansActive = builder.run(springArgs).isActive();
                }
            } catch (InterruptedException e) {
                log.error(e.getMessage());
                log.error(Throwables.getStackTraceAsString(e));
            }
        }


    }
}
