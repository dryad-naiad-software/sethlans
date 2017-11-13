package com.dryadandnaiad.sethlans.domains.database.node;

import com.dryadandnaiad.sethlans.domains.database.AbstractEntityClass;
import com.dryadandnaiad.sethlans.domains.hardware.CPU;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.enums.BlenderBinaryOS;
import com.dryadandnaiad.sethlans.enums.ComputeType;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 10/28/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Entity
public class SethlansNode extends AbstractEntityClass {
    private String hostname;
    private String ipAddress;
    private String networkPort;
    private BlenderBinaryOS sethlansNodeOS;
    private ComputeType computeType;
    private CPU cpuinfo;
    private String selectedCores;
    @ElementCollection
    private List<GPUDevice> selectedGPUs = new ArrayList<>();
    @ElementCollection
    private List<String> selectedCUDA;
    private boolean active;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getNetworkPort() {
        return networkPort;
    }

    public void setNetworkPort(String networkPort) {
        this.networkPort = networkPort;
    }

    public BlenderBinaryOS getSethlansNodeOS() {
        return sethlansNodeOS;
    }

    public void setSethlansNodeOS(BlenderBinaryOS sethlansNodeOS) {
        this.sethlansNodeOS = sethlansNodeOS;
    }

    public ComputeType getComputeType() {
        return computeType;
    }

    public void setComputeType(ComputeType computeType) {
        this.computeType = computeType;
    }

    public List<GPUDevice> getSelectedGPUs() {
        return selectedGPUs;
    }

    public void setSelectedGPUs(List<GPUDevice> selectedGPUs) {
        this.selectedGPUs = selectedGPUs;
    }

    public List<String> getSelectedCUDA() {
        return selectedCUDA;
    }

    public void setSelectedCUDA(List<String> selectedCUDA) {
        this.selectedCUDA = selectedCUDA;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public CPU getCpuinfo() {
        return cpuinfo;
    }

    public void setCpuinfo(CPU cpuinfo) {
        this.cpuinfo = cpuinfo;
    }

    public String getSelectedCores() {
        return selectedCores;
    }

    public void setSelectedCores(String selectedCores) {
        this.selectedCores = selectedCores;
    }

    @Override
    public String toString() {
        return "SethlansNode{" +
                "hostname='" + hostname + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", networkPort='" + networkPort + '\'' +
                ", sethlansNodeOS=" + sethlansNodeOS +
                ", computeType=" + computeType +
                ", cpuinfo=" + cpuinfo +
                ", selectedCores='" + selectedCores + '\'' +
                ", selectedGPUs=" + selectedGPUs +
                ", selectedCUDA=" + selectedCUDA +
                ", active=" + active +
                '}';
    }
}
