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
package com.dryadandnaiad.sethlans.utils;

import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.ConfigKey;
import com.dryadandnaiad.sethlans.enums.LogLevel;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.enums.UIType;
import java.io.*;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Mario Estrella <mestrella@dryadandnaiad.com>
 */
public class Configuration {

    private static final Logger logger = LogManager.getLogger(Configuration.class);

    private final String path = System.getProperty("user.home") + File.separator + ".sethlans";
    private final File configDirectory = new File(path + File.separator + "config");
    private final File defaultConfigFile = new File(configDirectory + File.separator + "sethlansconfig.xml");

    private ComputeType computeMethod;
    private boolean firstTime = true;
    private int cores;
    private SethlansMode mode;
    private UIType ui_type;
    private LogLevel loglevel = LogLevel.INFO;

    public Configuration() {
        check();
    }

    public void setComputeMethod(ComputeType computeMethod) {
        this.computeMethod = computeMethod;
        setProperty(ConfigKey.COMPUTE_METHOD, this.computeMethod);
    }

    public void setCores(int cores) {
        this.cores = cores;
        setProperty(ConfigKey.CORES, Integer.toString(this.cores));
    }

    public void setMode(SethlansMode mode) {
        this.mode = mode;
        setProperty(ConfigKey.MODE, this.mode);
    }

    public void setUi_type(UIType ui_type) {
        this.ui_type = ui_type;
        setProperty(ConfigKey.UITYPE, this.ui_type);
    }

    public void setLoglevels(LogLevel loglevel) {
        this.loglevel = loglevel;
        setProperty(ConfigKey.LOGLEVEL, this.loglevel);
    }

    private void check() {
        if (defaultConfigFile.isFile()) {
            firstTime = false;
            logger.debug("Configuration exists");
        }

        if (firstTime) {
            logger.debug("No configuration present, setting up initial structure");
            setup();
        }
    }

    private void setup() {
        configDirectory.mkdirs();
        initialConfigFile();
    }

    private void initialConfigFile() {

        try {
            Properties properties = new Properties();
            properties.setProperty(ProjectUtils.enumToString(ConfigKey.LOGLEVEL), ProjectUtils.enumToString(loglevel));
            try (FileOutputStream fileOut = new FileOutputStream(defaultConfigFile)) {
                properties.storeToXML(fileOut, ProjectUtils.updaterTimeStamp());
            }

        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void setProperty(Enum key, Enum value) {
        try {
            Properties properties = new Properties();
            try (FileInputStream fileIn = new FileInputStream(defaultConfigFile)) {
                properties.loadFromXML(fileIn);
            }

            properties.setProperty(ProjectUtils.enumToString(key), ProjectUtils.enumToString(value));
            try (FileOutputStream fileOut = new FileOutputStream(defaultConfigFile)) {
                properties.storeToXML(fileOut, ProjectUtils.updaterTimeStamp());
            }

        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
    
        private void setProperty(Enum key, String value) {
        try {
            Properties properties = new Properties();
            try (FileInputStream fileIn = new FileInputStream(defaultConfigFile)) {
                properties.loadFromXML(fileIn);
            }

            properties.setProperty(ProjectUtils.enumToString(key), value);
            try (FileOutputStream fileOut = new FileOutputStream(defaultConfigFile)) {
                properties.storeToXML(fileOut, ProjectUtils.updaterTimeStamp());
            }

        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void getProperty() {

    }

}
