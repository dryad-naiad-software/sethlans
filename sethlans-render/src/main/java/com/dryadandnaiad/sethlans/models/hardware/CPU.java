package com.dryadandnaiad.sethlans.models.hardware;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.util.FormatUtil;

/**
 * Created Mario Estrella on 3/29/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class CPU {
    private String name;
    private String model;
    private String family;
    private String arch; // 32 or 64 bits
    private int cores;
    private String totalMemory;

    public CPU() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        CentralProcessor processor = hal.getProcessor();
        GlobalMemory globalMemory = hal.getMemory();
        this.name = processor.getProcessorIdentifier().getName();
        this.model = processor.getProcessorIdentifier().getModel();
        this.family = processor.getProcessorIdentifier().getFamily();
        this.cores = processor.getLogicalProcessorCount();
        this.totalMemory = FormatUtil.formatBytes(globalMemory.getTotal());
        this.generateArch();
    }


    public void generateArch() {
        String arch = System.getProperty("os.arch").toLowerCase();
        switch (arch) {
            case "i386":
            case "i686":
            case "x86":
                this.arch = "32bit";
                break;
            case "amd64":
            case "x86_64":
                this.arch = "64bit";
                break;
            default:
                this.arch = null;
                break;
        }
    }
}
