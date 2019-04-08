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

package com.dryadandnaiad.sethlans.utils;

import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

/**
 * Created Mario Estrella on 8/23/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class SethlansConfigUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SethlansConfigUtils.class);
    private static Properties sethlansProperties = new Properties();


    static File getConfigFile() {
        try {
            File configDirectory = new File(System.getProperty("user.home") + File.separator + ".sethlans_install" + File.separator + "config" + File.separator);
            File installFile = new File(configDirectory + File.separator + "sethlans_install.properties");
            configDirectory = new File(getProperty(SethlansConfigKeys.CONFIG_DIR, installFile));
            File configFile = new File(configDirectory + File.separator + "sethlans.properties");
            if (configFile.exists()) {
                return configFile;
            }

        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return new File(System.getProperty("user.home") + File.separator + ".sethlans_install" + File.separator + "config" + File.separator + "sethlans_install.properties");
    }

    private static String updateComment(String comment, File configFile) {
        if (configFile.exists()) {
            try (FileInputStream fileIn = new FileInputStream(getConfigFile())) {
                sethlansProperties.load(fileIn);
                comment = updateTimeStamp();
            } catch (IOException e) {
                LOG.error(Throwables.getStackTraceAsString(e));
            }
        }
        return comment;
    }


    private static String updateTimeStamp() {
        Date currentDate = GregorianCalendar.getInstance().getTime();
        return String.format("Updated: %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", currentDate);
    }


    public static boolean writePropertyToFile(SethlansConfigKeys configKey, String value, File configFile) {
        String comment = "";
        String key = configKey.toString();
        comment = updateComment(comment, configFile);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(configFile);
            sethlansProperties.setProperty(key, value);
            //Save Properties to File
            sethlansProperties.store(fileOutputStream, comment);
            LOG.debug("SethlansConfigKey:" + key + " written to " + configFile.toString());
            return true;
        } catch (IOException e) {
            LOG.error(e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return false;
    }

    public static boolean writeProperty(String key, String value) {
        String comment = "";
        comment = updateComment(comment, getConfigFile());
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(getConfigFile());
            sethlansProperties.setProperty(key, value);
            //Save Properties to File
            sethlansProperties.store(fileOutputStream, comment);
            LOG.debug("SethlansConfigKey: " + key + " written to " + getConfigFile());
            return true;
        } catch (IOException e) {
            LOG.error(e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return false;
    }

    public static boolean writeProperty(SethlansConfigKeys configKey, String value) {
        String comment = "";
        String key = configKey.toString();
        comment = updateComment(comment, getConfigFile());

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(getConfigFile());
            sethlansProperties.setProperty(key, value);
            //Save Properties to File
            sethlansProperties.store(fileOutputStream, comment);
            LOG.debug("SethlansConfigKey: " + key + " written to " + getConfigFile().toString());
            return true;
        } catch (IOException e) {
            LOG.error(e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return false;
    }

    public static String getProperty(SethlansConfigKeys key, File installFile) {
        final Properties properties = new Properties();
        try {
            FileInputStream fileIn = new FileInputStream(installFile);
            properties.load(fileIn);
            fileIn.close();
            return properties.getProperty(key.toString());
        } catch (IOException e) {
            LOG.error("Unable to read config file, either missing or this is a first time execution");

        }
        return null;
    }


    public static String getProperty(SethlansConfigKeys key) {
        return getProperty(key.toString());
    }

    public static String getProperty(String key) {
        final Properties properties = new Properties();
        try {
            FileInputStream fileIn = new FileInputStream(getConfigFile());
            properties.load(fileIn);
            fileIn.close();
            return properties.getProperty(key);
        } catch (IOException e) {
            LOG.error("Unable to read config file, either missing or this is a first time execution");

        }
        return null;
    }
}
