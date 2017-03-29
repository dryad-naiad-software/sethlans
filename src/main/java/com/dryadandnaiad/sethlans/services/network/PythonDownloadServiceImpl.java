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
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import com.google.common.base.Throwables;
import org.apache.commons.lang3.SystemUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

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

    private boolean setPythonBinary() {
        GetRawDataService getJSONData = new GetRawDataServiceImpl();
        String data = getJSONData.getLocalResult("pythondownload.json");
        try {
            JSONObject jsonData = new JSONObject(data);
            JSONObject pythondownload = jsonData.getJSONObject("pythondownload");
            String binaryURL = null;
            String md5 = null;
            String filename = null;

            if (SystemUtils.IS_OS_WINDOWS) {
                String arch = System.getenv("PROCESSOR_ARCHITECTURE");
                String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

                String realArch = arch.endsWith("64")
                        || wow64Arch != null && wow64Arch.endsWith("64")
                        ? "64" : "32";
                if (realArch.equals("64")) {
                    binaryURL = pythondownload.getString("windows64");
                    md5 = pythondownload.getString("windows64_md5");
                    filename = pythondownload.getString("windows64_filename");

                } else {
                    binaryURL = pythondownload.getString("windows32");
                    md5 = pythondownload.getString("windows32_md5");
                    filename = pythondownload.getString("windows32_filename");

                }
            }
            if (SystemUtils.IS_OS_LINUX) {
                if (SystemUtils.OS_ARCH.contains("64")) {
                    binaryURL = pythondownload.getString("linux64");
                    md5 = pythondownload.getString("linux64_md5");
                    filename = pythondownload.getString("linux64_filename");
                } else {
                    binaryURL = pythondownload.getString("linux32");
                    md5 = pythondownload.getString("linux32_md5");
                    filename = pythondownload.getString("linux32_filename");
                }
            }
            if (SystemUtils.IS_OS_MAC) {
                binaryURL = pythondownload.getString("macos");
                md5 = pythondownload.getString("macos_md5");
                filename = pythondownload.getString("macos_filename");
            }

            if (binaryURL != null && md5 != null) {
                this.pythonDownloadFile = new PythonDownloadFile(binaryURL, md5, filename);
                LOG.debug(pythonDownloadFile.toString());
                return true;
            }

        } catch (JSONException jsonEX) {
            LOG.error("Error processing JSON data" + jsonEX.getMessage());
            LOG.error(Throwables.getStackTraceAsString(jsonEX));
        }
        return false;
    }

    private String startDownload() {
        File saveLocation = new File(binDir);
        saveLocation.mkdirs();
        URL url;
        HttpURLConnection connection = null;
        try {
            url = new URL(pythonDownloadFile.getBinaryURL());
            connection = (HttpURLConnection) url.openConnection();
            InputStream stream = connection.getInputStream();
            LOG.debug("Downloading  " + pythonDownloadFile.getFilename() + "...");
            Files.copy(stream, Paths.get(saveLocation + File.separator + pythonDownloadFile.getFilename()));
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
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
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
