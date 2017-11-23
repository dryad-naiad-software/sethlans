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
    private Integer id;
    private Integer version;


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


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "NodeAddForm{" +
                "ipAddress='" + ipAddress + '\'' +
                ", port='" + port + '\'' +
                ", previous=" + previous +
                ", progress=" + progress +
                ", id=" + id +
                ", version=" + version +
                '}';
    }
}
