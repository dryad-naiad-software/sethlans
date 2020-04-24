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
import org.springframework.core.io.ClassPathResource;

import javax.swing.*;
import javax.xml.bind.DatatypeConverter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Created by Mario Estrella on 4/23/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class SethlansFileUtils {

    public static boolean isDirectoryEmpty(File directory) {
        if (directory.isDirectory()) {
            String[] files = directory.list();
            return files == null || files.length <= 0;
        }
        return true;
    }

    public static boolean fileCheckMD5(File file, String md5) throws IOException, NoSuchAlgorithmException {
        String hashValue = getMD5ofFile(file);
        log.debug("Current file md5: " + hashValue + " Submitted md5: " + md5);
        return hashValue.equals(md5);
    }

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

    public static Image createImage(String image, String description) {
        URL imageURL = null;
        try {
            imageURL = new ClassPathResource(image).getURL();
        } catch (IOException e) {
            log.error("Failed Creating Image. Resource not found. " + e.getMessage());
            System.exit(1);
        }
        return new ImageIcon(imageURL, description).getImage();
    }

    public static File createArchive(List<String> files, String rootDir, String zipFileName) {
        File createdArchive = null;
        try {
            if (!new File(rootDir + File.separator + zipFileName + ".zip").exists()) {
                log.debug("Creating Archive " + zipFileName);
                ZipFile zipFile = new ZipFile(rootDir + File.separator + zipFileName + ".zip");
                ZipParameters parameters = new ZipParameters();
                parameters.setCompressionMethod(CompressionMethod.DEFLATE);
                parameters.setCompressionLevel(CompressionLevel.MAXIMUM);
                for (String frameFileName : files) {
                    File frame = new File(frameFileName);
                    zipFile.addFile(frame, parameters);
                }
                createdArchive = new File(rootDir + File.separator + zipFileName + ".zip");
            } else {
                createdArchive = new File(rootDir + File.separator + zipFileName + ".zip");
            }

        } catch (ZipException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
        }
        return createdArchive;
    }
}
