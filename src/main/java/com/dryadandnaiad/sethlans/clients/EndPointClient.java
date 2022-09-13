package com.dryadandnaiad.sethlans.clients;

import com.dryadandnaiad.sethlans.models.system.Server;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.net.URI;

@FeignClient(name="sethlans-endpoint-client", url = "https://test")
public interface EndPointClient {

    @PostMapping(path = "/add_server_to_node")
    boolean addServerToNode(URI baseUrl, @RequestBody Server server);
}
