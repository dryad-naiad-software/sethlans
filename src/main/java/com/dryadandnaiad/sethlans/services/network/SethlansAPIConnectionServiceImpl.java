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

package com.dryadandnaiad.sethlans.services.network;

import com.dryadandnaiad.sethlans.utils.SSLUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created Mario Estrella on 12/12/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class SethlansAPIConnectionServiceImpl implements SethlansAPIConnectionService {
    private static final Logger LOG = LoggerFactory.getLogger(SethlansAPIConnectionServiceImpl.class);

    @Override
    public boolean sendToRemotePOST(String connectionURL, String params) {
        LOG.debug("Connecting to " + connectionURL);
        HttpsURLConnection connection;
        try {
            LOG.debug("Sending the following parameters to API via POST: " + params);
            URL url = new URL(connectionURL);
            SSLUtilities.trustAllHostnames();
            SSLUtilities.trustAllHttpsCertificates();
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(params);
            wr.flush();
            wr.close();

            int response = connection.getResponseCode();
            LOG.debug("HTTP Response code " + response);
            if (response == 200) {
                return true;
            }


        } catch (UnsupportedEncodingException e) {
            LOG.error("Unsupported Encoding Exception " + e.getMessage());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return false;
    }

    @Override
    public String downloadFromRemoteGET(String connectionURL, String params, String location) {
        LOG.debug("Connecting to " + connectionURL);
        HttpsURLConnection connection;
        try {
            LOG.debug("Sending the following parameters to API via GET: " + connectionURL + params);
            URL url = new URL(connectionURL + params);
            SSLUtilities.trustAllHostnames();
            SSLUtilities.trustAllHttpsCertificates();
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            String fieldValue = connection.getHeaderField("Content-Disposition");
            if (fieldValue == null || !fieldValue.contains("filename=\"")) {
                throw new IOException("No filename found");
            }

            String filename = fieldValue.substring(fieldValue.indexOf("filename=\"") + 10, fieldValue.length() - 1);

            InputStream stream = connection.getInputStream();
            LOG.debug("Saving file to location: " + location);
            File download = new File(location, filename);
            if (download.exists()) {
                LOG.debug("Previous download of  " + download.toString() + " did not complete successfully, deleting and re-downloading.");
                if (download.delete()) {
                    LOG.debug("Re-Downloading " + filename + "...");
                    Files.copy(stream, Paths.get(download.toString()));
                }
            } else {
                LOG.debug("Downloading " + download.toString());
                Files.copy(stream, Paths.get(download.toString()));

            }
            LOG.debug("Download of " + download.toString() + " complete.");
            return filename;


        } catch (UnsupportedEncodingException e) {
            LOG.error("Unsupported Encoding Exception " + e.getMessage());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return null;
    }

    public boolean sendToRemoteGET(String connectionURL, String params) {
        return false;
    }
}