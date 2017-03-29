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

import com.dryadandnaiad.sethlans.domains.PythonBinary;
import com.google.common.base.Throwables;
import org.apache.commons.lang3.SystemUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created Mario Estrella on 3/28/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class PythonDownloadServiceImpl implements PythonDownloadService {
    private static final Logger LOG = LoggerFactory.getLogger(PythonDownloadServiceImpl.class);
    private String serverDir;
    private PythonBinary pythonBinary;

    private boolean setPythonBinary() {
        GetRawDataService getJSONData = new GetRawDataServiceImpl();
        String data = getJSONData.getLocalResult("pythondownload.json");
        try {
            JSONObject jsonData = new JSONObject(data);
            JSONObject pythondownload = jsonData.getJSONObject("pythondownload");
            String binary = null;
            String md5 = null;

            if (SystemUtils.IS_OS_WINDOWS) {
                String arch = System.getenv("PROCESSOR_ARCHITECTURE");
                String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

                String realArch = arch.endsWith("64")
                        || wow64Arch != null && wow64Arch.endsWith("64")
                        ? "64" : "32";
                if (realArch.equals("64")) {
                    binary = pythondownload.getString("windows64");
                    md5 = pythondownload.getString("windows64_md5");

                } else {
                    binary = pythondownload.getString("windows32");
                    md5 = pythondownload.getString("windows32_md5");

                }
            }
            if (SystemUtils.IS_OS_LINUX) {
                if (SystemUtils.OS_ARCH.contains("64")) {
                    binary = pythondownload.getString("linux64");
                    md5 = pythondownload.getString("linux64_md5");
                } else {
                    binary = pythondownload.getString("linux32");
                    md5 = pythondownload.getString("linux32_md5");
                }
            }
            if (SystemUtils.IS_OS_MAC) {
                binary = pythondownload.getString("macos");
                md5 = pythondownload.getString("macos_md5");
            }

            if (binary != null && md5 != null) {
                this.pythonBinary = new PythonBinary(binary, md5);
                LOG.debug(pythonBinary.toString());
                return true;
            }

        } catch (JSONException jsonEX) {
            LOG.error("Error processing JSON data" + jsonEX.getMessage());
            LOG.error(Throwables.getStackTraceAsString(jsonEX));
        }
        return false;
    }

    private boolean startDownload() {

        return false;
    }


    @Override
    public boolean downloadPython(String serverDir) {
        this.serverDir = serverDir;
        if (setPythonBinary()) {
            startDownload();
        }




        return false;
    }
}
