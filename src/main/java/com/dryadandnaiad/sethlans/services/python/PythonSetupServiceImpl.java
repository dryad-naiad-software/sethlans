/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC.
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

package com.dryadandnaiad.sethlans.services.python;

import com.dryadandnaiad.sethlans.domains.python.PythonArchive;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.network.GetRawDataService;
import com.dryadandnaiad.sethlans.services.network.GetRawDataServiceImpl;
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

import java.io.*;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Created Mario Estrella on 3/27/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class PythonSetupServiceImpl implements PythonSetupService {
    private static final Logger LOG = LoggerFactory.getLogger(PythonSetupServiceImpl.class);

    @Override
    public boolean installPython(String binaryDir) {
        String pythonFile = downloadPython(binaryDir);
        if (SethlansUtils.archiveExtract(pythonFile, new File(binaryDir))) {
            if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_LINUX) {
                try {
                    ProcessBuilder pb = new ProcessBuilder("chmod", "-R", "+x", binaryDir + "python" + File.separator + "bin");
                    pb.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_LINUX) {
                SethlansUtils.writeProperty(SethlansConfigKeys.PYTHON_BIN, binaryDir + "python" + File.separator + "bin" + File.separator + "python3.5m");
            }
            if (SystemUtils.IS_OS_WINDOWS) {
                SethlansUtils.writeProperty(SethlansConfigKeys.PYTHON_BIN, binaryDir + "python" + File.separator + "bin" + File.separator + "python.exe");
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean setupScripts(String scriptsDir) {
        File blendInfo = new File(scriptsDir + File.separator + "blend_info.py");
        File blendFile = new File(scriptsDir + File.separator + "blendfile.py");
        try {
            BufferedWriter writer;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new Resources("scripts/blendfile.py").getResource(), "UTF-8"))) {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(blendFile)));
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line + "\n");
                }
            }
            writer.close();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new Resources("scripts/blend_info.py").getResource(), "UTF-8"))) {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(blendInfo)));
                String line;
                while ((line = reader.readLine()) != null) {
                    writer.write(line + "\n");
                }
            }
            writer.close();
        } catch (NoSuchFileException | UnsupportedEncodingException | FileNotFoundException ex) {
            LOG.error(ex.getMessage());
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return false;
    }

    private String binDir;
    private PythonArchive pythonArchive;

    public PythonArchive getPythonArchive() {
        setPythonBinary();
        return pythonArchive;
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
                    this.pythonArchive = new PythonArchive(pythonDownload.get("windows64"), pythonDownload.get("windows64_md5"), pythonDownload.get("windows64_filename"));
                    return true;
                } else {
                    this.pythonArchive = new PythonArchive(pythonDownload.get("windows32"), pythonDownload.get("windows32_md5"), pythonDownload.get("windows32_filename"));
                    return true;
                }
            }
            if (SystemUtils.IS_OS_LINUX) {
                if (SystemUtils.OS_ARCH.contains("64")) {
                    this.pythonArchive = new PythonArchive(pythonDownload.get("linux64"), pythonDownload.get("linux64_md5"), pythonDownload.get("linux64_filename"));
                    return true;
                } else {
                    this.pythonArchive = new PythonArchive(pythonDownload.get("linux32"), pythonDownload.get("linux32_md5"), pythonDownload.get("linux32_filename"));
                    return true;
                }
            }
            if (SystemUtils.IS_OS_MAC) {
                this.pythonArchive = new PythonArchive(pythonDownload.get("macos"), pythonDownload.get("macos_md5"), pythonDownload.get("macos_filename"));
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
            InputStream inputStream = new Resources(pythonArchive.getBinaryURL()).getResource();
            LOG.debug("Copying  " + pythonArchive.getFilename() + "...");
            Files.copy(inputStream, Paths.get(saveLocation + File.separator + pythonArchive.getFilename()));
            if (SethlansUtils.fileCheckMD5(new File(saveLocation + File.separator + pythonArchive.getFilename()), pythonArchive.getMd5())) {
                LOG.debug(pythonArchive.getFilename() + " downloaded successfully.");
                return pythonArchive.getFilename();
            } else {
                LOG.error("MD5 sums didn't match, removing file " + pythonArchive.getFilename());
                File toDelete = new File(saveLocation + File.separator + pythonArchive.getFilename());
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
