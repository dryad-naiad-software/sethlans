package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.domains.hardware.CPU;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.utils.BlenderUtils;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @Value("${sethlans.firsttime}")
    private boolean firstTime;

    @GetMapping(value = {"/first_time"})
    public boolean isFirstTime() {
        return firstTime;
    }

    @GetMapping(value = {"/version"})
    public String getVersion() {
        return SethlansUtils.getVersion();
    }

    @GetMapping(value = {"/blender_versions"})
    public List<String> getBlenderVersions() {
        return BlenderUtils.listVersions();
    }

    @GetMapping(value = {"/available_methods"})
    public List<ComputeType> getAvailableMethods() {
        return SethlansUtils.getAvailableMethods();
    }

    @GetMapping(value = {"/total_cores"})
    public Integer getTotalCores() {
        return new CPU().getCores();
    }
}
