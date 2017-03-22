/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package com.dryadandnaiad.sethlans.commands;

import com.dryadandnaiad.sethlans.client.hardware.gpu.GPU;
import com.dryadandnaiad.sethlans.domains.hardware.CPU;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.enums.BlenderBinaryOS;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.enums.SetupProgress;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 3/12/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class SetupForm {
    @NotNull
    @Min(1)
    @Max(65535)
    private String httpsPort;

    @NotEmpty
    @Size(min = 4, max = 75)
    private String userName;


    @NotEmpty
    @Size(min = 8, max = 50)
    private String passWord;

    @NotEmpty
    @Size(min = 8, max = 50)
    private String passWordConf;

    @NotEmpty
    private String dataDirectory;
    @NotEmpty
    private String projectDirectory;
    @NotEmpty
    private String blenderDirectory;
    @NotEmpty
    private String tempDirectory;
    @NotEmpty
    private String workingDirectory;
    @NotEmpty
    private String logDirectory;

    private ComputeType selectedMethod;
    private List<ComputeType> availableMethods;
    private List<GPUDevice> availableGPUs;
    private String blenderVersion;
    private SethlansMode mode;
    private List<Integer> selectedGPUId;
    private int cores;
    private int totalCores;
    private SetupProgress progress;
    private SetupProgress previous;
    private List<BlenderBinaryOS> blenderBinaryOS;


    public SetupForm() {
        this.httpsPort = "7443";
        this.mode = SethlansMode.SERVER;
        this.availableGPUs = GPU.listDevices();
        this.availableMethods = new ArrayList<>();
        this.selectedGPUId = new ArrayList<>();
        this.cores = 1;
        this.dataDirectory = System.getProperty("user.home") + File.separator + ".sethlans" + File.separator + "data" + File.separator;
        this.projectDirectory = System.getProperty("user.home") + File.separator + ".sethlans" + File.separator + "projects" + File.separator;
        this.blenderDirectory = System.getProperty("user.home") + File.separator + ".sethlans" + File.separator + "blenderzips" + File.separator;
        this.tempDirectory = System.getProperty("user.home") + File.separator + ".sethlans" + File.separator + "temp" + File.separator;
        this.workingDirectory = System.getProperty("user.home") + File.separator + ".sethlans" + File.separator + "cache" + File.separator;
        this.logDirectory = System.getProperty("user.home") + File.separator + ".sethlans" + File.separator + "logs" + File.separator;
        this.totalCores = populateCores();
        populateAvailableMethods();
    }

    private int populateCores() {
        CPU cpu = new CPU();
        return cpu.cores();

    }

    private void populateAvailableMethods() {
        if (GPU.listDevices().size() != 0) {
            availableMethods.add(ComputeType.CPU_GPU);
            availableMethods.add(ComputeType.GPU);
            availableMethods.add(ComputeType.CPU);
        } else {
            availableMethods.add(ComputeType.CPU);
        }
    }

    public int getTotalCores() {
        return totalCores;
    }

    public int getCores() {
        return cores;
    }

    public void setCores(int cores) {
        this.cores = cores;
    }

    public ComputeType getSelectedMethod() {
        return selectedMethod;
    }

    public void setSelectedMethod(ComputeType selectedMethod) {
        this.selectedMethod = selectedMethod;
    }

    public List<ComputeType> getAvailableMethods() {
        return availableMethods;
    }

    public SethlansMode getMode() {
        return mode;
    }

    public void setMode(SethlansMode mode) {
        this.mode = mode;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public String getProjectDirectory() {
        return projectDirectory;
    }

    public void setProjectDirectory(String projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    public String getBlenderDirectory() {
        return blenderDirectory;
    }

    public void setBlenderDirectory(String blenderDirectory) {
        this.blenderDirectory = blenderDirectory;
    }

    public String getTempDirectory() {
        return tempDirectory;
    }

    public void setTempDirectory(String tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public String getBlenderVersion() {
        return blenderVersion;
    }

    public void setBlenderVersion(String blenderVersion) {
        this.blenderVersion = blenderVersion;
    }

    public String getHttpsPort() {
        return httpsPort;
    }

    public void setHttpsPort(String httpsPort) {
        this.httpsPort = httpsPort;
    }

    public List<GPUDevice> getAvailableGPUs() {
        return availableGPUs;
    }

    public void setAvailableGPUs(List<GPUDevice> availableGPUs) {
        this.availableGPUs = availableGPUs;
    }

    public SetupProgress getProgress() {
        return progress;
    }

    public void setProgress(SetupProgress progress) {
        this.progress = progress;
    }

    public String getPassWordConf() {
        return passWordConf;
    }

    public void setPassWordConf(String passWordConf) {
        this.passWordConf = passWordConf;
    }

    public SetupProgress getPrevious() {
        return previous;
    }

    public void setPrevious(SetupProgress previous) {
        this.previous = previous;
    }

    public List<Integer> getSelectedGPUId() {
        return selectedGPUId;
    }

    public void setSelectedGPUId(List<Integer> selectedGPUId) {
        this.selectedGPUId = selectedGPUId;
    }

    public List<BlenderBinaryOS> getBlenderBinaryOS() {
        return blenderBinaryOS;
    }

    public void setBlenderBinaryOS(List<BlenderBinaryOS> blenderBinaryOS) {
        this.blenderBinaryOS = blenderBinaryOS;
    }

    public String getLogDirectory() {
        return logDirectory;
    }

    public void setLogDirectory(String logDirectory) {
        this.logDirectory = logDirectory;
    }

    @Override
    public String toString() {
        return "SetupForm{" +
                "httpsPort='" + httpsPort + '\'' +
                ", userName='" + userName + '\'' +
                ", passWord='" + passWord + '\'' +
                ", passWordConf='" + passWordConf + '\'' +
                ", dataDirectory='" + dataDirectory + '\'' +
                ", projectDirectory='" + projectDirectory + '\'' +
                ", blenderDirectory='" + blenderDirectory + '\'' +
                ", tempDirectory='" + tempDirectory + '\'' +
                ", workingDirectory='" + workingDirectory + '\'' +
                ", logDirectory='" + logDirectory + '\'' +
                ", selectedMethod=" + selectedMethod +
                ", availableMethods=" + availableMethods +
                ", availableGPUs=" + availableGPUs +
                ", blenderVersion='" + blenderVersion + '\'' +
                ", mode=" + mode +
                ", selectedGPUId=" + selectedGPUId +
                ", cores=" + cores +
                ", totalCores=" + totalCores +
                ", progress=" + progress +
                ", previous=" + previous +
                ", blenderBinaryOS=" + blenderBinaryOS +
                '}';
    }
}
