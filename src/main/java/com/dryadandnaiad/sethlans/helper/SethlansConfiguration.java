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
package com.dryadandnaiad.sethlans.helper;

import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.ConfigKey;
import com.dryadandnaiad.sethlans.enums.LogLevel;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.utils.Resources;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
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
public class SethlansConfiguration {

    private static final Logger logger = LogManager.getLogger(SethlansConfiguration.class);
    private static final String CONFIG_VERSION_ID = "1"; // Used to handle changes to config as versions increase.

    private final String path = System.getProperty("user.home") + File.separator + ".sethlans";
    private final File configDirectory = new File(path + File.separator + "config");
    private final File defaultConfigFile = new File(configDirectory + File.separator + "sethlansconfig.xml");

    private ComputeType computeMethod;
    private boolean firstTime = true;
    private Integer cores;
    private SethlansMode mode;
    private LogLevel logLevel;
    private String httpPort;
    private String httpsPort;
    private static SethlansConfiguration instance = null;

    private SethlansConfiguration() {
        check();
    }

    public static SethlansConfiguration getInstance() {
        if (instance == null) {
            logger.debug("Creating new instance of configuration helper");
            instance = new SethlansConfiguration();
        }
        return instance;
    }

    private void loadConfig() {
        if (!CONFIG_VERSION_ID.equals(getProperty(ConfigKey.CONFIG_VERSION))) {
            logger.info("Old Config version, starting update");
            configUpdate();
            logger.info("Configuration updated to config version " + CONFIG_VERSION_ID);
        } else {
            this.logLevel = LogLevel.valueOf(getProperty(ConfigKey.LOGLEVEL).toUpperCase());
            this.computeMethod = ComputeType.valueOf(getProperty(ConfigKey.COMPUTE_METHOD).toUpperCase());
            this.cores = Integer.parseInt(getProperty(ConfigKey.CORES));
            this.mode = SethlansMode.valueOf(getProperty(ConfigKey.MODE).toUpperCase());
            this.httpPort = getProperty(ConfigKey.HTTP_PORT);
            this.httpsPort = getProperty(ConfigKey.HTTPS_PORT);
            Configurator.setRootLevel(this.logLevel.getLevel());
            logger.debug("Config values loaded");
        }

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

    public void setLoglevel(LogLevel value) {
        setProperty(ConfigKey.LOGLEVEL, value);
        loadConfig();
    }

    public void setHttpPort(String value) {
        setProperty(ConfigKey.HTTP_PORT, value);
        loadConfig();
    }

    public void setHttpsPort(String value) {
        setProperty(ConfigKey.HTTPS_PORT, value);
        loadConfig();
    }

    public ComputeType getComputeMethod() {
        return computeMethod;
    }

    public Integer getCores() {
        return cores;
    }

    public SethlansMode getMode() {
        return mode;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public String getHttpPort() {
        return httpPort;
    }

    public String getHttpsPort() {
        return httpsPort;
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

            properties.setProperty(SethlansUtils.enumToString(key), SethlansUtils.enumToString(value));
            try (FileOutputStream fileOut = new FileOutputStream(defaultConfigFile)) {
                properties.storeToXML(fileOut, SethlansUtils.updaterTimeStamp());
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

            properties.setProperty(SethlansUtils.enumToString(key), value);
            try (FileOutputStream fileOut = new FileOutputStream(defaultConfigFile)) {
                properties.storeToXML(fileOut, SethlansUtils.updaterTimeStamp());
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

            return properties.getProperty(SethlansUtils.enumToString(key));

        } catch (FileNotFoundException e) {
            logger.error(e.getMessage());

        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return null;

    }

    private void configUpdate() {
        //TODO handle upgrades if versions change.
    }

}
