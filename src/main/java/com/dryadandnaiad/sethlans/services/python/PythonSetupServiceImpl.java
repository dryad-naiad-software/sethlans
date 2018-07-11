/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
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

import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.utils.Resources;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import com.google.common.base.Throwables;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

/**
 * Created Mario Estrella on 3/27/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class PythonSetupServiceImpl implements PythonSetupService {
    private static final Logger LOG = LoggerFactory.getLogger(PythonSetupServiceImpl.class);

    @Value("${sethlans.configDir}")
    private String configDir;

    @Override
    public boolean installPython(String binaryDir) {
        String pythonFile = copyPython(binaryDir);
        if (SethlansUtils.archiveExtract(pythonFile, new File(binaryDir))) {
            if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_LINUX) {
                try {
                    ProcessBuilder pb = new ProcessBuilder("chmod", "-R", "+x", binaryDir + "python" + File.separator + "bin");
                    pb.start();
                } catch (IOException e) {
                    LOG.error(Throwables.getStackTraceAsString(e));
                    return false;
                }
            }
            if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_LINUX) {
                SethlansUtils.writeProperty(SethlansConfigKeys.PYTHON_BIN, binaryDir + "python" + File.separator + "bin" + File.separator + "python3.5m", new File(configDir + SethlansUtils.CONFIG_FILENAME));
            }
            if (SystemUtils.IS_OS_WINDOWS) {
                SethlansUtils.writeProperty(SethlansConfigKeys.PYTHON_BIN, binaryDir + "python" + File.separator + "bin" + File.separator + "python.exe", new File(configDir + SethlansUtils.CONFIG_FILENAME));
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


    private String copyPython(String binaryDir) {
        try {
            if (SystemUtils.IS_OS_WINDOWS) {
                String arch = System.getenv("PROCESSOR_ARCHITECTURE");
                String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

                String realArch = arch.endsWith("64")
                        || wow64Arch != null && wow64Arch.endsWith("64")
                        ? "64" : "32";
                if (realArch.equals("64")) {
                    String filename = "python-3.5-windows64.txz";
                    String windows64 = "archives/python/" + filename;
                    LOG.debug("Preparing Python binaries for Windows");
                    InputStream inputStream = new Resources(windows64).getResource();
                    String path = binaryDir + filename;
                    Files.copy(inputStream, Paths.get(path));
                    return filename;
                }
            }
            if (SystemUtils.IS_OS_LINUX) {
                if (SystemUtils.OS_ARCH.contains("64")) {
                    String filename = "python-3.5-linux64.txz";
                    String linux64 = "archives/python/" + filename;
                    LOG.debug("Preparing Python binaries for Linux");
                    InputStream inputStream = new Resources(linux64).getResource();
                    String path = binaryDir + filename;
                    Files.copy(inputStream, Paths.get(path));
                    return filename;

                }
            }
            if (SystemUtils.IS_OS_MAC) {
                String filename = "python-3.5-macOS.txz";
                String macOS = "archives/python/" + filename;
                LOG.debug("Preparing Python binaries for Mac");
                InputStream inputStream = new Resources(macOS).getResource();
                String path = binaryDir + filename;
                Files.copy(inputStream, Paths.get(path));
                return filename;

            }

        } catch (IOException e) {
            LOG.error("Error during resource copy" + Throwables.getStackTraceAsString(e));
        }
        return null;
    }


}
