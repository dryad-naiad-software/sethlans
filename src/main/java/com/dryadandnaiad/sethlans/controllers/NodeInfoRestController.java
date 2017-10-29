package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.domains.node.NodeInfo;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 * Created Mario Estrella on 10/28/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@Profile({"NODE", "DUAL"})
public class NodeInfoRestController {

    @Value("${server.port}")
    private String sethlansPort;

    @Value("${sethlans.computeMethod}")
    private ComputeType computeType;

    @Value("${sethlans.cores}")
    private String cores;

    @Value("${sethlans.cuda}")
    private String cuda;



    @RequestMapping(value = "/nodeinfo", method = RequestMethod.GET)
    public NodeInfo nodeInfo(){
        NodeInfo nodeInfo = new NodeInfo();
        try {
            nodeInfo.populateNodeInfo();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        nodeInfo.setNetworkPort(sethlansPort);
        nodeInfo.setComputeType(computeType);

        if(computeType == ComputeType.CPU_GPU || computeType == ComputeType.CPU) {
            nodeInfo.setCores(cores);
        }

        if(computeType == ComputeType.GPU || computeType == ComputeType.CPU_GPU) {
            List<String> cudaList = Arrays.asList(cuda.split(","));
            nodeInfo.setSelectedCUDA(cudaList);
            nodeInfo.setSelectedGPUs();

        }
        return nodeInfo;
    }
}
