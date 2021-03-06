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
import java.util.Map;

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
        LOG.info("Connecting to " + connectionURL);
        HttpsURLConnection connection;
        try {
            LOG.debug("Sending the following parameters to API via POST: " + params);
            URL url = new URL(connectionURL);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setHostnameVerifier(SSLUtilities.allHostsValid());
            connection.setSSLSocketFactory(SSLUtilities.buildSSLSocketFactory());
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(params);
            wr.flush();
            wr.close();

            int responseCode = connection.getResponseCode();
            LOG.debug("HTTP Response code " + responseCode);
            if (responseCode == 200) {
                return checkBooleanResponse(connection);
            }


        } catch (UnsupportedEncodingException e) {
            LOG.error("Unsupported Encoding Exception " + e.getMessage());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return false;
    }

    private boolean checkBooleanResponse(HttpsURLConnection connection) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();
        String output;
        while ((output = br.readLine()) != null) {
            stringBuilder.append(output);
        }
        br.close();
        if (stringBuilder.toString().contains("true")) {
            return true;
        }
        return !stringBuilder.toString().contains("false");
    }


    @Override
    public boolean uploadToRemotePOST(String connectionURL, Map<String, String> params, File toUpload) {
        LOG.info("Connecting to " + connectionURL);
        HttpsURLConnection connection;
        String boundary = Long.toHexString(System.currentTimeMillis());
        String charset = "UTF-8";
        String CRLF = "\r\n"; // Line separator required by multipart/form-data.
        if (toUpload.exists()) {
            try {
                LOG.debug("Sending the following parameters to API via POST: " + params);
                URL url = new URL(connectionURL);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setSSLSocketFactory(SSLUtilities.buildSSLSocketFactory());
                connection.setHostnameVerifier(SSLUtilities.allHostsValid());
                connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.setDoOutput(true);

                OutputStream output = connection.getOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);

                for (String key : params.keySet()) {
                    writer.append("--" + boundary).append(CRLF);
                    writer.append("Content-Disposition: form-data; name=\"" + key + "\"").append(CRLF);
                    writer.append("Content-Type: text/plain; charset=" + charset).append(CRLF);
                    writer.append(CRLF).append(params.get(key)).append(CRLF).flush();
                }


                // Send binary file.
                writer.append("--" + boundary).append(CRLF);
                writer.append("Content-Disposition: form-data; name=\"part\"; filename=\"" + toUpload.getName() + "\"").append(CRLF);
                writer.append("Content-Type: " + HttpsURLConnection.guessContentTypeFromName(toUpload.getName())).append(CRLF);
                writer.append("Content-Transfer-Encoding: binary").append(CRLF);
                writer.append(CRLF).flush();
                Files.copy(toUpload.toPath(), output);
                output.flush(); // Important before continuing with writer!
                writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.

                // End of multipart/form-data.
                writer.append("--" + boundary + "--").append(CRLF).flush();


                int response = connection.getResponseCode();
                LOG.debug("HTTP Response code " + response);
                if (response == 200) {
                    return true;
                } else {
                    BufferedReader br = new BufferedReader(new InputStreamReader((connection.getErrorStream())));
                    StringBuilder sb = new StringBuilder();
                    String stringoutput;
                    while ((stringoutput = br.readLine()) != null) {
                        sb.append(stringoutput);
                        LOG.debug(sb.toString());
                    }
                    br.close();
                }


            } catch (UnsupportedEncodingException e) {
                LOG.error("Unsupported Encoding Exception " + e.getMessage());
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }
        } else {
            LOG.error("The file " + toUpload.toString() + " does not exist.");
        }

        return false;
    }

    @Override
    public String downloadFromRemoteGET(String connectionURL, String params, String location) {
        LOG.info("Connecting to " + connectionURL);
        HttpsURLConnection connection;
        try {
            LOG.debug("Sending the following parameters to API via GET: " + connectionURL + "?" + params);
            URL url = new URL(connectionURL + "?" + params);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(SSLUtilities.buildSSLSocketFactory());
            connection.setHostnameVerifier(SSLUtilities.allHostsValid());
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
                LOG.warn("Previous download of  " + download.toString() + " did not complete successfully, deleting and re-downloading.");
                if (download.delete()) {
                    LOG.info("Re-Downloading " + filename + "...");
                    Files.copy(stream, Paths.get(download.toString()));
                }
            } else {
                LOG.info("Downloading " + download.toString());
                Files.copy(stream, Paths.get(download.toString()));

            }
            LOG.info("Download of " + download.toString() + " complete.");
            connection.disconnect();
            return filename;


        } catch (UnsupportedEncodingException e) {
            LOG.error("Unsupported Encoding Exception " + e.getMessage());
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return "";
    }

    @Override
    public boolean queryNode(String connectionURL, String params) {
        HttpsURLConnection connection;
        try {
            URL url = new URL(connectionURL + "?" + params);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(SSLUtilities.buildSSLSocketFactory());
            connection.setHostnameVerifier(SSLUtilities.allHostsValid());
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            int response = connection.getResponseCode();
            if (response == 200) {
                return checkBooleanResponse(connection);
            }
        } catch (UnsupportedEncodingException e) {
            LOG.error("Unsupported Encoding Exception " + e.getMessage());
        } catch (IOException e) {
            LOG.error("Query to node at " + connectionURL + " failed! " + e.getMessage());
        }
        return false;
    }

    public boolean sendToRemoteGET(String connectionURL, String params) {
        LOG.info("Connecting to " + connectionURL);
        HttpsURLConnection connection;
        try {
            LOG.debug("Sending the following parameters to API via GET: " + connectionURL + "?" + params);
            URL url = new URL(connectionURL + "?" + params);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(SSLUtilities.buildSSLSocketFactory());
            connection.setHostnameVerifier(SSLUtilities.allHostsValid());
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

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
}
