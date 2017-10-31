package com.dryadandnaiad.sethlans.commands;

import com.dryadandnaiad.sethlans.enums.NodeAddProgress;

/**
 * Created Mario Estrella on 10/30/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class NodeAddForm {

    private String ipAddress;
    private String port;
    private NodeAddProgress previous;
    private NodeAddProgress progress;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }


    public NodeAddProgress getPrevious() {
        return previous;
    }

    public void setPrevious(NodeAddProgress previous) {
        this.previous = previous;
    }

    public NodeAddProgress getProgress() {
        return progress;
    }

    public void setProgress(NodeAddProgress progress) {
        this.progress = progress;
    }

    @Override
    public String toString() {
        return "NodeAddForm{" +
                "ipAddress='" + ipAddress + '\'' +
                ", port='" + port + '\'' +
                ", previous=" + previous +
                ", progress=" + progress +
                '}';
    }
}
