package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.domains.hardware.CPU;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.osnative.hardware.gpu.GPU;
import com.dryadandnaiad.sethlans.utils.BlenderUtils;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created Mario Estrella on 2/11/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@RequestMapping("/api/info")
public class InfoController {
    private static final Logger LOG = LoggerFactory.getLogger(InfoController.class);

    @Value("${sethlans.firsttime}")
    private boolean firstTime;

    @Value("${sethlans.mode}")
    private SethlansMode mode;

    @Value("${sethlans.gpu_id}")
    private String gpuIds;

    @Value("${sethlans.cores}")
    private String selectedCores;

    @Value("${sethlans.computeMethod}")
    private ComputeType selectedComputeMethod;

    private List<String> blenderVersions = BlenderUtils.listVersions();
    private List<ComputeType> availableMethods = SethlansUtils.getAvailableMethods();
    private Integer totalCores = new CPU().getCores();
    private List<GPUDevice> gpuDevices = GPU.listDevices();


    @GetMapping(value = {"/first_time"})
    public boolean isFirstTime() {
        return firstTime;
    }

    @GetMapping(value = {"/selected_compute_method"})
    public ComputeType getSelectedComputeMethod() {
        return this.selectedComputeMethod;
    }

    @GetMapping(value = {"/version"})
    public String getVersion() {
        return SethlansUtils.getVersion();
    }

    @GetMapping(value = {"/blender_versions"})
    public List<String> getBlenderVersions() {
        return blenderVersions;
    }

    @GetMapping(value = {"/available_methods"})
    public List<ComputeType> getAvailableMethods() {
        return availableMethods;
    }

    @GetMapping(value = {"/total_cores"})
    public Integer getTotalCores() {
        return totalCores;
    }

    @GetMapping(value = {"/available_gpus"})
    public List<GPUDevice> getAvailableGPUs() {
        return gpuDevices;
    }

    @GetMapping(value = {"/root_directory"})
    public String getRootDirectory() {
        if (firstTime) {
            return System.getProperty("user.home") + File.separator + ".sethlans";
        } else {
            return null;
        }
    }

    @GetMapping(value = {"/current_cores"})
    public Integer getCurrentCores() {
        return Integer.parseInt(this.selectedCores);
    }

    @GetMapping(value = {"/selected_gpus"})
    public List<GPUDevice> getSelectedGPU() {
        List<String> gpuIdsList = Arrays.asList(gpuIds.split(","));
        List<GPUDevice> gpuDeviceList = getAvailableGPUs();
        List<GPUDevice> selectedGPUs = new ArrayList<>();
        for (String gpuID : gpuIdsList) {
            for (GPUDevice aGpuDeviceList : gpuDeviceList) {
                if (aGpuDeviceList.getDeviceID().equals(gpuID)) {
                    selectedGPUs.add(aGpuDeviceList);
                }
            }

        }

        return selectedGPUs;
    }

    @GetMapping(value = {"/sethlans_port"})
    public String getHttpsPort() {
        if (firstTime) {
            return "7443";
        } else {
            return SethlansUtils.getPort();
        }
    }


    @GetMapping(value = {"/sethlans_mode"})
    public String getSethlansMode() {
        return mode.toString();
    }

    @GetMapping(value = {"/sethlans_ip"})
    public String getSethlansIPAddress() {
        return SethlansUtils.getIP();
    }
}
