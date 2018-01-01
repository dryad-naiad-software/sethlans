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

import com.dryadandnaiad.sethlans.domains.database.AbstractEntityClass;

import javax.persistence.Entity;

/**
 * Created Mario Estrella on 12/28/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Entity
public class BlenderRenderQueueItem extends AbstractEntityClass {
    private String connection_uuid;
    private String project_uuid;
    private BlenderFramePart blenderFramePart;
    private boolean complete;
    private boolean paused;

    public String getConnection_uuid() {
        return connection_uuid;
    }

    public void setConnection_uuid(String connection_uuid) {
        this.connection_uuid = connection_uuid;
    }

    public String getProject_uuid() {
        return project_uuid;
    }

    public void setProject_uuid(String project_uuid) {
        this.project_uuid = project_uuid;
    }

    public BlenderFramePart getBlenderFramePart() {
        return blenderFramePart;
    }

    public void setBlenderFramePart(BlenderFramePart blenderFramePart) {
        this.blenderFramePart = blenderFramePart;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public String toString() {
        return "BlenderRenderQueueItem{" +
                "connection_uuid='" + connection_uuid + '\'' +
                ", project_uuid='" + project_uuid + '\'' +
                ", blenderFramePart=" + blenderFramePart +
                ", complete=" + complete +
                ", paused=" + paused +
                '}';
    }
}
