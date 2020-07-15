/*
 * Copyright (c) 2020. Dryad and Naiad Software LLC.
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.dryadandnaiad.sethlans.models.blender.tasks;

import com.dryadandnaiad.sethlans.models.AbstractModel;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.persistence.Entity;

/**
 * File created by Mario Estrella on 4/2/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@ToString
@EqualsAndHashCode(callSuper = true)
public class RenderTask extends AbstractModel {
    private String blenderVersion;
    private String blenderExecutable;
    private String taskID;
    private String taskBlendFile;
    private String taskBlendFileMD5Sum;
    private String taskDir;
    private String projectName;
    private String projectID;
    private Long renderTime;
    private boolean benchmark;
    private boolean cancelRequestReceived;
    private boolean complete;
    private boolean inProgress;
    private boolean useParts;
    private TaskFrameInfo frameInfo;
    private TaskScriptInfo scriptInfo;
    private TaskServerInfo serverInfo;
}
