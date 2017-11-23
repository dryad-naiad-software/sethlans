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

package com.dryadandnaiad.sethlans.services.system;

import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.network.PythonDownloadService;
import com.dryadandnaiad.sethlans.utils.Resources;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.NoSuchFileException;

/**
 * Created Mario Estrella on 3/27/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class PythonSetupServiceImpl implements PythonSetupService {
    private static final Logger LOG = LoggerFactory.getLogger(PythonSetupServiceImpl.class);

    private PythonDownloadService pythonDownloadService;

    @Autowired
    public void setPythonDownloadService(PythonDownloadService pythonDownloadService) {
        this.pythonDownloadService = pythonDownloadService;
    }

    @Override
    public boolean installPython(String binaryDir) {
        String pythonFile = pythonDownloadService.downloadPython(binaryDir);
        if (SethlansUtils.pythonExtract(pythonFile, new File(binaryDir))) {
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
}
