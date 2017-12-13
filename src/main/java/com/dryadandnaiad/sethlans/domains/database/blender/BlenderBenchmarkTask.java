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

import javax.persistence.Entity;

/**
 * Created Mario Estrella on 12/12/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Entity
public class BlenderBenchmarkTask extends AbstractEntityClass {
    private String benchmarkURL;
    private String blenderVersion;
    private String server_uuid;
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

    public String getServer_uuid() {
        return server_uuid;
    }

    public void setServer_uuid(String server_uuid) {
        this.server_uuid = server_uuid;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "BlenderBenchmarkTask{" +
                "benchmarkURL='" + benchmarkURL + '\'' +
                ", blenderVersion='" + blenderVersion + '\'' +
                ", server_uuid='" + server_uuid + '\'' +
                ", rating=" + rating +
                '}';
    }
}
