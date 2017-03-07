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

import com.dryadandnaiad.sethlans.enums.StringKey;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import com.dryadandnaiad.sethlans.webui.systray.SystemTrayIconListener;
import com.dryadandnaiad.sethlans.webui.systray.SystemTrayIconMenu;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.scan.StandardJarScanner;

import javax.servlet.ServletException;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author Mario Estrella <mestrella@dryadandnaiad.com>
 */
public class SethlansWebUI {

    private static final Logger LOG = LogManager.getLogger(SethlansWebUI.class);
    private static SystemTrayIconMenu sysTrayMenu;
    private static Tomcat tomcat;

    public static void start(String httpPort, String httpsPort) {

        try {

            System.setProperty("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE", "true");
            tomcat = new Tomcat();
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

            if (SystemTray.isSupported()) {
                sysTrayMenu = new SystemTrayIconMenu();
                sysTrayMenu.refresh();
                sysTrayMenu.setMenuListener(new SystemTrayIconListener() {
                    @Override
                    public void aboutDialogRequested() {
                        LOG.debug("About dialog selected");

                    }

                    @Override
                    public void startServerEventRequested() {
                        LOG.debug("Starting tomcat server");
                        try {
                            tomcat.start();
                        } catch (LifecycleException e) {
                            LOG.error("A lifecycle exception occurred" + e.getMessage());
                        }
                    }

                    @Override
                    public void stopServerEventRequested() {
                        LOG.debug("Stopping tomcat server");
                        try {
                            tomcat.stop();
                        } catch (LifecycleException e) {
                            LOG.error("A lifecycle exception occurred" + e.getMessage());
                        }
                    }

                    @Override
                    public void exitEventOccurred() {

                        stopServerEventRequested();
                        LOG.info("********************* " + SethlansUtils.getString(StringKey.APP_NAME) + " Shutdown" + " *********************");
                        System.exit(0);
                    }
                });
            }
            tomcat.getServer().await();

        } catch (IOException | ServletException | LifecycleException ex) {
            LOG.error(ex.getMessage());
        }

    }
}
