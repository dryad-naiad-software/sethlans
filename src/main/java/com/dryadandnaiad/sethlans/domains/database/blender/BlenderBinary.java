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
import org.springframework.context.annotation.Profile;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import java.util.List;

/**
 * Created Mario Estrella on 3/23/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Entity
@Profile({"SERVER", "DUAL"})
public class BlenderBinary extends AbstractEntityClass {
    private String blenderVersion;
    private String blenderBinaryOS;
    private String blenderFile;
    private String blenderFileMd5;
    @ElementCollection
    private List<String> downloadMirrors;
    private boolean downloaded = false;

    public String getBlenderVersion() {
        return blenderVersion;
    }

    public void setBlenderVersion(String blenderVersion) {
        this.blenderVersion = blenderVersion;
    }

    public String getBlenderBinaryOS() {
        return blenderBinaryOS;
    }

    public void setBlenderBinaryOS(String blenderBinaryOS) {
        this.blenderBinaryOS = blenderBinaryOS;
    }

    public String getBlenderFile() {
        return blenderFile;
    }

    public void setBlenderFile(String blenderFile) {
        this.blenderFile = blenderFile;
    }

    public String getBlenderFileMd5() {
        return blenderFileMd5;
    }

    public void setBlenderFileMd5(String blenderFileMd5) {
        this.blenderFileMd5 = blenderFileMd5;
    }

    public List<String> getDownloadMirrors() {
        return downloadMirrors;
    }

    public void setDownloadMirrors(List<String> downloadMirrors) {
        this.downloadMirrors = downloadMirrors;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    @Override
    public String toString() {
        return "BlenderBinary{" +
                "blenderVersion='" + blenderVersion + '\'' +
                ", blenderBinaryOS='" + blenderBinaryOS + '\'' +
                ", blenderFile='" + blenderFile + '\'' +
                ", blenderFileMd5='" + blenderFileMd5 + '\'' +
                ", downloadMirrors=" + downloadMirrors +
                ", downloaded=" + downloaded +
                '}';
    }
}