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

package com.dryadandnaiad.sethlans.services.network;

import com.dryadandnaiad.sethlans.domains.blender.BlenderZip;
import com.dryadandnaiad.sethlans.domains.blender.BlenderZipEntity;
import com.dryadandnaiad.sethlans.services.database.BlenderZipService;
import com.dryadandnaiad.sethlans.utils.BlenderUtils;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created Mario Estrella on 3/24/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class BlenderDownloadServiceImpl implements BlenderDownloadService {
    private static final Logger LOG = LoggerFactory.getLogger(BlenderDownloadServiceImpl.class);
    private BlenderZipService blenderZipService;
    private List<BlenderZipEntity> blenderFileEntities;
    private List<BlenderZip> blenderBinaries;
    @Value("${sethlans.blenderDir}")
    private String downloadLocation;
    private int downloadMirror = 0;

    @Autowired
    public void setBlenderZipService(BlenderZipService blenderZipService) {
        this.blenderZipService = blenderZipService;
    }

    @Override
    @Async
    public Future<Boolean> downloadRequestedBlenderFilesAsync() {
        if (doDownload()) {
            return new AsyncResult<>(true);
        } else {
            return new AsyncResult<>(false);
        }
    }

    private boolean doDownload() {
        blenderFileEntities = (List<BlenderZipEntity>) blenderZipService.listAll();
        blenderBinaries = BlenderUtils.listBinaries();
        List<BlenderZipEntity> blenderDownloadList = prepareDownload();


        for (BlenderZipEntity blenderZipEntity : blenderDownloadList) {
            String blenderVersion = blenderZipEntity.getBlenderVersion();
            File saveLocation = new File(downloadLocation + File.separator + "binaries" + File.separator + blenderVersion + File.separator);
            saveLocation.mkdirs();
            URL url;
            HttpURLConnection connection = null;
            String filename = null;
            try {

                url = new URL(blenderZipEntity.getDownloadMirrors().get(downloadMirror));
                LOG.debug("Attempting to establish a connection to " + url.toString());
                connection = (HttpURLConnection) url.openConnection();
                InputStream stream = connection.getInputStream();
                if (!blenderZipEntity.getBlenderBinaryOS().contains("Linux")) {
                    filename = blenderVersion + "-" + blenderZipEntity.getBlenderBinaryOS().toLowerCase() + ".zip";
                    LOG.debug(filename);
                } else {
                    filename = blenderVersion + "-" + blenderZipEntity.getBlenderBinaryOS().toLowerCase() + ".tar.bz2";
                }

                File toDownload = new File(saveLocation + File.separator + filename);
                LOG.debug("Downloading " + filename + "...");
                if (toDownload.exists()) {
                    LOG.debug("Previous download did not complete successfully, deleting and re-downloading.");
                    toDownload.delete();
                    LOG.debug("Re-Downloading " + filename + "...");
                    Files.copy(stream, Paths.get(toDownload.toString()));
                } else {
                    Files.copy(stream, Paths.get(toDownload.toString()));
                }

                if (SethlansUtils.fileCheckMD5(toDownload, blenderZipEntity.getBlenderFileMd5())) {
                    blenderZipEntity.setBlenderFile(toDownload.toString());
                    LOG.debug(filename + " downloaded successfully.");
                    blenderZipEntity.setDownloaded(true);
                    blenderZipService.saveOrUpdate(blenderZipEntity);
                } else {
                    LOG.error("MD5 sums didn't match, removing file " + filename);
                    toDownload.delete();
                    throw new IOException();
                }

            } catch (MalformedURLException e) {
                LOG.error("Invalid URL: " + e.getMessage());
                return false;
            } catch (IOException e) {
                LOG.error("IO Exception: " + e.getMessage());
                if (e.getMessage().contains("Connection timed out")) {
                    LOG.error("Connection time out " + blenderZipEntity.getDownloadMirrors().get(downloadMirror));
                    downloadMirror++;
                    if (connection != null) {
                        connection.disconnect();
                    }
                    doDownload();

                } else {
                    LOG.error(Throwables.getStackTraceAsString(e));
                    return false;
                }

            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }


        }
        LOG.debug("All downloads complete");
        return true;
    }

    private List<BlenderZipEntity> prepareDownload() {
        List<BlenderZipEntity> list = new ArrayList<>();
        String filename;
        for (BlenderZipEntity blenderZipEntity : blenderFileEntities) {
            if (!blenderZipEntity.isDownloaded()) {
                for (BlenderZip blenderBinary : blenderBinaries) {
                    if (blenderBinary.getBlenderVersion().equals(blenderZipEntity.getBlenderVersion())) {
                        switch (blenderZipEntity.getBlenderBinaryOS().toLowerCase()) {
                            case "windows32":
                                filename = blenderBinary.getWindows32().get(0);
                                blenderZipEntity.setDownloadMirrors(blenderBinary.getWindows32());
                                blenderZipEntity.setBlenderFile(filename.substring(filename.lastIndexOf("/") + 1));
                                blenderZipEntity.setBlenderFileMd5(blenderBinary.getMd5Windows32());
                                break;
                            case "windows64":
                                filename = blenderBinary.getWindows64().get(0);
                                blenderZipEntity.setDownloadMirrors(blenderBinary.getWindows64());
                                blenderZipEntity.setBlenderFile(filename.substring(filename.lastIndexOf("/") + 1));
                                blenderZipEntity.setBlenderFileMd5(blenderBinary.getMd5Windows64());
                                break;
                            case "macos":
                                filename = blenderBinary.getMacOS().get(0);
                                blenderZipEntity.setDownloadMirrors(blenderBinary.getMacOS());
                                blenderZipEntity.setBlenderFile(filename.substring(filename.lastIndexOf("/") + 1));
                                blenderZipEntity.setBlenderFileMd5(blenderBinary.getMd5MacOS());
                                break;
                            case "linux64":
                                filename = blenderBinary.getLinux64().get(0);
                                blenderZipEntity.setDownloadMirrors(blenderBinary.getLinux64());
                                blenderZipEntity.setBlenderFile(filename.substring(filename.lastIndexOf("/") + 1));
                                blenderZipEntity.setBlenderFileMd5(blenderBinary.getMd5Linux64());
                                break;
                            case "linux32":
                                filename = blenderBinary.getLinux32().get(0);
                                blenderZipEntity.setDownloadMirrors(blenderBinary.getLinux32());
                                blenderZipEntity.setBlenderFile(filename.substring(filename.lastIndexOf("/") + 1));
                                blenderZipEntity.setBlenderFileMd5(blenderBinary.getMd5Linux32());
                                break;
                            default:
                                LOG.error("Invalid blender binary operating system " + blenderZipEntity.getBlenderBinaryOS());
                        }
                    }
                }
                list.add(blenderZipEntity);
            }
        }
        if (list.size() == 0) {
            LOG.debug("Nothing to download");
        }
        return list;
    }
}
