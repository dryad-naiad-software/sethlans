/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC
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

package com.dryadandnaiad.sethlans.services.blender.benchmark;

import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

/**
 * Created Mario Estrella on 12/10/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public interface BlenderBenchmarkService {
    void sendBenchmarktoNode(SethlansNode sethlansNode);

    @Async
    void benchmarkOnNodeRestart();

    @Async
    void processReceivedBenchmark(String benchmark_uuid);

    @Async
    void processReceivedBenchmarks(List<String> benchmark_uuids);

}
