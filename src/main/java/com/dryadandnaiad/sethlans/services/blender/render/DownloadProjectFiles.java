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

package com.dryadandnaiad.sethlans.services.blender.render;

import com.dryadandnaiad.sethlans.domains.database.render.RenderTask;
import com.dryadandnaiad.sethlans.domains.database.server.SethlansServer;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.database.SethlansServerDatabaseService;
import com.dryadandnaiad.sethlans.services.network.SethlansAPIConnectionService;
import com.dryadandnaiad.sethlans.utils.SethlansFileUtils;
import com.dryadandnaiad.sethlans.utils.SethlansQueryUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.dryadandnaiad.sethlans.utils.BlenderUtils.assignBlenderExecutable;
import static com.dryadandnaiad.sethlans.utils.BlenderUtils.renameBlenderDir;
import static com.dryadandnaiad.sethlans.utils.SethlansConfigUtils.getProperty;
import static com.dryadandnaiad.sethlans.utils.SethlansFileUtils.archiveExtract;

/**
 * Created Mario Estrella on 9/16/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
class DownloadProjectFiles {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadProjectFiles.class);

    static boolean downloadRequiredFiles(SethlansServerDatabaseService sethlansServerDatabaseService, SethlansAPIConnectionService sethlansAPIConnectionService,
                                         String binDir,
                                         File renderDir, File blendFileDir, RenderTask renderTask) {
        LOG.info("Downloading required files");
        SethlansServer sethlansServer = sethlansServerDatabaseService.getByConnectionUUID(renderTask.getConnectionUUID());
        String serverIP = sethlansServer.getIpAddress();
        String serverPort = sethlansServer.getNetworkPort();
        String cachedBlenderBinaries = getProperty(SethlansConfigKeys.CACHED_BLENDER_BINARIES);

        if (renderDir.mkdirs()) {
            //Download Blender from server
            String[] cachedBinariesList;
            boolean versionCached = false;
            cachedBinariesList = cachedBlenderBinaries.split(",");
            for (String binary : cachedBinariesList) {
                if (binary.equals(renderTask.getBlenderVersion())) {
                    versionCached = true;
                    LOG.info(binary + " renderer is already cached.  Skipping Download.");
                }
            }


            if (!versionCached) {
                String connectionURL = "https://" + serverIP + ":" + serverPort + "/api/project/blender_binary/";
                String params = "connection_uuid=" + renderTask.getConnectionUUID() + "&version=" +
                        renderTask.getBlenderVersion() + "&os=" + SethlansQueryUtils.getOS();
                String filename = sethlansAPIConnectionService.downloadFromRemoteGET(connectionURL, params, binDir);


                if (archiveExtract(filename, new File(binDir), true)) {
                    LOG.debug("Extraction complete.");
                    LOG.debug("Attempting to rename blender directory. Will attempt 3 tries.");
                    int count = 0;
                    while (!renameBlenderDir(renderDir, new File(binDir), renderTask, cachedBlenderBinaries)) {
                        count++;
                        if (count == 2) {
                            return false;
                        }
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            } else {
                renderTask.setRenderDir(renderDir.toString());
                renderTask.setBlenderExecutable(assignBlenderExecutable(new File(binDir), renderTask.getBlenderVersion()));
            }

            // Download Blend File from server
            if (blendFileDir.mkdirs()) {
                String connectionURL = "https://" + serverIP + ":" + serverPort + "/api/project/blend_file/";
                String params = "connection_uuid=" + renderTask.getConnectionUUID() + "&project_uuid=" + renderTask.getProjectUUID();
                String blendFile = sethlansAPIConnectionService.downloadFromRemoteGET(connectionURL, params, blendFileDir.toString());
                try {
                    if (SethlansFileUtils.fileCheckMD5(new File(blendFileDir + File.separator + blendFile), renderTask.getBlendFileMD5Sum())) {
                        List<String> filenameSplit = Arrays.asList(blendFile.split("\\.(?=[^.]+$)"));
                        if (filenameSplit.get(1).contains("zip")) {
                            LOG.debug("Blend file zip bundle.  Extracting contents of zip first.");
                            archiveExtract(blendFile, blendFileDir, true);
                            selectCachedBlend(blendFileDir, renderTask);
                        } else {
                            renderTask.setBlendFilename(blendFileDir + File.separator + blendFile);
                            LOG.info("Required files downloaded.");
                        }
                    }
                } catch (IOException e) {
                    LOG.error(e.getMessage());
                }
            } else {
                ComputeType computeType = ComputeType.valueOf(getProperty(SethlansConfigKeys.COMPUTE_METHOD));
                try {
                    List<String> filenameSplit = Arrays.asList(renderTask.getBlendFilename().split("\\.(?=[^.]+$)"));
                    boolean isBlendFile = filenameSplit.get(1).contains("blend");
                    LOG.debug("Is Blend File? " + isBlendFile);

                    switch (computeType) {
                        case CPU:
                            if (isBlendFile) {
                                selectCachedBlend(blendFileDir, renderTask);
                            } else {
                                waitForExtraction(blendFileDir, renderTask);
                            }
                            break;
                        case GPU:
                            if (isBlendFile) {
                                selectCachedBlend(blendFileDir, renderTask);
                            } else {
                                waitForExtraction(blendFileDir, renderTask);
                            }
                            break;
                        case CPU_GPU:
                            if (isBlendFile) {
                                selectCachedBlend(blendFileDir, renderTask);
                            } else {
                                waitForExtraction(blendFileDir, renderTask);
                            }

                    }
                } catch (IOException e) {
                    LOG.error(e.getMessage());
                }
            }
            return true;
        }
        return false;
    }

    private static void waitForExtraction(File blendFileDir, RenderTask renderTask) {
        int count = 0;
        LOG.debug("Checking to see if blend file download or extraction is currently in progress. " + blendFileDir.toString());
        while (count < 10) {
            String filenameWithoutExt = FilenameUtils.removeExtension(renderTask.getBlendFilename());
            File[] files = blendFileDir.listFiles();
            for (File file : files != null ? files : new File[0]) {
                if (file.toString().contains(filenameWithoutExt + ".blend")) {
                    renderTask.setBlendFilename(file.toString());
                    LOG.info("Blend file for this project exists, using cached version");
                    count = 11;
                    break;
                } else {
                    try {
                        count++;
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static void selectCachedBlend(File blendFileDir, RenderTask renderTask) throws IOException {
        List<String> filenameSplit = Arrays.asList(renderTask.getBlendFilename().split("\\.(?=[^.]+$)"));
        if (filenameSplit.get(1).contains("blend")) {
            LOG.debug("Verifying md5sum");
            if (SethlansFileUtils.fileCheckMD5(new File(blendFileDir + File.separator + renderTask.getBlendFilename()), renderTask.getBlendFileMD5Sum())) {
                renderTask.setBlendFilename(blendFileDir + File.separator + renderTask.getBlendFilename());
                LOG.info("Blend file for this project exists, using cached version");
            }
        } else {
            String filenameWithoutExt = filenameSplit.get(0);
            File[] files = blendFileDir.listFiles();
            for (File file : files != null ? files : new File[0]) {
                if (file.toString().contains(filenameWithoutExt + ".blend")) {
                    renderTask.setBlendFilename(file.toString());
                    LOG.info("Blend file for this project exists, using cached version");
                }
            }
        }
    }
}
