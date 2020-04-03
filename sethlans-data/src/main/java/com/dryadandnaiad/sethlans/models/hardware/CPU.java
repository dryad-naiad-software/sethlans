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


package com.dryadandnaiad.sethlans.models.hardware;

import com.dryadandnaiad.sethlans.models.BaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.util.FormatUtil;

import javax.persistence.Entity;

/**
 * Created by Mario Estrella on 3/29/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Entity
@Data
@NoArgsConstructor
public class CPU extends BaseEntity {
    private String name;
    private String model;
    private String family;
    private String arch; // 32 or 64 bits
    private int cores;
    private int cpuPackage;
    private String totalMemory;


    public void populateCPU() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        CentralProcessor processor = hal.getProcessor();
        GlobalMemory globalMemory = hal.getMemory();
        this.name = processor.getProcessorIdentifier().getName();
        this.model = processor.getProcessorIdentifier().getModel();
        this.family = processor.getProcessorIdentifier().getFamily();
        this.cores = processor.getLogicalProcessorCount();
        this.cpuPackage = processor.getPhysicalPackageCount();
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
