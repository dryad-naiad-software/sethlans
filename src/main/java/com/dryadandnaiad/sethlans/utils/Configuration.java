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
import java.nio.file.NoSuchFileException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

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
    private LogLevel logLevel;

    private Configuration() {
        check();
    }
    
    public static Configuration getInstance() {
        return ConfigurationHolder.INSTANCE;
    }
    
    private static class ConfigurationHolder {
        private static final Configuration INSTANCE = new Configuration();
    }

    public void setComputeMethod(ComputeType value) {
        setProperty(ConfigKey.COMPUTE_METHOD, value);
        loadConfig();
    }

    public void setCores(int value) {
        setProperty(ConfigKey.CORES, Integer.toString(this.cores));
        loadConfig();
    }

    public void setMode(SethlansMode value) {
        setProperty(ConfigKey.MODE, value);
        loadConfig();
    }

    public void setUi_type(UIType value) {
        setProperty(ConfigKey.UITYPE, value);
        loadConfig();
    }

    public void setLoglevel(LogLevel value) {
        setProperty(ConfigKey.LOGLEVEL, value);
        loadConfig();
    }

    private void check() {
        if (defaultConfigFile.isFile()) {
            firstTime = false;
            loadConfig();
            logger.debug("Configuration exists");
        }

        if (firstTime) {
            logger.debug("No configuration present, setting default config file");
            setup();
        }
    }

    private void loadConfig() {
        this.logLevel = LogLevel.valueOf(getProperty(ConfigKey.LOGLEVEL).toUpperCase());
        this.ui_type = UIType.valueOf(getProperty(ConfigKey.UITYPE).toUpperCase());
        Configurator.setRootLevel(this.logLevel.getLevel());
    }

    private void setup() {
        configDirectory.mkdirs();
        initialConfigFile();

    }

    private void initialConfigFile() {
        
        try {
            BufferedWriter writer;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new Resources("defaultconfig/default_config.xml").getResource(), "UTF-8"))) {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(defaultConfigFile), "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                }
            }
            writer.close();
        } catch (NoSuchFileException | UnsupportedEncodingException | FileNotFoundException ex) {
            logger.error(ex.getMessage());
        } catch (IOException ex) {
            logger.error(ex.getMessage());
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

    private String getProperty(Enum key) {
        try {
            Properties properties = new Properties();
            try (FileInputStream fileIn = new FileInputStream(defaultConfigFile)) {
                properties.loadFromXML(fileIn);
            }

            return properties.getProperty(ProjectUtils.enumToString(key));

        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return null;

    }

}
