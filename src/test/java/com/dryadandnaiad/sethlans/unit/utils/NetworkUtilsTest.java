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
        System.out.println(NetworkUtils.getJSONFromURLWithAuth(url.toString(), url.toString(), "test", "test"));

    }

    @Test
    void getJSONFromURL() throws MalformedURLException {
        var url = new URL("https://jsonplaceholder.typicode.com/posts/143");
        System.out.println(NetworkUtils.getJSONFromURL(url));

    }

    @Test
    void postJSONToURLWithAuth() {
    }

    @Test
    void postJSONToURL() {
    }
}