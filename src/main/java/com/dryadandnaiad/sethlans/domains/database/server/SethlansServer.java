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

package com.dryadandnaiad.sethlans.domains.database.server;

import com.dryadandnaiad.sethlans.domains.database.AbstractEntityClass;

import javax.persistence.Entity;

/**
 * Created Mario Estrella on 12/4/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Entity
public class SethlansServer extends AbstractEntityClass {
    private String hostname;
    private String ipAddress;
    private String networkPort;
    private String connection_uuid;
    private boolean acknowledged;
    private boolean pendingAcknowledgementResponse;

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

    public String getConnection_uuid() {
        return connection_uuid;
    }

    public void setConnection_uuid(String connection_uuid) {
        this.connection_uuid = connection_uuid;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public void setPendingAcknowledgementResponse(boolean pendingAcknowledgementResponse) {
        this.pendingAcknowledgementResponse = pendingAcknowledgementResponse;
    }

    public boolean isPendingAcknowledgementResponse() {
        return pendingAcknowledgementResponse;
    }

    @Override
    public String toString() {
        return "SethlansServer{" +
                "hostname='" + hostname + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", networkPort='" + networkPort + '\'' +
                ", connection_uuid='" + connection_uuid + '\'' +
                ", acknowledged=" + acknowledged +
                ", pendingAcknowledgementResponse=" + pendingAcknowledgementResponse +
                '}';
    }
}
