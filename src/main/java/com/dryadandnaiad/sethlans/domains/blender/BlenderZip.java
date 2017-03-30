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

package com.dryadandnaiad.sethlans.domains.blender;

import java.util.List;

/**
 * Created Mario Estrella on 3/21/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class BlenderZip {
    private String blenderVersion;
    private List<String> windows32;
    private List<String> windows64;
    private List<String> macOS;
    private List<String> linux32;
    private List<String> linux64;
    private String md5MacOS;
    private String md5Windows64;
    private String md5Windows32;
    private String md5Linux32;
    private String md5Linux64;

    public BlenderZip(String blenderVersion, List<String> windows32, List<String> windows64, List<String> macOS, List<String> linux32, List<String> linux64, String md5MacOS, String md5Windows64, String md5Windows32, String md5Linux32, String md5Linux64) {
        this.blenderVersion = blenderVersion;
        this.windows32 = windows32;
        this.windows64 = windows64;
        this.macOS = macOS;
        this.linux32 = linux32;
        this.linux64 = linux64;
        this.md5MacOS = md5MacOS;
        this.md5Windows64 = md5Windows64;
        this.md5Windows32 = md5Windows32;
        this.md5Linux32 = md5Linux32;
        this.md5Linux64 = md5Linux64;
    }

    public String getBlenderVersion() {
        return blenderVersion;
    }

    public String getMd5MacOS() {
        return md5MacOS;
    }

    public String getMd5Windows64() {
        return md5Windows64;
    }

    public String getMd5Windows32() {
        return md5Windows32;
    }

    public String getMd5Linux32() {
        return md5Linux32;
    }

    public String getMd5Linux64() {
        return md5Linux64;
    }

    public List<String> getWindows32() {
        return windows32;
    }

    public List<String> getWindows64() {
        return windows64;
    }

    public List<String> getMacOS() {
        return macOS;
    }

    public List<String> getLinux32() {
        return linux32;
    }

    public List<String> getLinux64() {
        return linux64;
    }

    @Override
    public String toString() {
        return "BlenderZip{" +
                "blenderVersion='" + blenderVersion + '\'' +
                ", windows32=" + windows32 +
                ", windows64=" + windows64 +
                ", macOS=" + macOS +
                ", linux32=" + linux32 +
                ", linux64=" + linux64 +
                ", md5MacOS='" + md5MacOS + '\'' +
                ", md5Windows64='" + md5Windows64 + '\'' +
                ", md5Windows32='" + md5Windows32 + '\'' +
                ", md5Linux32='" + md5Linux32 + '\'' +
                ", md5Linux64='" + md5Linux64 + '\'' +
                '}';
    }
}
