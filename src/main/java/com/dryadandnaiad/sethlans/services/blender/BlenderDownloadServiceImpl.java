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

package com.dryadandnaiad.sethlans.services.blender;

import com.dryadandnaiad.sethlans.domains.blender.BlenderZip;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBinary;
import com.dryadandnaiad.sethlans.domains.database.events.SethlansNotification;
import com.dryadandnaiad.sethlans.enums.NotificationType;
import com.dryadandnaiad.sethlans.services.database.BlenderBinaryDatabaseService;
import com.dryadandnaiad.sethlans.services.notification.SethlansNotificationService;
import com.dryadandnaiad.sethlans.utils.BlenderUtils;
import com.google.common.base.Throwables;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.dryadandnaiad.sethlans.utils.SethlansFileUtils.fileCheckMD5;

/**
 * Created Mario Estrella on 3/24/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class BlenderDownloadServiceImpl implements BlenderDownloadService {
    private static final Logger LOG = LoggerFactory.getLogger(BlenderDownloadServiceImpl.class);
    private BlenderBinaryDatabaseService blenderBinaryDatabaseService;
    @Value("${sethlans.blenderDir}")
    private String downloadLocation;
    private int downloadMirror = 0;
    private boolean atBoot;
    private SethlansNotificationService sethlansNotificationService;

    @Override
    @Async
    public void downloadRequestedBlenderFilesAsync() {
        //noinspection InfiniteLoopStatement
        try {
            Thread.sleep(10000);
            atBoot = true;

            while (true) {
                try {
                    if (doDownload()) {
                        LOG.debug("All downloads complete");
                    } else {
                        LOG.debug("Blender Download Service failed");
                    }
                    atBoot = false;
                    Thread.sleep(120000);
                } catch (InterruptedException e) {
                    LOG.debug("Stopping Blender Binary Download Service");
                    break;
                }
            }
        } catch (InterruptedException e) {
            LOG.debug("Stopping Blender Binary Download Service");
        }
    }

    private boolean doDownload() {
        prepareDownload();
        List<BlenderBinary> blenderDownloadList = blenderBinaryDatabaseService.listAll();
        String blenderVersion;

        for (BlenderBinary blenderBinary : blenderDownloadList) {
            if (!blenderBinary.isDownloaded()) {
                blenderVersion = blenderBinary.getBlenderVersion();
                File saveLocation = new File(downloadLocation + File.separator + "binaries" +
                        File.separator + blenderVersion + File.separator);
                if (atBoot && saveLocation.exists()) {
                    try {
                        FileUtils.deleteDirectory(saveLocation);

                    } catch (IOException e) {
                        LOG.error(e.getMessage());
                    }
                }
                saveLocation.mkdirs();
                URL url;
                HttpURLConnection connection = null;
                String filename;
                File downloadPlaceholder = new File(saveLocation + File.separator + blenderVersion + blenderBinary.getBlenderBinaryOS() + ".pending");

                while (true) {
                    try {
                        if (downloadPlaceholder.exists()) {
                            // This will prevent a download if another async process is downloading the same file.
                            break;
                        }

                        url = new URL(blenderBinary.getDownloadMirrors().get(downloadMirror));
                        LOG.debug("Attempting to establish a connection to " + url.toString());
                        connection = (HttpURLConnection) url.openConnection();
                        InputStream stream = connection.getInputStream();
                        if (connection.getResponseCode() == 200) {
                            LOG.debug("Creating placeholder file" + downloadPlaceholder + " to let service know an active download is in place.");
                            //noinspection ResultOfMethodCallIgnored
                            downloadPlaceholder.createNewFile();
                        }

                        if (blenderBinary.getBlenderFile().contains("tar")) {
                            filename = blenderVersion + "-" +
                                    blenderBinary.getBlenderBinaryOS().toLowerCase() + ".tar." +
                                    com.google.common.io.Files.getFileExtension(blenderBinary.getBlenderFile());
                            LOG.debug("Setting filename to  " + filename);
                        } else {
                            filename = blenderVersion + "-" + blenderBinary.getBlenderBinaryOS().toLowerCase() + "." +
                                    com.google.common.io.Files.getFileExtension(blenderBinary.getBlenderFile());
                            LOG.debug("Setting filename to" + filename);
                        }

                        File toDownload = new File(saveLocation + File.separator + filename);


                        // Send notification of pending download.
                        LOG.info("Downloading " + filename + "...");
                        String blenderFileInfo = "Downloading Blender " + blenderVersion + " for " + blenderBinary.getBlenderBinaryOS();
                        SethlansNotification notification = new SethlansNotification();
                        notification.setMessage(blenderFileInfo);
                        notification.setNotificationType(NotificationType.BLENDER_DOWNLOAD);
                        notification.setLinkPresent(false);
                        notification.setMessageDate(System.currentTimeMillis());
                        sethlansNotificationService.sendNotification(notification);

                        LOG.debug("Saving file to " + toDownload.toString());
                        Files.copy(stream, Paths.get(toDownload.toString()));

                        // Check MD5 sum
                        LOG.debug("Starting MD5sum check");
                        if (fileCheckMD5(toDownload, blenderBinary.getBlenderFileMd5())) {
                            blenderBinary.setBlenderFile(toDownload.toString());
                            LOG.info(filename + " downloaded successfully.");
                            blenderBinary.setDownloaded(true);
                            //noinspection ResultOfMethodCallIgnored
                            downloadPlaceholder.delete();
                            LOG.debug(blenderBinary.toString());
                            blenderBinaryDatabaseService.saveOrUpdate(blenderBinary);
                            break;
                        } else {
                            LOG.error("MD5 sums didn't match, removing file " + filename);
                            //noinspection ResultOfMethodCallIgnored
                            toDownload.delete();
                            //noinspection ResultOfMethodCallIgnored
                            downloadPlaceholder.delete();
                            throw new IOException();
                        }

                    } catch (MalformedURLException e) {
                        LOG.error("Invalid URL: " + e.getMessage());
                        return false;
                    } catch (IOException e) {
                        LOG.error("IO Exception: " + e.getMessage());
                        if (e.getMessage().contains("Connection timed out")) {
                            LOG.error("Connection time out " + blenderBinary.getDownloadMirrors().get(downloadMirror));
                            downloadMirror++;
                        } else {
                            LOG.error(Throwables.getStackTraceAsString(e));
                            return false;
                        }
                    } finally {
                        LOG.debug("Ending connection.");
                        if (connection != null) {
                            connection.disconnect();
                            SethlansNotification notification = new SethlansNotification();
                            notification.setMessage("Blender " + blenderVersion + " download for " + blenderBinary.getBlenderBinaryOS() + " has completed.");
                            notification.setNotificationType(NotificationType.BLENDER_DOWNLOAD);
                            notification.setLinkPresent(false);
                            notification.setMessageDate(System.currentTimeMillis());
                            sethlansNotificationService.sendNotification(notification);
                        }
                    }

                }
            }
        }

        return true;
    }

    private void prepareDownload() {
        List<BlenderZip> blenderBinaries = BlenderUtils.listBinaries();
        List<BlenderBinary> blenderFileEntities = blenderBinaryDatabaseService.listAll();
        LOG.debug("Preparing Blender Binary Download List");
        String filename;
        for (BlenderBinary blenderZipEntity : blenderFileEntities) {
            if (blenderZipEntity.isDownloaded()) {
                File blendFile = new File(blenderZipEntity.getBlenderFile());
                if (!blendFile.exists()) {
                    LOG.debug(blenderZipEntity.getBlenderFile() + " is missing, re-downloading file.");
                    blenderZipEntity.setDownloaded(false);
                    blenderBinaryDatabaseService.saveOrUpdate(blenderZipEntity);
                }
            } else if (!blenderZipEntity.isDownloaded()) {
                for (BlenderZip blenderBinary : blenderBinaries) {
                    if (blenderBinary.getBlenderVersion().equals(blenderZipEntity.getBlenderVersion())) {
                        switch (blenderZipEntity.getBlenderBinaryOS().toLowerCase()) {
                            case "windows64":
                                filename = blenderBinary.getWindows64().get(0);
                                blenderZipEntity.setDownloadMirrors(blenderBinary.getWindows64());
                                blenderZipEntity.setBlenderFile(filename.substring(filename.lastIndexOf("/") + 1));
                                blenderZipEntity.setBlenderFileMd5(blenderBinary.getMd5Windows64());
                                blenderBinaryDatabaseService.saveOrUpdate(blenderZipEntity);
                                break;
                            case "macos":
                                filename = blenderBinary.getMacOS().get(0);
                                blenderZipEntity.setDownloadMirrors(blenderBinary.getMacOS());
                                blenderZipEntity.setBlenderFile(filename.substring(filename.lastIndexOf("/") + 1));
                                blenderZipEntity.setBlenderFileMd5(blenderBinary.getMd5MacOS());
                                blenderBinaryDatabaseService.saveOrUpdate(blenderZipEntity);
                                break;
                            case "linux64":
                                filename = blenderBinary.getLinux64().get(0);
                                blenderZipEntity.setDownloadMirrors(blenderBinary.getLinux64());
                                blenderZipEntity.setBlenderFile(filename.substring(filename.lastIndexOf("/") + 1));
                                blenderZipEntity.setBlenderFileMd5(blenderBinary.getMd5Linux64());
                                blenderBinaryDatabaseService.saveOrUpdate(blenderZipEntity);
                                break;
                            default:
                                LOG.error("Invalid blender binary operating system " + blenderZipEntity.getBlenderBinaryOS());
                        }
                    }
                }
            }
        }
    }

    @Autowired
    public void setBlenderBinaryDatabaseService(BlenderBinaryDatabaseService blenderBinaryDatabaseService) {
        this.blenderBinaryDatabaseService = blenderBinaryDatabaseService;
    }

    @Autowired
    public void setSethlansNotificationService(SethlansNotificationService sethlansNotificationService) {
        this.sethlansNotificationService = sethlansNotificationService;
    }
}
