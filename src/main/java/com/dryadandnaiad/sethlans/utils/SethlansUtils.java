/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC.
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

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBinary;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.google.common.base.Throwables;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.lang3.SystemUtils;
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
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
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


    public static boolean archiveExtract(String toExtract, File extractLocation) {
        File archive = new File(extractLocation + File.separator + toExtract);
        extractLocation.mkdirs();
        try {
            if (archive.toString().contains("txz")) {
                Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.XZ);
                archiver.extract(archive, extractLocation);
                archive.delete();
                return true;
            }
            if (archive.toString().contains("tar.gz")) {
                Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);
                archiver.extract(archive, extractLocation);
                archive.delete();
                return true;
            } else {
                ZipFile archiver = new ZipFile(archive);
                LOG.debug("Extracting " + archive + " to " + extractLocation);
                archiver.extractAll(extractLocation.toString());
                archive.delete();
                return true;
            }

        } catch (ZipException | IOException e) {
            LOG.error("Error extracting archive " + e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
            System.exit(1);
        }

        return false;
    }


    public static void openWebpage(URL url) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(url.toURI());
            } catch (Exception e) {
                LOG.error("Unable to Open Web page" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static String getHostname() {
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        int iend = hostname.indexOf(".");
        if (iend != -1) {
            LOG.debug(hostname + " contains a domain name. Removing it.");
            hostname = hostname.substring(0, iend);
        }
        return hostname;
    }

    public static String getIP() {
        String ip = null;
        try {
            if (SystemUtils.IS_OS_LINUX) {
                // Make a connection to 8.8.8.8 DNS in order to get IP address
                Socket s = new Socket("8.8.8.8", 53);
                ip = s.getLocalAddress().getHostAddress();
                s.close();
            } else {
                ip = InetAddress.getLocalHost().getHostAddress();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ip;
    }

    public static boolean getFirstTime() {
        boolean firsttime = true;
        final Properties properties = new Properties();
        try {
            if (configFile.exists()) {
                FileInputStream fileIn = new FileInputStream(configFile);
                properties.load(fileIn);
            } else {
                properties.load(new InputStreamReader(new Resources("sethlans.properties").getResource(), "UTF-8"));
            }
            firsttime = Boolean.parseBoolean(properties.getProperty("sethlans.firsttime"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return firsttime;
    }

    public static String getPort() {
        String port = null;
        final Properties properties = new Properties();
        try {
            if (configFile.exists()) {
                FileInputStream fileIn = new FileInputStream(configFile);
                properties.load(fileIn);
            } else {
                properties.load(new InputStreamReader(new Resources("sethlans.properties").getResource(), "UTF-8"));
            }
            port = properties.getProperty("server.port");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return port;
    }

    public static String getOS() {
        if (SystemUtils.IS_OS_WINDOWS) {
            String arch = System.getenv("PROCESSOR_ARCHITECTURE");
            String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

            String realArch = arch.endsWith("64")
                    || wow64Arch != null && wow64Arch.endsWith("64")
                    ? "64" : "32";
            if (realArch.equals("64")) {
                return "Windows64";
            } else {
                return "Windows32";
            }
        }
        if (SystemUtils.IS_OS_MAC) {
            return "MacOS";
        }
        if (SystemUtils.IS_OS_LINUX) {
            if (SystemUtils.OS_ARCH.contains("64")) {
                return "Linux64";
            } else {
                return "Linux32";
            }
        }
        return null;
    }

    public static String getVersion() {
        String version = null;

        final Properties properties = new Properties();
        try {
            properties.load(new InputStreamReader(new Resources("git.properties").getResource(), "UTF-8"));
            String buildNumber = String.format("%04d", Integer.parseInt(properties.getProperty("git.closest.tag.commit.count")));
            version = properties.getProperty("git.build.version") + "." + buildNumber;
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (version == null) {
            // we could not compute the version so use a blank
            version = "";
        }

        return version;
    }


}
