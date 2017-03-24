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

package com.dryadandnaiad.sethlans.domains;

import com.dryadandnaiad.sethlans.enums.BlenderBinaryOS;

import javax.persistence.Entity;

/**
 * Created Mario Estrella on 3/23/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Entity
public class BlenderEntity extends AbstractEntityClass {
    private String blenderVersion;
    private BlenderBinaryOS blenderBinaryOS;

    public String getBlenderVersion() {
        return blenderVersion;
    }

    public void setBlenderVersion(String blenderVersion) {
        this.blenderVersion = blenderVersion;
    }

    public BlenderBinaryOS getBlenderBinaryOS() {
        return blenderBinaryOS;
    }

    public void setBlenderBinaryOS(BlenderBinaryOS blenderBinaryOS) {
        this.blenderBinaryOS = blenderBinaryOS;
    }
}
