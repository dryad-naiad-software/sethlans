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

package com.dryadandnaiad.sethlans.webui;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.scan.StandardJarScanner;

import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author Mario Estrella <mestrella@dryadandnaiad.com>
 */
public class SethlansWebUI {

    private static final Logger LOG = LogManager.getLogger(SethlansWebUI.class);

    public static void start(String httpPort, String httpsPort) {

        try {

            System.setProperty("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE", "true");
            Tomcat tomcat = new Tomcat();
            Path tempPath = Files.createTempDirectory("tomcat-base-dir");
            LOG.debug(tempPath.toString());
            tomcat.setBaseDir(tempPath.toString());
            String webPort = System.getenv("PORT");
            if (webPort == null || webPort.isEmpty()) {
                webPort = httpPort;
            }
            String contextPath = "";
            String appBase = ".";
            tomcat.setPort(Integer.valueOf(webPort));
            tomcat.getHost().setAppBase(appBase);
            StandardContext context = (StandardContext) tomcat.addWebapp(contextPath, appBase);
            ((StandardJarScanner) context.getJarScanner()).setScanAllDirectories(true);

            tomcat.start();
            tomcat.getServer().await();

        } catch (IOException | ServletException | LifecycleException ex) {
            LOG.error(ex.getMessage());
        }

    }

}
