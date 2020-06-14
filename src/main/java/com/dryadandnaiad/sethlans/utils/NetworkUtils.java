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

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.URL;
import java.util.HashSet;
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
        var multicastSocketPort = Integer.parseInt(ConfigUtils.getProperty(ConfigKeys.MULTICAST_PORT));
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
            connection.setSSLSocketFactory(SSLUtilities.buildSSLSocketFactory());
            connection.setHostnameVerifier(SSLUtilities.allHostsValid());
            connection.setRequestMethod("GET");
            connection.connect();

            if (connection.getResponseCode() == 200) {
                var reader = new InputStreamReader(connection.getInputStream());
                return CharStreams.toString(reader);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return null;
        }
        return null;
    }
}
