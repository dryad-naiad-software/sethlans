/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC
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

package com.dryadandnaiad.sethlans.osnative.hardware.gpu;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created Mario Estrella on 7/14/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class GPUTest {

    @Test
    public void test_gpu_lists_not_null() {
        if (GPU.generate()) {
            System.out.println("GPU(s) detected");
            Assert.assertNotNull(GPU.devices);
            Assert.assertNotNull(GPU.listDevices());
            Assert.assertNotNull(GPU.listModels());
        }
    }
}
