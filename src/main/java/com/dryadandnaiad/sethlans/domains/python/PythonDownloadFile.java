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

package com.dryadandnaiad.sethlans.domains.python;

/**
 * Created Mario Estrella on 3/28/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class PythonDownloadFile {
    private String binaryURL;
    private String md5;
    private String filename;

    public PythonDownloadFile(String binary, String md5, String filename) {
        this.binaryURL = binary;
        this.md5 = md5;
        this.filename = filename;
    }

    public String getBinaryURL() {
        return binaryURL;
    }

    public void setBinaryURL(String binaryURL) {
        this.binaryURL = binaryURL;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "PythonDownloadFile{" +
                "binaryURL='" + binaryURL + '\'' +
                ", md5='" + md5 + '\'' +
                ", filename='" + filename + '\'' +
                '}';
    }
}