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

package com.dryadandnaiad.sethlans.services.queue;

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.domains.database.queue.ProcessQueueItem;
import com.dryadandnaiad.sethlans.enums.ComputeType;

/**
 * Created Mario Estrella on 4/21/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public interface QueueService {

    void startQueue();

    boolean populateQueueWithProject(BlenderProject blenderProject);

    void pauseBlenderProjectQueue(BlenderProject blenderProject);

    void resumeBlenderProjectQueue(BlenderProject blenderProject);

    void stopBlenderProjectQueue(BlenderProject blenderProject);

    void addNodeToDeleteQueue(Long id);

    void nodeRejectQueueItem(String connection_uuid);

    void nodeAcknowledgeQueueItem(String queue_uuid);

    void nodeStatusUpdateItem(String connection_uuid, boolean online);

    void addItemToProcess(ProcessQueueItem processQueueItem);

    void queueIdleNode(String connection_uuid, ComputeType computeType);
}
