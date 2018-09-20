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

import com.google.common.base.Throwables;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.List;

/**
 * Created Mario Estrella on 8/23/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class SethlansFileUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SethlansFileUtils.class);

    public static void serveFile(File file, HttpServletResponse response) {
        try {
            String mimeType = "application/octet-stream";
            response.setContentType(mimeType);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
            response.setContentLength((int) file.length());
            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
            FileCopyUtils.copy(inputStream, response.getOutputStream());

        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    public static boolean isDirectoryEmpty(File directory) {
        if (directory.isDirectory()) {
            String[] files = directory.list();
            return files == null || files.length <= 0;
        }
        return true;
    }

    public static boolean fileCheckMD5(File file, String md5) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        String hashValue = DigestUtils.md5Hex(IOUtils.toByteArray(fileInputStream));
        LOG.debug("Current file md5: " + hashValue + " Submitted md5: " + md5);
        return hashValue.equals(md5);
    }

    public static String getMD5ofFile(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        return DigestUtils.md5Hex(IOUtils.toByteArray(fileInputStream));

    }

    public static Image createImage(String image, String description) {
        URL imageURL = null;
        try {
            imageURL = new ClassPathResource(image).getURL();
        } catch (IOException e) {
            LOG.error("Failed Creating Image. Resource not found. " + e.getMessage());
            System.exit(1);
        }
        return new ImageIcon(imageURL, description).getImage();
    }

    public static File createArchive(List<String> files, String rootDir, String zipFileName) {
        File createdArchive = null;
        try {
            if (!new File(rootDir + File.separator + zipFileName + ".zip").exists()) {
                LOG.debug("Creating Archive " + zipFileName);
                ZipFile zipFile = new ZipFile(rootDir + File.separator + zipFileName + ".zip");
                ZipParameters parameters = new ZipParameters();
                parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
                parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
                for (String frameFileName : files) {
                    File frame = new File(frameFileName);
                    zipFile.addFile(frame, parameters);
                }
                createdArchive = new File(rootDir + File.separator + zipFileName + ".zip");
            } else {
                createdArchive = new File(rootDir + File.separator + zipFileName + ".zip");
            }

        } catch (ZipException e) {
            LOG.error(e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return createdArchive;
    }

    /**
     * @param toExtract       Filename to extract
     * @param extractLocation Location of filename
     * @return
     */
    public static boolean archiveExtract(String toExtract, File extractLocation, boolean deleteArchive) {
        File archive = new File(extractLocation + File.separator + toExtract);
        try {
            if (archive.toString().contains("txz")) {
                extractLocation.mkdirs();
                LOG.debug("Extracting " + archive + " to " + extractLocation);
                Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.XZ);
                archiver.extract(archive, extractLocation);
                if (deleteArchive) {
                    archive.delete();
                }
                return true;
            }
            if (archive.toString().contains("tar.gz")) {
                extractLocation.mkdirs();
                LOG.debug("Extracting " + archive + " to " + extractLocation);
                Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);
                archiver.extract(archive, extractLocation);
                if (deleteArchive) {
                    archive.delete();
                }
                return true;
            }
            if (archive.toString().contains("tar.bz2")) {
                extractLocation.mkdirs();
                LOG.debug("Extracting " + archive + " to " + extractLocation);
                Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.BZIP2);
                archiver.extract(archive, extractLocation);
                if (deleteArchive) {
                    archive.delete();
                }
                return true;
            } else {
                extractLocation.mkdirs();
                ZipFile archiver = new ZipFile(archive);
                LOG.debug("Extracting " + archive + " to " + extractLocation);
                archiver.extractAll(extractLocation.toString());
                if (deleteArchive) {
                    archive.delete();
                }

                return true;
            }

        } catch (ZipException | IOException e) {
            LOG.error("Error extracting archive " + e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
            System.exit(1);
        }

        return false;
    }
}
