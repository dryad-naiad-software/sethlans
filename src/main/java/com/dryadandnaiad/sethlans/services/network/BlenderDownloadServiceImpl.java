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

import com.dryadandnaiad.sethlans.domains.BlenderFile;
import com.dryadandnaiad.sethlans.domains.BlenderFileEntity;
import com.dryadandnaiad.sethlans.server.BlenderUtils;
import com.dryadandnaiad.sethlans.services.database.BlenderFileService;
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
    private BlenderFileService blenderFileService;
    private List<BlenderFileEntity> blenderFileEntities;
    private List<BlenderFile> blenderBinaries;
    @Value("${sethlans.blenderDir}")
    private String downloadLocation;

    @Autowired
    public void setBlenderFileService(BlenderFileService blenderFileService) {
        this.blenderFileService = blenderFileService;
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
        blenderFileEntities = (List<BlenderFileEntity>) blenderFileService.listAll();
        blenderBinaries = BlenderUtils.listBinaries();
        List<BlenderFileEntity> blenderDownloadList = prepareDownload();


        for (BlenderFileEntity blenderFileEntity : blenderDownloadList) {
            String blenderVersion = blenderFileEntity.getBlenderVersion();
            File saveLocation = new File(downloadLocation + File.separator + "binaries" + File.separator + blenderVersion + File.separator);
            saveLocation.mkdirs();
            URL url;
            HttpURLConnection connection = null;
            String filename = null;
            try {

                url = new URL(blenderFileEntity.getBlenderFile());
                connection = (HttpURLConnection) url.openConnection();
                InputStream stream = connection.getInputStream();
                if (!blenderFileEntity.getBlenderBinaryOS().contains("Linux")) {
                    filename = blenderVersion + "-" + blenderFileEntity.getBlenderBinaryOS().toLowerCase() + ".zip";
                } else {
                    filename = blenderVersion + "-" + blenderFileEntity.getBlenderBinaryOS().toLowerCase() + ".tar.bz2";
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

                if (SethlansUtils.fileCheckMD5(toDownload, blenderFileEntity.getBlenderFileMd5())) {
                    blenderFileEntity.setBlenderFile(toDownload.toString());
                    LOG.debug(filename + " downloaded successfully.");
                    blenderFileEntity.setDownloaded(true);
                    blenderFileService.saveOrUpdate(blenderFileEntity);
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
                LOG.error(Throwables.getStackTraceAsString(e));
                return false;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }


        }
        LOG.debug("All downloads complete");
        return true;
    }

    private List<BlenderFileEntity> prepareDownload() {
        List<BlenderFileEntity> list = new ArrayList<>();
        for (BlenderFileEntity blenderFileEntity : blenderFileEntities) {
            if (!blenderFileEntity.isDownloaded()) {
                for (BlenderFile blenderBinary : blenderBinaries) {
                    if (blenderBinary.getBlenderVersion().equals(blenderFileEntity.getBlenderVersion())) {
                        switch (blenderFileEntity.getBlenderBinaryOS().toLowerCase()) {
                            case "windows32":
                                blenderFileEntity.setBlenderFile(blenderBinary.getWindows32());
                                blenderFileEntity.setBlenderFileMd5(blenderBinary.getMd5Windows32());
                                break;
                            case "windows64":
                                blenderFileEntity.setBlenderFile(blenderBinary.getWindows64());
                                blenderFileEntity.setBlenderFileMd5(blenderBinary.getMd5Windows64());
                                break;
                            case "macos":
                                blenderFileEntity.setBlenderFile(blenderBinary.getMacOS());
                                blenderFileEntity.setBlenderFileMd5(blenderBinary.getMd5MacOS());
                                break;
                            case "linux64":
                                blenderFileEntity.setBlenderFile(blenderBinary.getLinux64());
                                blenderFileEntity.setBlenderFileMd5(blenderBinary.getMd5Linux64());
                                break;
                            case "linux32":
                                blenderFileEntity.setBlenderFile(blenderBinary.getLinux32());
                                blenderFileEntity.setBlenderFileMd5(blenderBinary.getMd5Linux32());
                                break;
                            default:
                                LOG.error("Invalid blender binary operating system " + blenderFileEntity.getBlenderBinaryOS());
                        }
                    }
                }
                list.add(blenderFileEntity);
            }
        }
        if (list.size() == 0) {
            LOG.debug("Nothing to download");
        }
        return list;
    }
}
