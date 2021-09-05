package com.dryadandnaiad.sethlans.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
class NetworkUtilsTest {

    @Test
    void getJSONFromURLWithAuth() {
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