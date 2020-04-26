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

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import org.apache.commons.lang3.SystemUtils;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;
import org.springframework.core.io.ClassPathResource;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import javax.swing.*;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * File created by Mario Estrella on 4/23/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class SethlansFileUtils {

    /**
     * Checks if a directory is empty
     *
     * @param directory
     * @return boolean
     * @throws FileNotFoundException
     */
    public static boolean isDirectoryEmpty(File directory) throws FileNotFoundException {
        if (!directory.isDirectory()) {
            throw new FileNotFoundException("This is not a valid directory");
        }
        String[] files = directory.list();
        return files == null || files.length <= 0;
    }

    /**
     * Verify that the file and the MD5 hash provided match.
     *
     * @param file
     * @param md5
     * @return boolean
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static boolean fileCheckMD5(File file, String md5) throws IOException, NoSuchAlgorithmException {
        String hashValue = getMD5ofFile(file);
        log.debug("Current file md5: " + hashValue + " Submitted md5: " + md5);
        return hashValue.equals(md5);
    }

    /**
     * Retrieve the MD5 hash of a file
     *
     * @param file
     * @return String
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    public static String getMD5ofFile(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        InputStream inputStream = Files.newInputStream(Paths.get(file.toString()));
        DigestInputStream digestInputStream = new DigestInputStream(inputStream, md);
        byte[] buffer = new byte[4096];
        while (true) {
            if (digestInputStream.read(buffer) <= -1) break;
        }
        inputStream.close();

        return DatatypeConverter.printHexBinary(md.digest()).toLowerCase();
    }

    /**
     * Returns an Image object from the disk
     *
     * @param image
     * @param description
     * @return
     */
    public static Image createImageIcon(String image, String description) {
        URL imageURL = null;
        try {
            imageURL = new ClassPathResource(image).getURL();
        } catch (IOException e) {
            log.error("Failed Creating Image Icon. Resource not found. " + e.getMessage());
            System.exit(1);
        }
        return new ImageIcon(imageURL, description).getImage();
    }

    /**
     * @param files
     * @param rootDir
     * @param zipFileName
     * @return
     */
    public static File createZipArchive(List<String> files, String rootDir, String zipFileName) {
        File createdArchive = null;
        try {
            if (!new File(rootDir + File.separator + zipFileName + ".zip").exists()) {
                log.debug("Creating Zip Archive " + zipFileName);
                ZipFile zipFile = new ZipFile(rootDir + File.separator + zipFileName + ".zip");
                ZipParameters parameters = new ZipParameters();
                parameters.setCompressionMethod(CompressionMethod.DEFLATE);
                parameters.setCompressionLevel(CompressionLevel.MAXIMUM);
                for (String filename : files) {
                    File file = new File(filename);
                    zipFile.addFile(file, parameters);
                }
            } else {
                log.info("Zip Archive " + zipFileName + " already exists");
            }
            createdArchive = new File(rootDir + File.separator + zipFileName + ".zip");

        } catch (ZipException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
        }
        return createdArchive;
    }

    /**
     * Extracts xz, bzip, gzip and zip files.
     * Supports files with extension txz, tar.gz, tar.bz2 and zip
     *
     * @param toExtract
     * @param extractLocation
     * @param deleteArchive
     * @return boolean
     */
    public static boolean extractArchive(File toExtract, File extractLocation, boolean deleteArchive) {
        try {
            if (toExtract.toString().contains(".txz") || toExtract.toString().contains(".tar.xz")) {
                extractLocation.mkdirs();
                log.debug("Extracting " + toExtract + " to " + extractLocation);
                Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.XZ);
                archiver.extract(toExtract, extractLocation);
                if (deleteArchive) {
                    toExtract.delete();
                }
                return true;
            }
            if (toExtract.toString().contains(".tar.gz")) {
                extractLocation.mkdirs();
                log.debug("Extracting " + toExtract + " to " + extractLocation);
                Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);
                archiver.extract(toExtract, extractLocation);
                if (deleteArchive) {
                    toExtract.delete();
                }
                return true;
            }
            if (toExtract.toString().contains(".tar.bz2")) {
                extractLocation.mkdirs();
                log.debug("Extracting " + toExtract + " to " + extractLocation);
                Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.BZIP2);
                archiver.extract(toExtract, extractLocation);
                if (deleteArchive) {
                    toExtract.delete();
                }
                return true;
            }
            if (toExtract.toString().contains(".zip")) {
                extractLocation.mkdirs();
                ZipFile archiver = new ZipFile(toExtract);
                log.debug("Extracting " + toExtract + " to " + extractLocation);
                archiver.extractAll(extractLocation.toString());
                if (deleteArchive) {
                    toExtract.delete();
                }

                return true;
            } else {
                log.error("Unsupported archive extension/format provided.");
            }

        } catch (IOException e) {
            log.error("Error extracting archive " + e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            System.exit(1);
        }

        return false;
    }

    /**
     * @param dmgFile
     * @return
     */
    public static boolean extractBlenderFromDMG(String dmgFile, String destination) {
        if (SystemUtils.IS_OS_MAC) {
            try {
                int exit = new ProcessExecutor().command("hdiutil", "mount", dmgFile)
                        .redirectOutput(Slf4jStream.ofCaller().asInfo()).execute().getExitValue();
                if (exit > 0) {
                    return false;
                }
                exit = new ProcessExecutor().command("cp", "-R", "/Volumes/Blender/Blender.app", destination)
                        .redirectOutput(Slf4jStream.ofCaller().asInfo()).execute().getExitValue();
                return exit <= 0;
            } catch (IOException | InterruptedException | TimeoutException e) {
                log.error(e.getMessage());
                log.error(Throwables.getStackTraceAsString(e));
            }
        }
        return false;
    }

}
