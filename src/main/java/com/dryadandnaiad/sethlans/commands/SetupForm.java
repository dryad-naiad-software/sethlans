/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC.
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

import com.dryadandnaiad.sethlans.domains.hardware.CPU;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.enums.BlenderBinaryOS;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.enums.SetupProgress;
import com.dryadandnaiad.sethlans.osnative.hardware.gpu.GPU;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.apache.commons.lang3.SystemUtils;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    @Size(min = 4, max = 32)
    private String username;


    @NotEmpty
    @Size(max = 32)
    private String password;

    @NotEmpty
    @Size(max = 32)
    private String passwordConfirm;

    @NotEmpty
    private String sethlansRootDirectory;

    private ComputeType selectedMethod;
    private List<ComputeType> availableMethods;
    private List<GPUDevice> availableGPUs;
    private List<Integer> selectedGPUId;
    private String blenderVersion;
    private SethlansMode mode;
    private int cores;
    private int totalCores;
    private SetupProgress progress;
    private SetupProgress previous;
    private List<BlenderBinaryOS> blenderBinaryOS;
    private static final Logger LOG = LoggerFactory.getLogger(SetupForm.class);
    private String scriptsDirectory;
    private String projectDirectory;
    private String blenderDirectory;
    private String tempDirectory;
    private String workingDirectory;
    private String logDirectory;
    private String binDirectory;
    private String benchmarkDirectory;


    public SetupForm() {
        this.httpsPort = "7443";
        this.mode = SethlansMode.SERVER;
        this.availableGPUs = GPU.listDevices();
        this.selectedGPUId = new ArrayList<>();
        this.blenderBinaryOS = new ArrayList<>();
        this.cores = 1;
        this.sethlansRootDirectory = System.getProperty("user.home") + File.separator + ".sethlans";
        this.totalCores = populateCores();
        populateBlenderOS();
        this.availableMethods = SethlansUtils.getAvailableMethods();
        this.selectedMethod = ComputeType.CPU;
    }

    private void populateBlenderOS() {
        if (SystemUtils.IS_OS_MAC) {
            this.blenderBinaryOS.add(BlenderBinaryOS.MacOS);
        }
        if (SystemUtils.IS_OS_LINUX) {
            String arch = System.getProperty("os.arch");

            if (arch.equals("x86")) {
                this.blenderBinaryOS.add(BlenderBinaryOS.Linux32);
            } else {
                this.blenderBinaryOS.add(BlenderBinaryOS.Linux64);
            }

        }
        if (SystemUtils.IS_OS_WINDOWS) {
            String arch = System.getenv("PROCESSOR_ARCHITECTURE");
            String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

            String realArch = arch.endsWith("64")
                    || wow64Arch != null && wow64Arch.endsWith("64")
                    ? "64" : "32";
            if (realArch.equals("64")) {
                this.blenderBinaryOS.add(BlenderBinaryOS.Windows64);
            } else {
                this.blenderBinaryOS.add(BlenderBinaryOS.Windows32);
            }


        }

    }

    private int populateCores() {
        CPU cpu = new CPU();
        return cpu.getCores();

    }


    public void setDirectories() {
        this.projectDirectory = this.sethlansRootDirectory + File.separator + "projects" + File.separator;
        this.blenderDirectory = this.sethlansRootDirectory + File.separator + "blenderzip" + File.separator;
        this.binDirectory = this.sethlansRootDirectory + File.separator + "bin" + File.separator;
        this.tempDirectory = this.sethlansRootDirectory + File.separator + "temp" + File.separator;
        this.workingDirectory = this.sethlansRootDirectory + File.separator + "cache" + File.separator;
        this.logDirectory = this.sethlansRootDirectory + File.separator + "logs" + File.separator;
        this.benchmarkDirectory = this.sethlansRootDirectory + File.separator + "benchmarks" + File.separator;
        this.scriptsDirectory = this.binDirectory + "scripts" + File.separator;
    }

    public String getBinDirectory() {
        return binDirectory;
    }

    public void setBinDirectory(String binDirectory) {
        this.binDirectory = binDirectory;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username.toLowerCase();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public String getPasswordConfirm() {
        return passwordConfirm;
    }

    public void setPasswordConfirm(String passwordConfirm) {
        this.passwordConfirm = passwordConfirm;
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


    public String getScriptsDirectory() {
        return scriptsDirectory;
    }

    public void setScriptsDirectory(String scriptsDirectory) {
        this.scriptsDirectory = scriptsDirectory;
    }

    public String getSethlansRootDirectory() {
        return sethlansRootDirectory;
    }

    public void setSethlansRootDirectory(String sethlansRootDirectory) {
        this.sethlansRootDirectory = sethlansRootDirectory;
    }

    public String getBenchmarkDirectory() {
        return benchmarkDirectory;
    }

    public void setBenchmarkDirectory(String benchmarkDirectory) {
        this.benchmarkDirectory = benchmarkDirectory;
    }

    @Override
    public String toString() {
        return "SetupForm{" +
                "httpsPort='" + httpsPort + '\'' +
                ", username='" + username + '\'' +
                ", sethlansRootDirectory='" + sethlansRootDirectory + '\'' +
                ", selectedMethod=" + selectedMethod +
                ", availableMethods=" + availableMethods +
                ", availableGPUs=" + availableGPUs +
                ", selectedGPUId=" + selectedGPUId +
                ", blenderVersion='" + blenderVersion + '\'' +
                ", mode=" + mode +
                ", cores=" + cores +
                ", totalCores=" + totalCores +
                ", progress=" + progress +
                ", previous=" + previous +
                ", blenderBinaryOS=" + blenderBinaryOS +
                ", scriptsDirectory='" + scriptsDirectory + '\'' +
                ", projectDirectory='" + projectDirectory + '\'' +
                ", blenderDirectory='" + blenderDirectory + '\'' +
                ", tempDirectory='" + tempDirectory + '\'' +
                ", workingDirectory='" + workingDirectory + '\'' +
                ", logDirectory='" + logDirectory + '\'' +
                ", binDirectory='" + binDirectory + '\'' +
                ", benchmarkDirectory='" + benchmarkDirectory + '\'' +
                '}';
    }
}
