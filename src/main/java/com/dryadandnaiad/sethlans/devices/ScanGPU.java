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
import com.google.common.base.Throwables;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import jcuda.driver.CUresult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import org.jocl.cl_device_id;
import org.jocl.cl_platform_id;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.sun.jna.Native.load;
import static org.jocl.CL.*;

/**
 * File created by Mario Estrella on 4/22/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class ScanGPU {
    private static final int CL_DEVICE_BOARD_NAME_AMD = 0x4038;
    private static List<GPU> devices;

    private static void generateCUDA() {
        boolean optix;
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

                String modelName = new String(name).trim();


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

                optix = modelName.contains("RTX");

                String gpuID;

                if (optix) {
                    gpuID = "OPTIX_" + num;

                } else {
                    gpuID = "CUDA_" + num;
                }

                log.info("One CUDA Device found, adding to list.");
                devices.add(GPU.builder()
                        .model(modelName)
                        .memory(ram.getValue())
                        .gpuID(gpuID)
                        .openCLDevice(false)
                        .cudaDevice(true)
                        .optixDevice(optix)
                        .build());
            }

        } catch (UnsatisfiedLinkError e) {
            log.error("Failed to load CUDA lib (path: " + path + "). CUDA is probably not installed.");
            log.error(e.getMessage());
        } catch (ExceptionInInitializerError e) {
            log.error("ExceptionInInitializerError: " + e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
        } catch (Exception e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
        }
    }

    private static void generateOpenCL() {
        try {
            log.info("Looking for Compatible OpenCL Devices");
            if (devices == null) {
                devices = new LinkedList<>();
            }
            int[] numPlatforms = new int[1];
            clGetPlatformIDs(0, null, numPlatforms);
            String model;
            long memory;
            String gpuID;
            // Obtain the platform IDs
            cl_platform_id[] platforms = new cl_platform_id[numPlatforms[0]];
            clGetPlatformIDs(platforms.length, platforms, null);

            // Collect all devices of all platforms
            List<cl_device_id> openCLdevices = new ArrayList<>();
            for (cl_platform_id platform : platforms) {

                // Obtain the number of devices for the current platform
                int[] numDevices = new int[1];
                clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, 0, null, numDevices);
                cl_device_id[] devicesArray = new cl_device_id[numDevices[0]];
                clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, numDevices[0], devicesArray, null);

                openCLdevices.addAll(Arrays.asList(devicesArray));
            }

            for (int i = 0; i < openCLdevices.size(); i++) {
                cl_device_id device = openCLdevices.get(i);

                boolean invalidModel = false;
                String deviceVendor = JOCLSupport.getString(device, CL_DEVICE_VENDOR);
                if (deviceVendor.toLowerCase().contains("nvidia") || deviceVendor.toLowerCase().contains("intel")) {
                    log.debug("Invalid OpenCL graphics card vendor detected, skipping further analysis. Intel and NVIDIA OpenCL is not supported.");
                    invalidModel = true;
                }

                if (!invalidModel) {
                    String openCLVersionString = JOCLSupport.getString(device, CL_DEVICE_OPENCL_C_VERSION);
                    String openCLgpuID = JOCLSupport.getString(device, CL_DEVICE_BOARD_NAME_AMD);
                    memory = JOCLSupport.getLong(device, CL_DEVICE_GLOBAL_MEM_SIZE);
                    float openCLVersion = Float.parseFloat(openCLVersionString.substring(openCLVersionString.toLowerCase().lastIndexOf("c") + 1));
                    gpuID = "OPENCL_" + i;
                    model = openCLgpuID;
                    if (openCLVersion > 1.2) {
                        log.info("One OpenCL device found, adding to list");
                        log.debug("Open CL version " + openCLVersion);
                        devices.add(GPU.builder()
                                .model(model)
                                .memory(memory)
                                .gpuID(gpuID)
                                .openCLDevice(true)
                                .cudaDevice(false)
                                .build());
                    }
                }
            }
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            log.error(e.getMessage() + " Most likely, OpenCL not present on system.");
        }
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
