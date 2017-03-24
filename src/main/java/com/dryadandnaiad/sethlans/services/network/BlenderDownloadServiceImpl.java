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
import com.dryadandnaiad.sethlans.domains.BlenderObject;
import com.dryadandnaiad.sethlans.server.BlenderUtils;
import com.dryadandnaiad.sethlans.services.database.BlenderFileService;
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
    private List<BlenderFile> blenderFiles;
    private List<BlenderObject> blenderBinaries;
    @Value("${sethlans.blenderDir}")
    private String downloadLocation;

    @Autowired
    public void setBlenderFileService(BlenderFileService blenderFileService) {
        this.blenderFileService = blenderFileService;
    }

    @Override
    @Async
    public Future<Boolean> downloadRequestedBlenderFiles() {
        blenderFiles = (List<BlenderFile>) blenderFileService.listAll();
        blenderBinaries = BlenderUtils.listBinaries();
        List<BlenderFile> blenderDownloadList = prepareDownload();


        for (BlenderFile blenderFile : blenderDownloadList) {
            String blenderVersion = blenderFile.getBlenderVersion();
            File saveLocation = new File(downloadLocation + File.separator + "binaries" + File.separator + blenderVersion + File.separator);
            saveLocation.mkdirs();
            URL url;
            HttpURLConnection connection = null;
            try {
                url = new URL(blenderFile.getBlenderFile());
                connection = (HttpURLConnection) url.openConnection();
                InputStream stream = connection.getInputStream();
                if (!blenderFile.getBlenderBinaryOS().contains("Linux")) {
                    String filename = blenderVersion + "-" + blenderFile.getBlenderBinaryOS().toLowerCase() + ".zip";
                    LOG.debug("Downloading " + filename + " ...");
                    Files.copy(stream, Paths.get(saveLocation + File.separator + filename));
                    LOG.debug(filename + " downloaded successfully.");
                    blenderFile.setBlenderFile(saveLocation + File.separator + filename);
                } else {
                    String filename = blenderVersion + "-" + blenderFile.getBlenderBinaryOS().toLowerCase() + ".tar.bz2";
                    LOG.debug("Downloading " + filename + " ...");
                    Files.copy(stream, Paths.get(saveLocation + File.separator + filename));
                    LOG.debug(filename + " downloaded successfully.");
                    blenderFile.setBlenderFile(saveLocation + File.separator + filename);
                }
                blenderFile.setDownloaded(true);
                blenderFileService.saveOrUpdate(blenderFile);

            } catch (MalformedURLException e) {
                LOG.error("Invalid URL" + e.getMessage());
                return new AsyncResult<>(false);
            } catch (IOException e) {
                LOG.error("IO Exception" + e.getMessage());
                return new AsyncResult<>(false);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }


        }
        LOG.debug("All downloads complete");
        return new AsyncResult<>(true);
    }

    private List<BlenderFile> prepareDownload() {
        List<BlenderFile> list = new ArrayList<>();
        for (BlenderFile blenderFile : blenderFiles) {
            if (!blenderFile.isDownloaded()) {
                for (BlenderObject blenderBinary : blenderBinaries) {
                    if (blenderBinary.getBlenderVersion().equals(blenderFile.getBlenderVersion())) {
                        switch (blenderFile.getBlenderBinaryOS().toLowerCase()) {
                            case "windows32":
                                blenderFile.setBlenderFile(blenderBinary.getWindows32());
                                break;
                            case "windows64":
                                blenderFile.setBlenderFile(blenderBinary.getWindows64());
                                break;
                            case "macos":
                                blenderFile.setBlenderFile(blenderBinary.getMacOS());
                                break;
                            case "linux64":
                                blenderFile.setBlenderFile(blenderBinary.getLinux64());
                                break;
                            case "linux32":
                                blenderFile.setBlenderFile(blenderBinary.getLinux32());

                        }
                    }
                }
                list.add(blenderFile);
            }
        }
        if (list.size() == 0) {
            LOG.debug("Nothing to download");
        }
        return list;
    }
}