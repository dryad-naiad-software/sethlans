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

    private static void generateCUDA() {
        if (devices == null) {
            devices = new LinkedList<>();
        }

        String path = getCUDALib();
        if (path == null) {
            LOG.debug("GPU::generateCUDA no CUDA lib path found");

        }
        CUDA cudalib;
        int result;
        try {
            cudalib = Native.loadLibrary(path, CUDA.class);
            result = cudalib.cuInit(0);
            if (result != CUresult.CUDA_SUCCESS) {
                LOG.info("GPU::generateCUDA cuInit failed (ret: " + result + ")");
                if (result == CUresult.CUDA_ERROR_UNKNOWN) {
                    LOG.info("If you are running Linux, this error is usually due to nvidia kernel module 'nvidia_uvm' not loaded. " +
                            "\nRelaunch the application as root or load the module. " +
                            "\nMost of time it does fix the issue.");
                }
            }

            if (result == CUresult.CUDA_ERROR_NO_DEVICE) {
                LOG.debug("No Device Found");
            }

            IntByReference count = new IntByReference();
            result = cudalib.cuDeviceGetCount(count);

            if (result != CUresult.CUDA_SUCCESS) {
                LOG.debug("GPU::generateCUDA cuDeviceGetCount failed (ret: " + CUresult.stringFor(result) + ")");
            }

            for (int num = 0; num < count.getValue(); num++) {
                byte name[] = new byte[256];

                result = cudalib.cuDeviceGetName(name, 256, num);
                if (result != CUresult.CUDA_SUCCESS) {
                    LOG.debug("GPU::generateCUDA cuDeviceGetName failed (ret: " + CUresult.stringFor(result) + ")");
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
                    LOG.debug("GPU::generateCUDA cuDeviceTotalMem failed (ret: " + CUresult.stringFor(result) + ")");
                }

                devices.add(new GPUDevice(new String(name).trim(), ram.getValue(), "CUDA_" + Integer.toString(num), false, true));
            }

        } catch (java.lang.UnsatisfiedLinkError e) {
            LOG.debug("GPU::generateCUDA failed to load CUDA lib (path: " + path + ")");
        } catch (java.lang.ExceptionInInitializerError e) {
            LOG.error("GPU::generateCUDA ExceptionInInitializerError " + e.getMessage());

        } catch (Exception e) {
            LOG.error("GPU::generateCUDA generic exception" + e.getMessage());

        }
    }

    private static void generateOpenCL() {
        LOG.debug("Generate OpenCL Called");
        int numPlatforms[] = new int[1];
        clGetPlatformIDs(0, null, numPlatforms);
        LOG.debug("Number of platforms: " + numPlatforms[0]);
        String model;
        long memory;
        int rating;
        String cudaName = "";
        boolean openCL = true;
        boolean cuda = false;

        // Obtain the platform IDs
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms[0]];
        clGetPlatformIDs(platforms.length, platforms, null);

        // Collect all devices of all platforms
        List<cl_device_id> openCLdevices = new ArrayList<cl_device_id>();
        for (cl_platform_id platform : platforms) {

            String platformName = JOCLSupport.getString(platform, CL_PLATFORM_NAME);
            // Obtain the number of devices for the current platform
            int numDevices[] = new int[1];
            clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, 0, null, numDevices);

            System.out.println("Number of devices in platform " + platformName + ": " + numDevices[0]);

            cl_device_id devicesArray[] = new cl_device_id[numDevices[0]];
            clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, numDevices[0], devicesArray, null);

            openCLdevices.addAll(Arrays.asList(devicesArray));
        }

        for (cl_device_id device : openCLdevices) {
            // CL_DEVICE_NAME
            String deviceName = JOCLSupport.getString(device, CL_DEVICE_NAME);
            // CL_DEVICE_VENDOR
            String deviceVendor = JOCLSupport.getString(device, CL_DEVICE_VENDOR);
            model = deviceVendor + " " + deviceName;
            // CL_DEVICE_GLOBAL_MEM_SIZE
            memory = JOCLSupport.getLong(device, CL_DEVICE_GLOBAL_MEM_SIZE);
            devices.add(new GPUDevice(model, memory, cudaName, true, false));
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