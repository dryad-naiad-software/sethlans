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

package com.dryadandnaiad.sethlans.components;

import com.dryadandnaiad.sethlans.services.BenchmarkService;
import com.dryadandnaiad.sethlans.services.MulticastService;
import com.dryadandnaiad.sethlans.services.RenderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * File created by Mario Estrella on 6/11/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Component
@Profile({"NODE", "DUAL"})
@Slf4j
public class NodeBackgroundComponent {
    private final MulticastService multicastService;
    private final BenchmarkService benchmarkService;
    private final RenderService renderTaskService;

    public NodeBackgroundComponent(MulticastService multicastService, BenchmarkService benchmarkService,
                                   RenderService renderTaskService) {
        this.multicastService = multicastService;
        this.benchmarkService = benchmarkService;
        this.renderTaskService = renderTaskService;
    }

    @PostConstruct
    public void startServices() {
        multicastService.sendSethlansMulticast();
        benchmarkService.pendingBenchmarks();
        renderTaskService.retrievePendingRenderTasks();
        renderTaskService.executeRenders();
    }
}
