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

import com.dryadandnaiad.sethlans.domains.python.PythonDownloadFile;
import com.dryadandnaiad.sethlans.utils.Resources;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import com.google.common.base.Throwables;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Created Mario Estrella on 3/28/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class PythonDownloadServiceImpl implements PythonDownloadService {
    private static final Logger LOG = LoggerFactory.getLogger(PythonDownloadServiceImpl.class);
    private String binDir;
    private PythonDownloadFile pythonDownloadFile;

    public PythonDownloadFile getPythonDownloadFile() {
        setPythonBinary();
        return pythonDownloadFile;
    }

    private boolean setPythonBinary() {
        GetRawDataService getJSONData = new GetRawDataServiceImpl();
        String data = getJSONData.getLocalResult("pythondownload.json");
        try {
            Type listType = new TypeToken<HashMap<String, String>>() {
            }.getType();
            Gson gson = new Gson();

            HashMap<String, String> pythonDownload = gson.fromJson(data, listType);
            if (SystemUtils.IS_OS_WINDOWS) {
                String arch = System.getenv("PROCESSOR_ARCHITECTURE");
                String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

                String realArch = arch.endsWith("64")
                        || wow64Arch != null && wow64Arch.endsWith("64")
                        ? "64" : "32";
                if (realArch.equals("64")) {
                    this.pythonDownloadFile = new PythonDownloadFile(pythonDownload.get("windows64"), pythonDownload.get("windows64_md5"), pythonDownload.get("windows64_filename"));
                    return true;
                } else {
                    this.pythonDownloadFile = new PythonDownloadFile(pythonDownload.get("windows32"), pythonDownload.get("windows32_md5"), pythonDownload.get("windows32_filename"));
                    return true;
                }
            }
            if (SystemUtils.IS_OS_LINUX) {
                if (SystemUtils.OS_ARCH.contains("64")) {
                    this.pythonDownloadFile = new PythonDownloadFile(pythonDownload.get("linux64"), pythonDownload.get("linux64_md5"), pythonDownload.get("linux64_filename"));
                    return true;
                } else {
                    this.pythonDownloadFile = new PythonDownloadFile(pythonDownload.get("linux32"), pythonDownload.get("linux32_md5"), pythonDownload.get("linux32_filename"));
                    return true;
                }
            }
            if (SystemUtils.IS_OS_MAC) {
                this.pythonDownloadFile = new PythonDownloadFile(pythonDownload.get("macos"), pythonDownload.get("macos_md5"), pythonDownload.get("macos_filename"));
                return true;
            }

        } catch (JsonSyntaxException jsonEX) {
            LOG.error("Error processing JSON data" + jsonEX.getMessage());
            LOG.error(Throwables.getStackTraceAsString(jsonEX));
        }
        return false;
    }

    private String startDownload() {
        File saveLocation = new File(binDir);
        saveLocation.mkdirs();
        try {
            InputStream inputStream = new Resources(pythonDownloadFile.getBinaryURL()).getResource();

            LOG.debug("Copying  " + pythonDownloadFile.getFilename() + "...");
            Files.copy(inputStream, Paths.get(saveLocation + File.separator + pythonDownloadFile.getFilename()));
            if (SethlansUtils.fileCheckMD5(new File(saveLocation + File.separator + pythonDownloadFile.getFilename()), pythonDownloadFile.getMd5())) {
                LOG.debug(pythonDownloadFile.getFilename() + " downloaded successfully.");
                return pythonDownloadFile.getFilename();
            } else {
                LOG.error("MD5 sums didn't match, removing file " + pythonDownloadFile.getFilename());
                File toDelete = new File(saveLocation + File.separator + pythonDownloadFile.getFilename());
                toDelete.delete();
                throw new IOException();
            }

        } catch (MalformedURLException e) {
            LOG.error("Invalid URL " + e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
        } catch (IOException e) {
            LOG.error("IO Exception " + e.getMessage());
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return null;
    }


    @Override
    public String downloadPython(String binDir) {
        this.binDir = binDir;
        String binaryFile = null;
        if (setPythonBinary()) {
            binaryFile = startDownload();
        }
        return binaryFile;
    }
}
