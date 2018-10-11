/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC
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

import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import jcuda.driver.CUresult;
import org.apache.commons.lang3.SystemUtils;
import org.jocl.cl_device_id;
import org.jocl.cl_platform_id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.jocl.CL.*;

/**
 * Created Mario Estrella on 3/19/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class GPU {
    private static final Logger LOG = LoggerFactory.getLogger(GPU.class);
    public static List<GPUDevice> devices = null;
    public static final int CL_DEVICE_BOARD_NAME_AMD = 0x4038;

    private static void generateCUDA() {
        LOG.info("Looking for Compatible CUDA Devices");
        if (devices == null) {
            devices = new LinkedList<>();
        }

        String path = getCUDALib();
        if (path == null) {
            LOG.info("no CUDA lib path found");

        }
        CUDA cudalib;
        int result;
        try {
            cudalib = Native.loadLibrary(path, CUDA.class);
            result = cudalib.cuInit(0);
            if (result != CUresult.CUDA_SUCCESS) {
                LOG.error("cuInit failed (ret: " + result + ")");
                if (result == CUresult.CUDA_ERROR_UNKNOWN) {
                    LOG.error("If you are running Linux, this error is usually due to nvidia kernel module 'nvidia_uvm' not loaded. " +
                            "Relaunch the application as root or load the module. " +
                            "Most of time it does fix the issue.");
                    return;
                }
            }

            if (result == CUresult.CUDA_ERROR_NO_DEVICE) {
                LOG.info("No Device Found");
                return;
            }

            IntByReference count = new IntByReference();
            result = cudalib.cuDeviceGetCount(count);

            if (result != CUresult.CUDA_SUCCESS) {
                LOG.error("cuDeviceGetCount failed (ret: " + CUresult.stringFor(result) + ")");
                return;
            }

            for (int num = 0; num < count.getValue(); num++) {
                byte name[] = new byte[256];

                result = cudalib.cuDeviceGetName(name, 256, num);
                if (result != CUresult.CUDA_SUCCESS) {
                    LOG.error("cuDeviceGetName failed (ret: " + CUresult.stringFor(result) + ")");
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
                    LOG.error("cuDeviceTotalMem failed (ret: " + CUresult.stringFor(result) + ")");
                    return;
                }

                LOG.info("One CUDA Device found, adding to list.");
                devices.add(new GPUDevice(new String(name).trim(), ram.getValue(), "CUDA_" + Integer.toString(num), false, true));
            }

        } catch (java.lang.UnsatisfiedLinkError e) {
            LOG.error("Failed to load CUDA lib (path: " + path + "). CUDA is probably not installed.");
        } catch (java.lang.ExceptionInInitializerError e) {
            LOG.error("ExceptionInInitializerError " + e.getMessage());

        } catch (Exception e) {
            LOG.error("Generic exception" + e.getMessage());

        }
    }

    private static void generateOpenCL() {
        try {
            LOG.info("Looking for Compatible OpenCL Devices");
            if (devices == null) {
                devices = new LinkedList<>();
            }
            int numPlatforms[] = new int[1];
            clGetPlatformIDs(0, null, numPlatforms);
            String model;
            long memory;
            String deviceID;
            // Obtain the platform IDs
            cl_platform_id platforms[] = new cl_platform_id[numPlatforms[0]];
            clGetPlatformIDs(platforms.length, platforms, null);

            // Collect all devices of all platforms
            List<cl_device_id> openCLdevices = new ArrayList<>();
            for (cl_platform_id platform : platforms) {

                // Obtain the number of devices for the current platform
                int numDevices[] = new int[1];
                clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, 0, null, numDevices);
                cl_device_id devicesArray[] = new cl_device_id[numDevices[0]];
                clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, numDevices[0], devicesArray, null);

                openCLdevices.addAll(Arrays.asList(devicesArray));
            }

            for (int i = 0; i < openCLdevices.size(); i++) {
                cl_device_id device = openCLdevices.get(i);

                // CL_DEVICE_VENDOR
                String deviceVendor = JOCLSupport.getString(device, CL_DEVICE_VENDOR);

                // CL_DEVICE_NAME
                String openCLDeviceId = JOCLSupport.getString(device, CL_DEVICE_BOARD_NAME_AMD);

                // CL_DEVICE_GLOBAL_MEM_SIZE
                memory = JOCLSupport.getLong(device, CL_DEVICE_GLOBAL_MEM_SIZE);
                String openCLVersionString = JOCLSupport.getString(device, CL_DEVICE_OPENCL_C_VERSION);
                float openCLVersion = Float.parseFloat(openCLVersionString.substring(openCLVersionString.toLowerCase().lastIndexOf("c") + 1));
                deviceID = "OPENCL_" + i;
                model = openCLDeviceId;
                boolean invalidModel = false;
                if (deviceVendor.toLowerCase().contains("nvidia") || deviceVendor.toLowerCase().contains("intel")) {
                    invalidModel = true;
                }

                if (!invalidModel && openCLVersion > 1.2) {
                    LOG.info("One OpenCL device found, adding to list");
                    LOG.debug("Open CL version " + openCLVersion);
                    devices.add(new GPUDevice(model, memory, deviceID, true, false));
                }


            }
        } catch (UnsatisfiedLinkError e) {
            LOG.error(e.getMessage() + " Most likely, OpenCL not present on system.");
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

    public static List<String> listModels() {
        if (devices == null) {
            generateCUDA();
            generateOpenCL();
        }

        List<String> devs = new LinkedList<>();
        for (GPUDevice dev : devices) {
            devs.add(dev.getModel());
        }
        return devs;
    }

    public static List<GPUDevice> listDevices() {
        if (devices == null) {
            generateCUDA();
            generateOpenCL();
        }

        return devices;
    }

}