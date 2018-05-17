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

package com.dryadandnaiad.sethlans.domains.database.node;

import com.dryadandnaiad.sethlans.domains.database.AbstractEntityClass;
import com.dryadandnaiad.sethlans.domains.hardware.CPU;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.enums.BlenderBinaryOS;
import com.dryadandnaiad.sethlans.enums.ComputeType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.OptimisticLockType;
import org.hibernate.annotations.OptimisticLocking;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import java.util.ArrayList;
import java.util.List;

/**
 * Created Mario Estrella on 10/28/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Entity
@OptimisticLocking(type = OptimisticLockType.NONE)
@Data
@EqualsAndHashCode(callSuper = false)
public class SethlansNode extends AbstractEntityClass {
    private String hostname;
    private String ipAddress;
    private String networkPort;
    private BlenderBinaryOS sethlansNodeOS;
    private ComputeType computeType;
    private CPU cpuinfo;
    private String selectedCores;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<GPUDevice> selectedGPUs = new ArrayList<>();
    @ElementCollection
    private List<String> selectedDeviceID;
    private boolean active;
    private boolean disabled;
    private boolean pendingActivation;
    private String connection_uuid;
    private int cpuRating;
    private boolean benchmarkComplete;
    private int totalRenderingSlots;
    private int availableRenderingSlots;
    private boolean cpuSlotInUse;
    private boolean gpuSlotInUse;
    private boolean combined;

    public Integer getCombinedGPURating() {
        List<Integer> gpuRatings = new ArrayList<>();
        if (this.computeType.equals(ComputeType.CPU_GPU) || this.computeType.equals(ComputeType.GPU)) {
            for (GPUDevice gpuDevice : selectedGPUs) {
                gpuRatings.add(gpuDevice.getRating());
            }
            Integer sum = 0;
            for (Integer gpuRating : gpuRatings) {
                sum += gpuRating;
            }
            return sum / selectedGPUs.size();
        }
        return 0;

    }

    public Integer getCombinedCPUGPURating() {
        int cpuRating = getCpuRating();
        int gpuRating = getCombinedGPURating();
        if (gpuRating == 0 || cpuRating == 0) {
            return gpuRating + cpuRating;
        }
        return gpuRating + cpuRating / 2;
    }

    @Override
    public String toString() {
        return "SethlansNode{" +
                "hostname='" + hostname + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", networkPort='" + networkPort + '\'' +
                ", sethlansNodeOS=" + sethlansNodeOS +
                ", computeType=" + computeType +
                ", cpuinfo=" + cpuinfo +
                ", selectedCores='" + selectedCores + '\'' +
                ", selectedGPUs=" + selectedGPUs +
                ", combinedGPURating=" + getCombinedGPURating() +
                ", combinedCPUGPURating=" + getCombinedCPUGPURating() +
                ", active=" + active +
                ", pendingActivation=" + pendingActivation +
                ", connection_uuid='" + connection_uuid + '\'' +
                ", cpuRating=" + cpuRating +
                ", benchmarkComplete=" + benchmarkComplete +
                ", gpuInUse=" + gpuSlotInUse +
                ", cpuInUse=" + cpuSlotInUse +
                ", totalSlots=" + totalRenderingSlots +
                ", availableSlots=" + availableRenderingSlots +
                '}';
    }
}
