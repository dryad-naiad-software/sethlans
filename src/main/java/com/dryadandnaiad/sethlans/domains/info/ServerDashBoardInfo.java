package com.dryadandnaiad.sethlans.domains.info;

import lombok.Data;

import java.util.List;

/**
 * Created Mario Estrella on 7/23/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: Sethlans
 */
@Data
public class ServerDashBoardInfo {
    private int totalNodes;
    private int activeNodes;
    private int inactiveNodes;
    private int disabledNodes;
    private int totalSlots;
    private String cpuName;
    private String totalMemory;
    private Long freeSpace;
    private Long totalSpace;
    private Long usedSpace;
    private List<Integer> numberOfActiveNodesArray;

}
