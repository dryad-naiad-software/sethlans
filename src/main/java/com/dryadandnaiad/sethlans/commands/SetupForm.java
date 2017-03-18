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

import com.dryadandnaiad.sethlans.enums.ComputeType;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.enums.SetupProgress;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created Mario Estrella on 3/12/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class SetupForm {

    private SethlansMode mode;

    @NotEmpty
    @Size(min = 4, max = 75)
    private String userName;


    @NotEmpty
    @Size(min = 8, max = 50)
    private String passWord;

    @NotEmpty
    @Size(min = 8, max = 50)
    private String passWordConf;

    private String configDirectory;
    private String dataDirectory;
    private String projectDirectory;
    private String blenderDirectory;
    private String tempDirectory;
    private ComputeType devices;
    private String workingDirectory;
    private String blenderVersion;

    @NotNull
    @Min(1)
    @Max(65535)
    private String httpPort;

    @NotNull
    @Min(1)
    @Max(65535)
    private String httpsPort;
    private boolean useHttps;
    private SetupProgress progress;
    private SetupProgress previous;

    public SethlansMode getMode() {
        if (mode == null) {
            return SethlansMode.BOTH;
        }
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

    public String getConfigDirectory() {
        if (configDirectory == null) {
            return System.getProperty("user.home") + "/.sethlans/config/";
        }
        return configDirectory;
    }

    public void setConfigDirectory(String configDirectory) {
        this.configDirectory = configDirectory;
    }

    public String getDataDirectory() {
        if (dataDirectory == null) {
            return System.getProperty("user.home") + "/.sethlans/data/";
        }
        return dataDirectory;
    }

    public void setDataDirectory(String dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public String getProjectDirectory() {
        if (projectDirectory == null) {
            return System.getProperty("user.home") + "/.sethlans/projects/";
        }
        return projectDirectory;
    }

    public void setProjectDirectory(String projectDirectory) {
        this.projectDirectory = projectDirectory;
    }

    public String getBlenderDirectory() {
        if (blenderDirectory == null) {
            return System.getProperty("user.home") + "/.sethlans/blenderZips/";
        }
        return blenderDirectory;
    }

    public void setBlenderDirectory(String blenderDirectory) {
        this.blenderDirectory = blenderDirectory;
    }

    public String getTempDirectory() {
        if (tempDirectory == null) {
            return System.getProperty("user.home") + "/.sethlans/temp/";
        }
        return tempDirectory;
    }

    public void setTempDirectory(String tempDirectory) {
        this.tempDirectory = tempDirectory;
    }

    public ComputeType getDevices() {
        return devices;
    }

    public void setDevices(ComputeType devices) {
        this.devices = devices;
    }

    public String getWorkingDirectory() {
        if (workingDirectory == null) {
            return System.getProperty("user.home") + "/.sethlans/cache/";
        }
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

    public String getHttpPort() {
        if (httpPort == null) {
            return "7007";
        }
        return httpPort;
    }

    public void setHttpPort(String httpPort) {
        this.httpPort = httpPort;
    }

    public String getHttpsPort() {
        if (httpsPort == null) {
            return "7443";
        }
        return httpsPort;
    }

    public void setHttpsPort(String httpsPort) {
        this.httpsPort = httpsPort;
    }

    public boolean isUseHttps() {
        return useHttps;
    }

    public void setUseHttps(boolean useHttps) {
        this.useHttps = useHttps;
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

    @Override
    public String toString() {
        return "SetupForm{" +
                "mode=" + mode +
                ", userName='" + userName + '\'' +
                ", passWord='" + passWord + '\'' +
                ", passWordConf='" + passWordConf + '\'' +
                ", configDirectory='" + configDirectory + '\'' +
                ", dataDirectory='" + dataDirectory + '\'' +
                ", projectDirectory='" + projectDirectory + '\'' +
                ", blenderDirectory='" + blenderDirectory + '\'' +
                ", tempDirectory='" + tempDirectory + '\'' +
                ", devices=" + devices +
                ", workingDirectory='" + workingDirectory + '\'' +
                ", blenderVersion='" + blenderVersion + '\'' +
                ", httpPort='" + httpPort + '\'' +
                ", httpsPort='" + httpsPort + '\'' +
                ", useHttps=" + useHttps +
                ", progress=" + progress +
                ", previous=" + previous +
                '}';
    }
}
