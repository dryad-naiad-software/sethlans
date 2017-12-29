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

import com.dryadandnaiad.sethlans.domains.blender.BlenderFramePart;
import com.dryadandnaiad.sethlans.domains.database.AbstractEntityClass;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import java.util.List;

/**
 * Created Mario Estrella on 12/28/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */

@Entity
public class BlenderFrame extends AbstractEntityClass {
    private String frameFileName;
    private int frameNumber;
    @ElementCollection
    private List<BlenderFramePart> blenderFrameParts;

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

    public List<BlenderFramePart> getBlenderFrameParts() {
        return blenderFrameParts;
    }

    public void setBlenderFrameParts(List<BlenderFramePart> blenderFrameParts) {
        this.blenderFrameParts = blenderFrameParts;
    }

    @Override
    public String toString() {
        return "BlenderFrame{" +
                "frameFileName='" + frameFileName + '\'' +
                ", frameNumber=" + frameNumber +
                ", blenderFrameParts=" + blenderFrameParts +
                '}';
    }
}
