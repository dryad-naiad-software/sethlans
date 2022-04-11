/*
 * Copyright (c) 2022 Dryad and Naiad Software LLC
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
 */

package com.dryadandnaiad.sethlans.models.query;

import com.dryadandnaiad.sethlans.enums.NodeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * File created by Mario Estrella on 4/8/2022
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NodeDashboard {
    private NodeType nodeType;
    private String cpuName;
    private String totalMemory;
    private int selectedCores;
    private Long freeSpace;
    private Long totalSpace;
    private Long usedSpace;
    private List<String> selectedGPUModels;
    private List<String> availableGPUModels;
    private int totalSlots;
    private boolean gpuCombined;
    private boolean apiKeyPresent;
    private Integer tileSizeCPU;
    private Integer tileSizeGPU;
}
