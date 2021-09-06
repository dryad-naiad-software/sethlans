package com.dryadandnaiad.sethlans.unit.utils;

import com.dryadandnaiad.sethlans.utils.NetworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
class NetworkUtilsTest {

    @Test
    void getJSONFromURLWithAuth() throws MalformedURLException {
        var url = new URL("https://tau:7443/login");
//        System.out.println(NetworkUtils.getJSONFromURLWithAuth("url.toString()",
//                "https://tau:7443/api/v1/management/network_node_scan", "testuser", "testPa$$1234"));

    }

    @Test
    void getJSONFromURL() throws MalformedURLException {
        var path = "/posts/13";
        var host = "jsonplaceholder.typicode.com";
        var port = "443";
        System.out.println(NetworkUtils.getJSONFromURL(path, host, port, true));

    }

    @Test
    void postJSONToURLWithAuth() {
    }

    @Test
    void postJSONToURL() {
    }
}