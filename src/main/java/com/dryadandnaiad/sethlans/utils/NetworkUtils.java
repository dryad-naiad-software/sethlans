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
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.undertow.util.StatusCodes;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static io.restassured.RestAssured.*;

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
            var path = "/api/v1/info/node_info?api-key=" + ConfigUtils.getProperty(ConfigKeys.SETHLANS_API_KEY);
            log.info("Retrieving node information from " + ip + ":" + port);
            var objectMapper = new ObjectMapper();
            return objectMapper.readValue(getJSONFromURL(path, ip, port, true), new TypeReference<>() {
            });
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return null;
        }
    }

    public static String getJSONFromURLWithAuth(String path, String host, String port, boolean secure,
                                                String username, String password) {
        host = setHost(host, port, secure);


        authGetCSRFToken(username, password);

        return getJSON(path, host, port);

    }

    private static String getJSON(String path, String host, String port) {
        try {
            RestAssured.basePath = path;
            return get()
                    .then().statusCode(200)
                    .extract()
                    .response()
                    .body()
                    .asString();
        } catch (AssertionError | Exception e) {
            log.error("Unable to connect to " + host + ":" + port + path);
            log.error(Throwables.getStackTraceAsString(e));
            return null;
        }
    }

    private static String setHost(String host, String port, boolean secure) {
        if (secure) {
            host = "https://" + host;
        } else {
            host = "http://" + host;
        }
        if (Boolean.parseBoolean(ConfigUtils.getProperty(ConfigKeys.USE_SETHLANS_CERT))) {
            RestAssured.useRelaxedHTTPSValidation();
        }
        RestAssured.port = Integer.parseInt(port);
        RestAssured.baseURI = host;
        return host;
    }

    public static String getJSONWithParams(String path, String host, String port, Map<String, String> params, boolean secure) {
        host = setHost(host, port, secure);

        try {
            RestAssured.basePath = path;
            return given().params(params).when().get()
                    .then().statusCode(200)
                    .extract()
                    .response()
                    .body()
                    .asString();
        } catch (AssertionError | Exception e) {
            log.error("Unable to connect to " + host + ":" + port + path);
            log.error(Throwables.getStackTraceAsString(e));
            return null;
        }
    }


    public static String getJSONFromURL(String path, String host, String port, boolean secure) {
        host = setHost(host, port, secure);

        return getJSON(path, host, port);
    }

    public static boolean postJSONToURLWithAuth(String port, String host, String path, boolean secure,
                                                String json, String username, String password) {
        host = setHost(host, port, secure);

        return postJSON(path, host, port, authGetCSRFToken(username, password), json);
    }

    public static boolean postJSONToURL(String path, String host, String port, String json, boolean secure) {
        host = setHost(host, port, secure);
        var token = "";
        return postJSON(path, host, port, token, json);
    }

    private static String authGetCSRFToken(String username, String password) {
        log.debug("Starting login");
        RestAssured.basePath = "/login";
        var response =
                given().
                        when().get().
                        then().extract().response();
        String token = response.cookie("XSRF-TOKEN");

        response = given().log().ifValidationFails()
                .header("X-XSRF-TOKEN", token)
                .cookie("XSRF-TOKEN", token)
                .param("username", username.toLowerCase())
                .param("password", password)
                .when().post().then().statusCode(302).extract().response();

        sessionId = response.cookie("JSESSIONID");
        log.debug("Login completed, obtained the following cookies");
        log.debug("XSRF-TOKEN: " + token);
        log.debug("JSESSIONID: " + sessionId);

        return token;
    }


    private static boolean postJSON(String path, String host, String port, String token, String json) {
        RestAssured.basePath = path;
        if (token.isEmpty()) {
            try {
                given()
                        .log()
                        .ifValidationFails()
                        .accept(ContentType.JSON)
                        .contentType(ContentType.JSON)
                        .body(json)
                        .post()
                        .then()
                        .statusCode(StatusCodes.CREATED);
                return true;
            } catch (AssertionError | Exception e) {
                log.error("Unable to connect to " + host + ":" + port + path);
                log.error(Throwables.getStackTraceAsString(e));
                return false;
            }
        } else {
            try {
                given()
                        .log()
                        .ifValidationFails()
                        .accept(ContentType.JSON)
                        .contentType(ContentType.JSON)
                        .header("X-XSRF-TOKEN", token)
                        .cookie("XSRF-TOKEN", token)
                        .body(json)
                        .post()
                        .then()
                        .statusCode(StatusCodes.CREATED);
                return true;
            } catch (AssertionError | Exception e) {
                log.error("Unable to connect to " + host + ":" + port + path);
                log.error(Throwables.getStackTraceAsString(e));
                return false;
            }

        }

    }


}
