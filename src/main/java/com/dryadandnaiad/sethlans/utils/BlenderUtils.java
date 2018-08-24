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

import com.dryadandnaiad.sethlans.domains.blender.BlenderZip;
import com.dryadandnaiad.sethlans.domains.database.blender.BlenderBenchmarkTask;
import com.dryadandnaiad.sethlans.domains.database.queue.RenderTask;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.network.GetRawDataService;
import com.dryadandnaiad.sethlans.services.network.GetRawDataServiceImpl;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created Mario Estrella on 3/21/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class BlenderUtils {
    private static final Logger LOG = LoggerFactory.getLogger(BlenderUtils.class);
    private static List<BlenderZip> blenderZipList = null;

    private static void getList() {

        GetRawDataService getJSONData = new GetRawDataServiceImpl();
        String data = getJSONData.getLocalResult("blenderdownload.json");
        LOG.debug("Retrieved JSON: " + data.substring(0, 100) + "...");
        if (data != null || !data.isEmpty()) {
            blenderZipList = new LinkedList<>();
            Gson gson = new Gson();


            try {
                Type collectionType = new TypeToken<Collection<BlenderZip>>() {
                }.getType();
                blenderZipList = gson.fromJson(data, collectionType);

            } catch (JsonSyntaxException jsonEx) {
                LOG.error("Error processing JSON data" + jsonEx.getMessage());
                LOG.error(Throwables.getStackTraceAsString(jsonEx));
            }
        }
    }

    public static List<BlenderZip> listBinaries() {
        if (blenderZipList == null) {
            getList();
        }
        return blenderZipList;
    }

    public static List<String> listVersions() {
        if (blenderZipList == null) {
            getList();
        }
        List<String> versions = new LinkedList<>();
        for (BlenderZip blenderZip : blenderZipList) {
            versions.add(blenderZip.getBlenderVersion());
        }
        return versions;
    }

    public static void refresh() {
        blenderZipList = null;
    }

    public static String assignBlenderExecutable(File binDir, String blenderVersion) {
        String executable = null;
        if (SethlansQueryUtils.getOS().equals("MacOS")) {
            executable = binDir.toString() + File.separator + "blender-" +
                    blenderVersion + File.separator + "blender.app" + File.separator + "Contents" + File.separator + "MacOS" + File.separator + "blender";
        }
        if (SethlansQueryUtils.getOS().equals("Windows64") || SethlansQueryUtils.getOS().equals("Windows32")) {
            executable = binDir.toString() + File.separator + "blender-" + blenderVersion + File.separator + "blender.exe";
        }
        if (SethlansQueryUtils.getOS().equals("Linux64") || SethlansQueryUtils.getOS().equals("Linux32")) {
            executable = binDir.toString() + File.separator + "blender-" + blenderVersion + File.separator + "blender";
        }
        LOG.debug("Setting executable to: " + executable);
        return executable;
    }

    public static void addCachedBlenderVersion(String blenderVersion) {
        SethlansConfigUtils.writeProperty(SethlansConfigKeys.CACHED_BLENDER_BINARIES, blenderVersion);
    }

    public static boolean renameBlenderDir(File renderDir, File binDir, RenderTask renderTask, String cachedBlenderBinaries) {
        if (renameBlenderDirectory(binDir, renderTask.getBlenderVersion())) {
            LOG.debug("Blender executable ready");
            renderTask.setRenderDir(renderDir.toString());
            renderTask.setBlenderExecutable(assignBlenderExecutable(binDir, renderTask.getBlenderVersion()));
            if (cachedBlenderBinaries == null || cachedBlenderBinaries.isEmpty() || cachedBlenderBinaries.equals("null")) {
                addCachedBlenderVersion(renderTask.getBlenderVersion());
            } else {
                appendCachedBlenderVersion(renderTask.getBlenderVersion());
            }
            return true;
        } else {
            LOG.debug("Rename failed.");
            return false;
        }

    }

    public static boolean renameBlenderDir(File benchmarkDir, File binDir, BlenderBenchmarkTask benchmarkTask, String cachedBlenderBinaries) {
        if (renameBlenderDirectory(binDir, benchmarkTask.getBlenderVersion())) {
            LOG.debug("Blender executable ready");
            benchmarkTask.setBenchmarkDir(benchmarkDir.toString());
            benchmarkTask.setBlenderExecutable(assignBlenderExecutable(binDir, benchmarkTask.getBlenderVersion()));
            if (cachedBlenderBinaries == null || cachedBlenderBinaries.isEmpty() || cachedBlenderBinaries.equals("null")) {
                addCachedBlenderVersion(benchmarkTask.getBlenderVersion());
            } else {
                appendCachedBlenderVersion(benchmarkTask.getBlenderVersion());
            }
            return true;
        } else {
            LOG.debug("Rename failed.");
            return false;
        }

    }

    public static void appendCachedBlenderVersion(String blenderVersion) {
        String currentVersions = SethlansConfigUtils.getProperty(SethlansConfigKeys.CACHED_BLENDER_BINARIES, SethlansConfigUtils.getConfigFile());
        SethlansConfigUtils.writeProperty(SethlansConfigKeys.CACHED_BLENDER_BINARIES, currentVersions + ", " + blenderVersion);
    }

    public static boolean renameBlenderDirectory(File binDir, String blenderVersion) {
        LOG.debug("Starting to rename of extracted directory in " + binDir + " to " + binDir + File.separator + "blender-" + blenderVersion);
        File[] files = binDir.listFiles();
        if (files != null) {
            for (File file : files) {
                LOG.debug("Searching " + binDir + " for extracted directory.");
                LOG.debug("Examining " + file);
                LOG.debug("Searching for directories containing " + blenderVersion);
                if (file.isDirectory() && file.toString().contains(blenderVersion)) {
                    LOG.debug(file.toString());
                    LOG.debug("Directory found, renaming");
                    if (file.renameTo(new File(binDir + File.separator + "blender-" + blenderVersion))) {
                        if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_LINUX) {
                            try {
                                ProcessBuilder pb = new ProcessBuilder("chmod", "-R", "+x", binDir.toString());
                                pb.start();
                                LOG.debug("Setting blender files as executable.");
                            } catch (IOException e) {
                                LOG.error(Throwables.getStackTraceAsString(e));
                            }
                        }
                    } else {
                        LOG.debug("Unable to rename directory.");
                        return false;
                    }
                    return true;
                }
            }
        } else {
            LOG.debug("Unable to get a list of files");
        }

        return false;
    }
}
