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

package com.dryadandnaiad.sethlans.utils;

import com.dryadandnaiad.sethlans.domains.blender.BlenderBinary;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.google.common.base.Throwables;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

/**
 * Created Mario Estrella on 3/9/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class SethlansUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SethlansUtils.class);
    private static final String path = System.getProperty("user.home") + File.separator + ".sethlans";
    private static final File configDirectory = new File(path + File.separator + "config");
    private static final File configFile = new File(configDirectory + File.separator + "sethlans.properties");
    private static Properties sethlansProperties = new Properties();

    public static Image createImage(String image, String description) {
        URL imageURL = null;
        try {
            imageURL = new ClassPathResource(image).getURL();
        } catch (IOException e) {
            LOG.error("Failed Creating Image. Resource not found.\n" + e.getMessage());
            System.exit(1);
        }
        return new ImageIcon(imageURL, description).getImage();
    }

    public static void openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                LOG.error("Unable to Open Web page" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void openWebpage(URL url) {
        try {
            openWebpage(url.toURI());
        } catch (URISyntaxException e) {
            LOG.error("Unable to Open Web page" + e.getMessage());
        }
    }

    public static boolean writeProperty(SethlansConfigKeys configKey, String value) {
        String comment = "";
        String key = configKey.toString();
        if (configFile.exists()) {
            try (FileInputStream fileIn = new FileInputStream(configFile)) {
                sethlansProperties.load(fileIn);
                comment = updateTimeStamp();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(configFile);
            sethlansProperties.setProperty(key, value);
            //Save Properties to File
            sethlansProperties.store(fileOutputStream, comment);
            LOG.debug("SethlansConfigKey:" + key + " written to " + configFile);
            return true;
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static boolean writeProperty(String key, String value) {
        String comment = "";
        if (configFile.exists()) {
            try (FileInputStream fileIn = new FileInputStream(configFile)) {
                sethlansProperties.load(fileIn);
                comment = updateTimeStamp();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(configFile);
            sethlansProperties.setProperty(key, value);
            //Save Properties to File
            sethlansProperties.store(fileOutputStream, comment);
            LOG.debug(key + " written to " + configFile);
            return true;
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public static String updateTimeStamp() {
        Date currentDate = GregorianCalendar.getInstance().getTime();
        return String.format("Updated: %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", currentDate);
    }

    public static boolean fileCheckMD5(File file, String md5) throws IOException {
        HashCode hash = Files.hash(file, Hashing.md5());
        LOG.debug("Hash md5: " + hash.toString() + "\n JSON md5: " + md5);
        if (hash.toString().equals(md5)) {
            return true;
        }
        return false;
    }

    public static boolean blenderExtract(BlenderBinary toExtract, File extractLocation, String alternateLocation) {
        File archive = new File(toExtract.getBlenderFile());
        if (!toExtract.getBlenderBinaryOS().contains("Linux")) {
            extractLocation.mkdirs();
            LOG.debug("Found zip file:");
            try {
                ZipFile archiver = new ZipFile(archive);
                LOG.debug("Extracting " + archive + " to " + extractLocation);
                archiver.extractAll(extractLocation.toString());
            } catch (ZipException e) {
                LOG.error("Error extracting using zip4j " + e.getMessage());
                LOG.error(Throwables.getStackTraceAsString(e));
                System.exit(1);
            }
            return true;
        }
        Archiver archiver;
        archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.BZIP2);
        LOG.debug("Found tar.bz2 file.");
        File linuxLocation = new File(alternateLocation);

        try {
            LOG.debug("Extracting " + archive + " to " + linuxLocation);
            archiver.extract(archive, linuxLocation);

        } catch (IOException e) {
            LOG.error("Error during extraction using jarchivelib " + e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
            System.exit(1);

        }
        return false;
    }

    public static boolean pythonExtract(String toExtract, File extractLocation) {
        File archive = new File(extractLocation + File.separator + toExtract);
        extractLocation.mkdirs();
        try {
            ZipFile archiver = new ZipFile(archive);
            LOG.debug("Extracting " + archive + " to " + extractLocation);
            archiver.extractAll(extractLocation.toString());
            archive.delete();
            return true;
        } catch (ZipException e) {
            LOG.error("Error extracting using zip4j " + e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
            System.exit(1);
        }
        return false;
    }

}
