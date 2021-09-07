package com.dryadandnaiad.sethlans.unit.utils;

import com.dryadandnaiad.sethlans.utils.NetworkUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;



@Slf4j
class NetworkUtilsTest {

    @Disabled
    @Test
    void getJSONFromURLWithAuth() {
        var response = NetworkUtils.getJSONFromURLWithAuth("/api/v1/management/network_node_scan",
                "tau", "7443", true, "testuser", "testPa$$1234");
        assertThat(response).isNotNull();
    }

    @Test
    void getJSONFromURL() {
        var path = "/posts/13";
        var host = "jsonplaceholder.typicode.com";
        var port = "443";
        var response = NetworkUtils.getJSONFromURL(path, host, port, true);
        assertThat(response).isNotNull();
    }

    @Disabled
    @Test
    void postJSONToURL() {
        var path = "/posts";
        var host = "jsonplaceholder.typicode.com";
        var port = "443";
        var json = "{" +
                "    title: 'foo'," +
                "    body: 'bar'," +
                "    userId: 1," +
                "  }";
        assertThat(NetworkUtils.postJSONToURL(path, host, port, json, true)).isTrue();

    }
}