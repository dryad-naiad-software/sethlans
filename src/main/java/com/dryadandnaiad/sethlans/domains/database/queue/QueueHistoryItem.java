/*
 * Copyright (c) 2019 Dryad and Naiad Software LLC
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

package com.dryadandnaiad.sethlans.domains.database.queue;

import com.dryadandnaiad.sethlans.domains.database.AbstractEntityClass;
import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Entity;

/**
 * Created Mario Estrella on 3/31/2019.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = false)
public class QueueHistoryItem extends AbstractEntityClass {
    private Long taskDate;
    private String nodeName;
    private BlenderEngine engine;
    private ComputeType computeType;
    private String projectName;
    private String deviceId;
    private String queueItemUUID;
    private boolean complete;
    private boolean paused;
    private boolean rendering;
    private boolean failed;
    String frameAndPartNumbers;
}
