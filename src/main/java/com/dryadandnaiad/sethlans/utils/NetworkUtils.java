/*
 * Copyright (c) 2020. Dryad and Naiad Software LLC.
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.dryadandnaiad.sethlans.utils;

import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.models.system.Node;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.contrib.org.apache.commons.codec_1_3.binary.Base64;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * File created by Mario Estrella on 6/11/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class NetworkUtils {

    public static Set<String> getSethlansMulticastMessages() {
        var detectedNodes = new HashSet<String>();
        var multicastIP = ConfigUtils.getProperty(ConfigKeys.MULTICAST_IP);
        var multicastSocketPort =
                Integer.parseInt(Objects.requireNonNull(ConfigUtils.getProperty(ConfigKeys.MULTICAST_PORT)));
        byte[] buffer = new byte[256];
        try {
            var clientSocket = new MulticastSocket(multicastSocketPort);
            clientSocket.setSoTimeout(10000);
            clientSocket.joinGroup(InetAddress.getByName(multicastIP));
            long start_time = System.currentTimeMillis();
            long wait_time = 15000;
            long end_time = start_time + wait_time;

            while (System.currentTimeMillis() < end_time) {
                var msgPacket = new DatagramPacket(buffer, buffer.length);
                clientSocket.receive(msgPacket);
                var msg = new String(msgPacket.getData(), 0, msgPacket.getLength());
                if (msg.contains("Sethlans")) {
                    detectedNodes.add(msg);
                }
            }
            clientSocket.close();
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
        }
        log.debug("Number of nodes detected: " + detectedNodes.size());
        return detectedNodes;
    }

    public static Set<Node> discoverNodesViaMulticast() {
        var nodeSet = new HashSet<Node>();
        log.info("Starting Node Multicast Scan");
        var multicastMessages = getSethlansMulticastMessages();
        if (!multicastMessages.isEmpty()) {
            for (String multicastMessage : multicastMessages) {
                log.debug("Processing received message: " + multicastMessage);
                var messageArray = multicastMessage.split(":");
                var ip = messageArray[1];
                var port = messageArray[2];
                var node = getNodeViaJson(ip, port);
                if (node != null) {
                    nodeSet.add(node);
                }
            }
        }
        return nodeSet;
    }

    public static Node getNodeViaJson(String ip, String port) {
        try {
            var nodeURL = new URL("https://" + ip + ":" + port + "/api/v1/info/node_info");
            log.info("Retrieving node information from " + nodeURL);
            var objectMapper = new ObjectMapper();
            return objectMapper.readValue(getJSONFromURL(nodeURL), new TypeReference<>() {
            });
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return null;
        }
    }

    public static String getJSONFromURL(URL url) {
        try {
            var connection = (HttpsURLConnection) url.openConnection();
            if (Boolean.parseBoolean(ConfigUtils.getProperty(ConfigKeys.USE_SETHLANS_CERT))) {
                connection.setSSLSocketFactory(SSLUtilities.buildSSLSocketFactory());
                connection.setHostnameVerifier(SSLUtilities.allHostsValid());
            }
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() == 200) {
                var reader = new InputStreamReader(connection.getInputStream());
                connection.disconnect();
                return CharStreams.toString(reader);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return null;
        }
        return null;
    }

    public static ResponseEntity<Void> postJSONToURLWithAuth(URL url, String json, String username, String password) {
        try {
            var auth = username + ":" + password;
            var encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
            var authHeaderValue = "Basic " + new String(encodedAuth);
            var firstConnection = (HttpsURLConnection) url.openConnection();
            if (Boolean.parseBoolean(ConfigUtils.getProperty(ConfigKeys.USE_SETHLANS_CERT))) {
                firstConnection.setSSLSocketFactory(SSLUtilities.buildSSLSocketFactory());
                firstConnection.setHostnameVerifier(SSLUtilities.allHostsValid());
            }
            firstConnection.setRequestMethod("GET");
            firstConnection.setRequestProperty("Authorization", authHeaderValue);
            firstConnection.setRequestProperty("X-CSRF-Token", "Fetch");
            firstConnection.connect();

            var sessionCookies = getSessionCookies(firstConnection);
            var xsrfToken = extractXRSFToken(firstConnection);

            firstConnection.disconnect();

            var secondConnection = (HttpsURLConnection) url.openConnection();
            if (Boolean.parseBoolean(ConfigUtils.getProperty(ConfigKeys.USE_SETHLANS_CERT))) {
                secondConnection.setSSLSocketFactory(SSLUtilities.buildSSLSocketFactory());
                secondConnection.setHostnameVerifier(SSLUtilities.allHostsValid());
            }

            secondConnection.setRequestMethod("POST");
            secondConnection.setRequestProperty("X-CSRF-Token", xsrfToken);
            secondConnection.setRequestProperty("Content-Type", "application/json; utf-8");
            secondConnection.setRequestProperty("Accept", "application/json");
            setSessionCookies(secondConnection, sessionCookies);
            return sendJSON(json, secondConnection);

        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


    public static ResponseEntity<Void> postJSONToURL(URL url, String json) {
        try {
            var connection = (HttpsURLConnection) url.openConnection();
            if (Boolean.parseBoolean(ConfigUtils.getProperty(ConfigKeys.USE_SETHLANS_CERT))) {
                connection.setSSLSocketFactory(SSLUtilities.buildSSLSocketFactory());
                connection.setHostnameVerifier(SSLUtilities.allHostsValid());
            }
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; utf-8");
            connection.setRequestProperty("Accept", "application/json");

            return sendJSON(json, connection);

        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private static List<String> getSessionCookies(HttpsURLConnection conn) {
        var response_headers = conn.getHeaderFields();
        var keys = response_headers.keySet().iterator();
        String key;
        while (keys.hasNext()) {
            key = keys.next();
            if ("set-cookie".equalsIgnoreCase(key)) {
                return response_headers.get(key);
            }
        }
        return null;
    }

    private static void setSessionCookies(HttpsURLConnection conn, List<String> session) {
        if (session != null) {
            StringBuilder aggregated_cookies = new StringBuilder();
            for (String cookie : session) {
                aggregated_cookies.append(cookie).append("; ");
            }
            conn.setRequestProperty("cookie", aggregated_cookies.toString());
        }
    }

    private static String extractXRSFToken(HttpsURLConnection conn) {
        List<String> value = null;
        var headers = conn.getHeaderFields();
        for (String key : headers.keySet()) {
            if ("X-CSRF-Token".equalsIgnoreCase(key)) {
                value = headers.get(key);
            }
        }

        if (value == null || value.size() == 0) {
            return null;
        }
        return value.get(0);
    }

    private static ResponseEntity<Void> sendJSON(String json, HttpsURLConnection connection) throws IOException {
        connection.setDoOutput(true);
        var outputStream = connection.getOutputStream();
        var input = json.getBytes(StandardCharsets.UTF_8);
        outputStream.write(input, 0, input.length);
        if (connection.getResponseCode() == 201) {
            connection.disconnect();
            return new ResponseEntity<>(HttpStatus.CREATED);
        } else {
            connection.disconnect();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
