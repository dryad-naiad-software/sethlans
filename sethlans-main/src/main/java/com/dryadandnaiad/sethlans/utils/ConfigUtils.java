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

package com.dryadandnaiad.sethlans.utils;

import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

/**
 * Created by Mario Estrella on 4/20/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class ConfigUtils {

    private static String updateTimeStamp() {
        Date currentDate = GregorianCalendar.getInstance().getTime();
        return String.format("Updated: %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", currentDate);
    }


    /**
     * Retrieves the Sethlans configuration file, if it doesn't exist, it will be created.
     *
     * @return sethlans configuration file
     */
    static File getConfigFile() {
        var configDirSuccess = false;
        val configDirectory = new File(SystemUtils.USER_HOME + File.separator + ".sethlans" + File.separator + "config" + File.separator);
        val configFile = new File(configDirectory + File.separator + "sethlans.properties");
        try {
            if (!configFile.exists()) {
                log.debug("sethlans.properties file doesn't exist, creating a new file. " + configFile.toString());
                configDirSuccess = configDirectory.mkdirs();
                configFile.createNewFile();
            }

        } catch (IOException e) {
            log.error(e.getMessage());
            if (!configDirSuccess) {
                log.error("Unable to create config directory");
            }
            log.error("Unable to write config file");
        }
        return configFile;
    }

    /**
     * Writes a property value to sethlans.properties file
     *
     * @param configKey Sethlans configuration key
     * @param value     value to store
     * @return true is write is successful
     */
    public static boolean writeProperty(ConfigKeys configKey, String value) {
        val comment = updateTimeStamp();
        val key = configKey.toString();
        val sethlansProperties = new Properties();

        try {
            val fileOutputStream = new FileOutputStream(getConfigFile());
            sethlansProperties.setProperty(key, value);
            //Save Properties to File
            sethlansProperties.store(fileOutputStream, comment);
            log.debug("SethlansConfigKey: " + key + " written to " + getConfigFile().toString());
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
        }
        return false;
    }

    /**
     * Retrieves a property from the Sethlans configuration file
     *
     * @param configKey Sethlans configuration key
     * @return String value of property
     */
    public static String getProperty(ConfigKeys configKey) {
        val key = configKey.toString();

        val properties = new Properties();
        try {
            // Try to get property from local file system first
            val fileIn = new FileInputStream(getConfigFile());
            properties.load(fileIn);
            fileIn.close();
            val value = properties.getProperty(key);
            if (value != null) {
                log.debug(value);
                return value;
            } else {
                // Obtain value from built in sethlans.properties file(defaults)
                loadFromResource(properties);
                return properties.getProperty(key);
            }

        } catch (
                IOException e) {
            log.error("Unable to read config file!");
            log.error(Throwables.getStackTraceAsString(e));

        }
        return null;
    }

    private static void loadFromResource(Properties properties) throws IOException {
        properties.load(new InputStreamReader(new Resources("sethlans.properties").getResource(),
                StandardCharsets.UTF_8));
    }


}
