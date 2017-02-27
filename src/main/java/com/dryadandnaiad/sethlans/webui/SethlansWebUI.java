/*
 * Copyright (C) 2017 Dryad and Naiad Software LLC
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
 */
package com.dryadandnaiad.sethlans.webui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import javax.servlet.ServletException;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceSet;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.EmptyResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.scan.Constants;
import org.apache.tomcat.util.scan.StandardJarScanFilter;

/**
 *
 * @author Mario Estrella <mestrella@dryadandnaiad.com>
 */
public class SethlansWebUI {

    private static final Logger logger = LogManager.getLogger(SethlansWebUI.class);

    private static File getRootFolder() {
        try {
            File root;
            String runningJarPath = SethlansWebUI.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath().replaceAll("\\\\", "/");
            int lastIndexOf = runningJarPath.lastIndexOf("/target/");
            if (lastIndexOf < 0) {
                root = new File("");
            } else {
                root = new File(runningJarPath.substring(0, lastIndexOf));
            }
            System.out.println("application resolved root folder: " + root.getAbsolutePath());
            return root;
        } catch (URISyntaxException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void start() {

        try {
            File root = getRootFolder();
            System.setProperty("org.apache.catalina.startup.EXIT_ON_INIT_FAILURE", "true");
            Tomcat tomcat = new Tomcat();
            Path tempPath = Files.createTempDirectory("tomcat-base-dir");
            tomcat.setBaseDir(tempPath.toString());
            
            String webPort = System.getenv("PORT");
            if (webPort == null || webPort.isEmpty()) {
                webPort = "8448";
            }
            
            
            tomcat.setPort(Integer.valueOf(webPort));
            File webContentFolder = new File(root.getAbsolutePath(), "src/main/webapp/");
            
            if (!webContentFolder.exists()) {
                webContentFolder = Files.createTempDirectory("default-doc-base").toFile();
            }
            
            StandardContext ctx = (StandardContext) tomcat.addWebapp("", webContentFolder.getAbsolutePath());
            //Set execution independent of current thread context classloader (compatibility with exec:java mojo)
            ctx.setParentClassLoader(SethlansWebUI.class.getClassLoader());
            
            //Disable TLD scanning by default
            if (System.getProperty(Constants.SKIP_JARS_PROPERTY) == null && System.getProperty(Constants.SKIP_JARS_PROPERTY) == null) {
                System.out.println("disabling TLD scanning");
                StandardJarScanFilter jarScanFilter = (StandardJarScanFilter) ctx.getJarScanner().getJarScanFilter();
                jarScanFilter.setTldSkip("*");
            }
            
            System.out.println("configuring app with basedir: " + webContentFolder.getAbsolutePath());
            
            // Declare an alternative location for your "WEB-INF/classes" dir
            // Servlet 3.0 annotation will work
            File additionWebInfClassesFolder = new File(root.getAbsolutePath(), "target/classes");
            WebResourceRoot resources = new StandardRoot(ctx);
            
            WebResourceSet resourceSet;
            if (additionWebInfClassesFolder.exists()) {
                resourceSet = new DirResourceSet(resources, "/WEB-INF/classes", additionWebInfClassesFolder.getAbsolutePath(), "/");
                System.out.println("loading WEB-INF resources from as '" + additionWebInfClassesFolder.getAbsolutePath() + "'");
            } else {
                resourceSet = new EmptyResourceSet(resources);
            }
            resources.addPreResources(resourceSet);
            ctx.setResources(resources);
            
            tomcat.start();
            tomcat.getServer().await();
        } catch (IOException | ServletException | LifecycleException ex) {
            logger.error(ex.getMessage());
        }

    }

}
