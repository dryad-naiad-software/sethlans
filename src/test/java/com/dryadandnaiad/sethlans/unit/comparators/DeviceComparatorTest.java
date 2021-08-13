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

package com.dryadandnaiad.sethlans.unit.comparators;

import com.dryadandnaiad.sethlans.comparators.DeviceComparator;
import com.dryadandnaiad.sethlans.models.hardware.Device;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * File created by Mario Estrella on 4/26/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
class DeviceComparatorTest {

    @Test
    void compare() {
        var device1 = Device.builder()
                .deviceBenchmark(4000).deviceID("CUDA_0").nodeID("abcde").assigned(false).build();
        var device2 = Device.builder()
                .deviceBenchmark(6000).deviceID("OPENCL_0").nodeID("fghij").assigned(false).build();
        var device3 = Device.builder()
                .deviceBenchmark(3000).deviceID("OPENCL_1").nodeID("fghij").assigned(false).build();
        var device4 = Device.builder()
                .deviceBenchmark(1234).deviceID("CUDA_1").nodeID("abcde").assigned(false).build();
        var devices = new ArrayList<Device>();
        devices.add(device1);
        devices.add(device2);
        devices.add(device3);
        devices.add(device4);
        var sortedDevices = new ArrayList<>(devices);
        sortedDevices.sort(new DeviceComparator());
        assertThat(sortedDevices.get(0)).isEqualTo(device4);
        assertThat(sortedDevices.get(3)).isEqualTo(device2);

    }
}
