/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC
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
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
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
    private static String key = "gQ5Q5Nxk0qjjxdwTQ$8UVExzO%nOehiF";

    public static File getConfigFile() {
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

    private static String updateComment(String comment, File configFile, Properties sethlansProperties) {
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


    public static String updateTimeStamp() {
        Date currentDate = GregorianCalendar.getInstance().getTime();
        return String.format("Updated: %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", currentDate);
    }

    public static String encryptPropertyValue(String value) {
        try {
            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            byte[] toEncode = cipher.doFinal(value.getBytes());
            byte[] encryptedValue = new Base64().encode(toEncode);
            return new String(encryptedValue, StandardCharsets.UTF_8);
        } catch (NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException | NoSuchPaddingException e) {
            LOG.error(e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return null;
    }

    public static String decryptPropertyValue(String value) {
        try {
            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            byte[] decoded = new Base64().decode(value.getBytes(StandardCharsets.UTF_8));
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted);
        } catch (BadPaddingException | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException e) {
            LOG.error(e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return null;
    }

    public static boolean writePropertyToFile(SethlansConfigKeys configKey, String value, File configFile) {
        Properties sethlansProperties = new Properties();

        String comment = "";
        String key = configKey.toString();
        comment = updateComment(comment, configFile, sethlansProperties);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(configFile);
            sethlansProperties.setProperty(key, value);
            //Save Properties to File
            sethlansProperties.store(fileOutputStream, comment);
            LOG.debug("SethlansConfigKey:" + key + " written to " + configFile.toString());
            return true;
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
        } catch (IOException e) {
            LOG.error(e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return false;
    }

    public static boolean writeProperty(String key, String value) {
        Properties sethlansProperties = new Properties();

        String comment = "";
        comment = updateComment(comment, getConfigFile(), sethlansProperties);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(getConfigFile());
            sethlansProperties.setProperty(key, value);
            //Save Properties to File
            sethlansProperties.store(fileOutputStream, comment);
            LOG.debug("SethlansConfigKey: " + key + " written to " + getConfigFile());
            return true;
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
        } catch (IOException e) {
            LOG.error(e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return false;
    }

    public static boolean writeProperty(SethlansConfigKeys configKey, String value) {
        Properties sethlansProperties = new Properties();

        String comment = "";
        String key = configKey.toString();
        comment = updateComment(comment, getConfigFile(), sethlansProperties);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(getConfigFile());
            sethlansProperties.setProperty(key, value);
            //Save Properties to File
            sethlansProperties.store(fileOutputStream, comment);
            LOG.debug("SethlansConfigKey: " + key + " written to " + getConfigFile().toString());
            return true;
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
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

            return properties.getProperty(key.toString());
        } catch (IOException e) {
            LOG.error("Unable to read config file, either missing or this is a first time execution");

        }
        return null;
    }


    public static String getProperty(SethlansConfigKeys key) {
        final Properties properties = new Properties();
        try {
            FileInputStream fileIn = new FileInputStream(getConfigFile());
            properties.load(fileIn);
            return properties.getProperty(key.toString());
        } catch (IOException e) {
            LOG.error("Unable to read config file, either missing or this is a first time execution");

        }
        return null;
    }

    public static String getProperty(String key) {
        final Properties properties = new Properties();
        try {
            FileInputStream fileIn = new FileInputStream(getConfigFile());
            properties.load(fileIn);
            return properties.getProperty(key);
        } catch (IOException e) {
            LOG.error("Unable to read config file, either missing or this is a first time execution");

        }
        return null;
    }
}
