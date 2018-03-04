package com.dryadandnaiad.sethlans.controllers;

import com.dryadandnaiad.sethlans.domains.database.user.SethlansUser;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.domains.info.SethlansSettingsInfo;
import com.dryadandnaiad.sethlans.domains.info.UserInfo;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.osnative.hardware.gpu.GPU;
import com.dryadandnaiad.sethlans.services.database.SethlansUserDatabaseService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created Mario Estrella on 3/2/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@RestController
@RequestMapping("/api/management")
public class AdminController {
    private SethlansUserDatabaseService sethlansUserDatabaseService;

    @Value("${sethlans.gpu_id}")
    private String gpuIds;

    @Value("${sethlans.cores}")
    private String selectedCores;

    @Value("${sethlans.computeMethod}")
    private ComputeType selectedComputeMethod;

    @Value("${sethlans.tileSizeGPU}")
    private String tileSizeGPU;

    @Value("${sethlans.tileSizeCPU}")
    private String titleSizeCPU;

    @GetMapping(value = "/user_list")
    public List<UserInfo> sethlansUserList() {
        List<SethlansUser> sethlansUsers = sethlansUserDatabaseService.listAll();
        List<UserInfo> userInfoList = new ArrayList<>();
        for (SethlansUser sethlansUser : sethlansUsers) {
            UserInfo userToSend = new UserInfo();
            userToSend.setUsername(sethlansUser.getUsername());
            userToSend.setActive(sethlansUser.isActive());
            userToSend.setRoles(sethlansUser.getRoles());
            userToSend.setEmail(sethlansUser.getEmail());
            userToSend.setId(sethlansUser.getId());
            userToSend.setLastUpdated(sethlansUser.getLastUpdated());
            userToSend.setDateCreated(sethlansUser.getDateCreated());
            userInfoList.add(userToSend);
        }
        return userInfoList;
    }

    @GetMapping(value = "/current_settings")
    public SethlansSettingsInfo sethlansSettingsInfo() {
        return SethlansUtils.getSettings();
    }

    @GetMapping(value = {"/selected_gpus"})
    public List<GPUDevice> getSelectedGPU() {
        List<String> gpuIdsList = Arrays.asList(gpuIds.split(","));
        List<GPUDevice> gpuDeviceList = GPU.listDevices();
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

    @GetMapping(value = {"/current_tilesize_gpu"})
    public Integer getCurrentTileSizeGPU() {
        return Integer.parseInt(this.tileSizeGPU);
    }

    @GetMapping(value = {"/current_cores"})
    public Integer getCurrentCores() {
        return Integer.parseInt(this.selectedCores);
    }

    @GetMapping(value = {"/current_tilesize_cpu"})
    public Integer getCurrentTileSizeCPU() {
        return Integer.parseInt(this.titleSizeCPU);
    }

    @GetMapping(value = {"/selected_compute_method"})
    public ComputeType getSelectedComputeMethod() {
        return this.selectedComputeMethod;
    }

    @Autowired
    public void setSethlansUserDatabaseService(SethlansUserDatabaseService sethlansUserDatabaseService) {
        this.sethlansUserDatabaseService = sethlansUserDatabaseService;
    }
}
