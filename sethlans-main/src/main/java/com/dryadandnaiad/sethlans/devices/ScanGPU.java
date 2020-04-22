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

package com.dryadandnaiad.sethlans.devices;


import com.dryadandnaiad.sethlans.models.hardware.GPU;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import jcuda.driver.CUresult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

import java.util.ArrayList;
import java.util.List;

import static com.sun.jna.Native.load;

/**
 * Created by Mario Estrella on 4/22/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class ScanGPU {
    private static final int CL_DEVICE_BOARD_NAME_AMD = 0x4038;
    private static List<GPU> devices;

    private static void generateCUDA() {
        log.info("Looking for Compatible CUDA Devices");

        String path = getCUDALib();
        if (path == null) {
            log.info("no CUDA lib path found");

        }
        CUDA cudalib;
        int result;
        try {
            cudalib = load(path, CUDA.class);
            result = cudalib.cuInit(0);
            if (result != CUresult.CUDA_SUCCESS) {
                log.error("cuInit failed (ret: " + result + ")");
                if (result == CUresult.CUDA_ERROR_UNKNOWN) {
                    log.error("If you are running Linux, this error is usually due to nvidia kernel module 'nvidia_uvm' not loaded. " +
                            "Relaunch the application as root or load the module. " +
                            "Most of time it does fix the issue.");
                    return;
                }
            }

            if (result == CUresult.CUDA_ERROR_NO_DEVICE) {
                log.info("No Device Found");
                return;
            }

            IntByReference count = new IntByReference();
            result = cudalib.cuDeviceGetCount(count);

            if (result != CUresult.CUDA_SUCCESS) {
                log.error("cuDeviceGetCount failed (ret: " + CUresult.stringFor(result) + ")");
                return;
            }

            for (int num = 0; num < count.getValue(); num++) {
                byte[] name = new byte[256];

                result = cudalib.cuDeviceGetName(name, 256, num);
                if (result != CUresult.CUDA_SUCCESS) {
                    log.error("cuDeviceGetName failed (ret: " + CUresult.stringFor(result) + ")");
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
                    log.error("cuDeviceTotalMem failed (ret: " + CUresult.stringFor(result) + ")");
                    return;
                }

                log.info("One CUDA Device found, adding to list.");
                devices.add(GPU.builder()
                        .model(new String(name).trim())
                        .memory(ram.getValue())
                        .deviceID("CUDA_" + num)
                        .openCLDevice(false)
                        .cudaDevice(true)
                        .build());
            }

        } catch (UnsatisfiedLinkError e) {
            log.error("Failed to load CUDA lib (path: " + path + "). CUDA is probably not installed.");
        } catch (ExceptionInInitializerError e) {
            log.error("ExceptionInInitializerError " + e.getMessage());

        } catch (Exception e) {
            log.error("Generic exception" + e.getMessage());

        }
    }

    private static void generateOpenCL() {
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

    public static List<GPU> listDevices() {
        devices = new ArrayList<>();
        generateCUDA();
        generateOpenCL();
        return devices;
    }


}
