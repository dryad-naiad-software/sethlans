/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
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

import javax.persistence.Embeddable;

/**
 * Created Mario Estrella on 12/28/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Embeddable
public class BlenderFramePart {
    private String frameFileName;
    private int frameNumber;
    private int partNumber;
    private Double partPositionMinY;
    private Double partPositionMaxY;
    private String partFilename;
    private String fileExtension;
    private String storedDir;

    public String getFrameFileName() {
        return frameFileName;
    }

    public void setFrameFileName(String frameFileName) {
        this.frameFileName = frameFileName;
    }

    public int getFrameNumber() {
        return frameNumber;
    }

    public void setFrameNumber(int frameNumber) {
        this.frameNumber = frameNumber;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    public Double getPartPositionMinY() {
        return partPositionMinY;
    }

    public void setPartPositionMinY(Double partPositionMinY) {
        this.partPositionMinY = partPositionMinY;
    }

    public Double getPartPositionMaxY() {
        return partPositionMaxY;
    }

    public void setPartPositionMaxY(Double partPositionMaxY) {
        this.partPositionMaxY = partPositionMaxY;
    }

    public String getPartFilename() {
        return partFilename;
    }

    public void setPartFilename(String partFilename) {
        this.partFilename = partFilename;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public String getStoredDir() {
        return storedDir;
    }

    public void setStoredDir(String storedDir) {
        this.storedDir = storedDir;
    }

    @Override
    public String toString() {
        return "BlenderFramePart{" +
                "frameFileName='" + frameFileName + '\'' +
                ", frameNumber=" + frameNumber +
                ", partNumber=" + partNumber +
                ", partPositionMinY=" + partPositionMinY +
                ", partPositionMaxY=" + partPositionMaxY +
                ", partFilename='" + partFilename + '\'' +
                ", fileExtension='" + fileExtension + '\'' +
                ", storedDir='" + storedDir + '\'' +
                '}';
    }
}
