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

import com.dryadandnaiad.sethlans.executor.MainExecutor;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

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
@EnableScheduling
@SpringBootApplication(exclude = EmbeddedMongoAutoConfiguration.class)
public class SethlansApplication {

    private static ConfigurableApplicationContext context;

    public static void main(String[] args) {
        ConfigUtils.getConfigFile();
        SethlansApplication.doMain(args);
    }

    public static void doMain(String[] args) {
        List<String> arrayArgs = new ArrayList<>();
        arrayArgs.add("--spring.config.name=sethlans");
        arrayArgs.add("--spring.config.additional-location=" + System.getProperty("user.home") + File.separator + ".sethlans" + File.separator + "config" + File.separator + "sethlans.properties");
        arrayArgs.add("--spring.main.headless=false");
        String[] springArgs = new String[arrayArgs.size()];
        springArgs = arrayArgs.toArray(springArgs);
        context = SpringApplication.run(SethlansApplication.class, springArgs);

    }

    public static void restart() {
        ApplicationArguments args = context.getBean(ApplicationArguments.class);

        Thread thread = new Thread(() -> {
            MainExecutor.getInstance().getExecutor().shutdown();
            context.close();
            context = SpringApplication.run(SethlansApplication.class, args.getSourceArgs());
        });

        thread.setDaemon(false);
        thread.start();
    }

}
