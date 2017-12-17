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

package com.dryadandnaiad.sethlans.domains.database.blender;

import com.dryadandnaiad.sethlans.domains.database.AbstractEntityClass;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import org.springframework.context.annotation.Profile;

import javax.persistence.Entity;

/**
 * Created Mario Estrella on 12/12/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Entity
@Profile({"NODE", "DUAL"})
public class BlenderBenchmarkTask extends AbstractEntityClass {
    private String benchmarkURL;
    private String blenderVersion;
    private String connection_uuid;
    private String benchmark_uuid;
    private ComputeType computeType;
    private String blenderExecutable;
    private int rating;

    public String getBenchmarkURL() {
        return benchmarkURL;
    }

    public void setBenchmarkURL(String benchmarkURL) {
        this.benchmarkURL = benchmarkURL;
    }

    public String getBlenderVersion() {
        return blenderVersion;
    }

    public void setBlenderVersion(String blenderVersion) {
        this.blenderVersion = blenderVersion;
    }

    public String getConnection_uuid() {
        return connection_uuid;
    }

    public void setConnection_uuid(String connection_uuid) {
        this.connection_uuid = connection_uuid;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public ComputeType getComputeType() {
        return computeType;
    }

    public void setComputeType(ComputeType computeType) {
        this.computeType = computeType;
    }

    public String getBenchmark_uuid() {
        return benchmark_uuid;
    }

    public void setBenchmark_uuid(String benchmark_uuid) {
        this.benchmark_uuid = benchmark_uuid;
    }

    public String getBlenderExecutable() {
        return blenderExecutable;
    }

    public void setBlenderExecutable(String blenderExecutable) {
        this.blenderExecutable = blenderExecutable;
    }

    @Override
    public String toString() {
        return "BlenderBenchmarkTask{" +
                "benchmarkURL='" + benchmarkURL + '\'' +
                ", blenderVersion='" + blenderVersion + '\'' +
                ", connection_uuid='" + connection_uuid + '\'' +
                ", benchmark_uuid='" + benchmark_uuid + '\'' +
                ", computeType=" + computeType +
                ", blenderExecutable='" + blenderExecutable + '\'' +
                ", rating=" + rating +
                '}';
    }
}
