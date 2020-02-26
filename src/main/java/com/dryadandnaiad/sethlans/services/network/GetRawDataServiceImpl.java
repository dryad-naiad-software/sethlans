/*
 * Copyright (c) 2019 Dryad and Naiad Software LLC
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

import com.dryadandnaiad.sethlans.utils.Resources;
import com.dryadandnaiad.sethlans.utils.SSLUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;

/**
 * Created Mario Estrella on 3/21/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class GetRawDataServiceImpl implements GetRawDataService {
    private static final Logger LOG = LoggerFactory.getLogger(GetRawDataServiceImpl.class);

    @Override
    public String getResult(String apiURL) {
        HttpsURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(apiURL);

            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            int response = connection.getResponseCode();
            // TODO: 3/24/2019  throw error if response code is something other than 200


            StringBuilder result = new StringBuilder();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                result.append(line).append("\n");
            }
            reader.close();

            return result.toString();

        } catch (MalformedURLException e) {
            LOG.error("Inavlid URL: " + e.getMessage());
        } catch (IOException e1) {
            LOG.error("IO Exception reading data: " + e1.getMessage() + "\n Site or internet connection most likely down.");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.error("Error closing stream: " + e.getMessage());
                }
            }
        }


        return null;
    }

    @Override
    public String getNodeResult(String nodeURL) {
        HttpsURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(nodeURL);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(SSLUtilities.buildSSLSocketFactory());
            connection.setHostnameVerifier(SSLUtilities.allHostsValid());
            connection.setRequestMethod("GET");
            connection.connect();

            int response = connection.getResponseCode();

            StringBuilder result = new StringBuilder();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                result.append(line).append("\n");
            }
            reader.close();

            return result.toString();

        } catch (MalformedURLException e) {
            LOG.error("Inavlid URL: " + e.getMessage());
        } catch (IOException e1) {
            LOG.error("IO Exception reading data: " + e1.getMessage() + "\n Site or internet connection most likely down.");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.error("Error closing stream: " + e.getMessage());
                }
            }
        }


        return null;
    }

    @Override
    public String getLocalResult(String resource) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new Resources(resource).getResource(), StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder();

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                result.append(line).append("\n");
            }
            reader.close();

            return result.toString();
        } catch (NoSuchFileException | UnsupportedEncodingException | FileNotFoundException ex) {
            LOG.error(ex.getMessage());
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOG.error("Error closing stream: " + e.getMessage());
                }
            }

        }
        return null;
    }
}
