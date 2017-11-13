/*
 * Copyright (C) 2010-2017 Laurent CLOUET
 * Author Laurent CLOUET <laurent.clouet@nopnop.net>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package com.dryadandnaiad.sethlans.osnative.hardware.gpu;

/**
 * Created Mario Estrella on 3/19/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */

import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import jcuda.driver.CUresult;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

public class GPU {
    private static final Logger LOG = LoggerFactory.getLogger(GPU.class);
    public static List<GPUDevice> devices = null;

    public static boolean generate() {
        devices = new LinkedList<>();
        String path = getCUDALib();
        if (path == null) {
            LOG.debug("GPU::generate no CUDA lib path found");
            return false;
        }
        CUDA cudalib = null;
        try {
            cudalib = (CUDA) Native.loadLibrary(path, CUDA.class);
        } catch (java.lang.UnsatisfiedLinkError e) {
            LOG.debug("GPU::generate failed to load CUDA lib (path: " + path + ")");
            return false;
        } catch (java.lang.ExceptionInInitializerError e) {
            LOG.error("GPU::generate ExceptionInInitializerError " + e.getMessage());
            return false;
        } catch (Exception e) {
            LOG.error("GPU::generate generic exception" + e.getMessage());
            return false;
        }

        int result = CUresult.CUDA_ERROR_UNKNOWN;

        result = cudalib.cuInit(0);
        if (result != CUresult.CUDA_SUCCESS) {
            LOG.info("GPU::generate cuInit failed (ret: " + result + ")");
            if (result == CUresult.CUDA_ERROR_UNKNOWN) {
                LOG.info("If you are running Linux, this error is usually due to nvidia kernel module 'nvidia_uvm' not loaded. " +
                        "\nRelaunch the application as root or load the module. " +
                        "\nMost of time it does fix the issue.");
            }
            return false;
        }

        if (result == CUresult.CUDA_ERROR_NO_DEVICE) {
            return false;
        }

        IntByReference count = new IntByReference();
        result = cudalib.cuDeviceGetCount(count);

        if (result != CUresult.CUDA_SUCCESS) {
            LOG.debug("GPU::generate cuDeviceGetCount failed (ret: " + CUresult.stringFor(result) + ")");
            return false;
        }

        for (int num = 0; num < count.getValue(); num++) {
            byte name[] = new byte[256];

            result = cudalib.cuDeviceGetName(name, 256, num);
            if (result != CUresult.CUDA_SUCCESS) {
                LOG.debug("GPU::generate cuDeviceGetName failed (ret: " + CUresult.stringFor(result) + ")");
                continue;
            }

            LongByReference ram = new LongByReference();
            try {
                result = cudalib.cuDeviceTotalMem_v2(ram, num);
            } catch (UnsatisfiedLinkError e) {
                // fall back to old function
                result = cudalib.cuDeviceTotalMem(ram, num);
            }

            if (result != CUresult.CUDA_SUCCESS) {
                LOG.debug("GPU::generate cuDeviceTotalMem failed (ret: " + CUresult.stringFor(result) + ")");
                return false;
            }

            devices.add(new GPUDevice(new String(name).trim(), ram.getValue(), "CUDA_" + Integer.toString(num)));
        }
        return true;
    }

    private static String getCUDALib() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "nvcuda";
        }
        if (SystemUtils.IS_OS_MAC) {
            return "/usr/local/cuda/lib/libcuda.dylib";
        }
        if (SystemUtils.IS_OS_LINUX) {
            return "cuda";
        }
        return null;
    }

    public static List<String> listModels() {
        if (devices == null) {
            generate();
        }

        List<String> devs = new LinkedList<String>();
        for (GPUDevice dev : devices) {
            devs.add(dev.getModel());
        }
        return devs;
    }

    public static List<GPUDevice> listDevices() {
        if (devices == null) {
            generate();
        }

        return devices;
    }

    public static GPUDevice getGPUDevice(String device_model) {
        if (device_model == null) {
            return null;
        }

        if (devices == null) {
            generate();
        }

        if (devices == null) {
            return null;
        }

        for (GPUDevice dev : devices) {
            if (device_model.equals(dev.getCudaName()) || device_model.equals(dev.getModel())) {
                return dev;
            }
        }
        return null;
    }
}