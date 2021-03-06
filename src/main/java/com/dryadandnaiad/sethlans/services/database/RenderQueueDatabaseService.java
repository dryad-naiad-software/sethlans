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
package com.dryadandnaiad.sethlans.services.database;

import com.dryadandnaiad.sethlans.domains.database.queue.RenderQueueItem;

import java.util.List;

/**
 * Created Mario Estrella on 12/28/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */

public interface RenderQueueDatabaseService extends CRUDService<RenderQueueItem> {
    List<RenderQueueItem> listPendingRender();

    boolean checkExistingProjectIndex(String projectUUID, int index);

    List<RenderQueueItem> listPendingRenderWithNodeAssigned();

    RenderQueueItem getByQueueUUID(String queueUUID);

    void deleteAllByProject(String project_uuid);

    void delete(RenderQueueItem renderQueueItem);

    List<RenderQueueItem> listQueueItemsByConnectionUUID(String connection_uuid);

    List<RenderQueueItem> listQueueItemsByProjectUUID(String project_uuid);



}
