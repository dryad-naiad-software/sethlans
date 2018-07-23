package com.dryadandnaiad.sethlans.domains.info;

import com.dryadandnaiad.sethlans.enums.ComputeType;
import lombok.Data;

import java.util.List;

/**
 * Created Mario Estrella on 7/23/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: Sethlans
 */
@Data
public class NodeDashBoardInfo {
    private ComputeType computeType;
    private String cpuName;
    private String totalMemory;
    private String selectedCores;
    private Long freeSpace;
    private Long totalSpace;
    private Long usedSpace;
    private List<String> selectedGPUModels;
    private List<String> availableGPUModels;
    private int totalSlots;
    private boolean gpuCombined;
}
