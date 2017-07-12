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

package com.dryadandnaiad.sethlans.commands;

import com.dryadandnaiad.sethlans.enums.BlenderBinaryOS;
import com.dryadandnaiad.sethlans.osnative.hardware.gpu.GPU;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;


/**
 * Created Mario Estrella on 7/10/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */


public class SetupFormTest {

    private SetupForm target = null;
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SetupFormTest.class);

    @Before
    public void setup() {

        target = new SetupForm();

    }

    @Test
    public void test_populate_blenderbinary_os() {
        List<BlenderBinaryOS> blenderBinaryOS = target.getBlenderBinaryOS();
        assertNotNull(blenderBinaryOS);

        if (SystemUtils.IS_OS_WINDOWS) {
            String arch = System.getenv("PROCESSOR_ARCHITECTURE");
            String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
            String realArch = arch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64")
                    ? "64" : "32";
            if (realArch.equals("64")) {
                assertEquals(BlenderBinaryOS.Windows64, target.getBlenderBinaryOS().get(0));
            } else {
                assertEquals(BlenderBinaryOS.Windows32, target.getBlenderBinaryOS().get(0));
            }

        }

        if (SystemUtils.IS_OS_MAC) {
            assertEquals(BlenderBinaryOS.MacOS, target.getBlenderBinaryOS().get(0));
        }

        if (SystemUtils.IS_OS_LINUX) {
            String arch = System.getProperty("os.arch");
            if (arch.equals("x86")) {
                assertEquals(BlenderBinaryOS.Linux32, target.getBlenderBinaryOS().get(0));
            } else {
                assertEquals(BlenderBinaryOS.Linux64, target.getBlenderBinaryOS().get(0));
            }
        }

    }

    @Test
    public void test_cores_not_zero() {
        assertTrue("Cores " + target.getTotalCores() + " should be greater than zero", target.getTotalCores() > 0);
    }

    @Test
    public void test_available_methods() {
        if (GPU.listDevices().size() != 0) {
            assertEquals(3, target.getAvailableMethods().size());
        } else {
            assertEquals(1, target.getAvailableMethods().size());
        }


    }
}
