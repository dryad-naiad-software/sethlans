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
import org.jocl.cl_device_id;
import org.jocl.cl_platform_id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.jocl.CL.*;

public class GPU {
    private static final Logger LOG = LoggerFactory.getLogger(GPU.class);
    public static List<GPUDevice> devices = null;

    public static boolean generateCUDA() {
        if (devices == null) {
            devices = new LinkedList<>();
        }

        String path = getCUDALib();
        if (path == null) {
            LOG.debug("GPU::generateCUDA no CUDA lib path found");
            return false;
        }
        CUDA cudalib;
        try {
            cudalib = Native.loadLibrary(path, CUDA.class);
        } catch (java.lang.UnsatisfiedLinkError e) {
            LOG.debug("GPU::generateCUDA failed to load CUDA lib (path: " + path + ")");
            return false;
        } catch (java.lang.ExceptionInInitializerError e) {
            LOG.error("GPU::generateCUDA ExceptionInInitializerError " + e.getMessage());
            return false;
        } catch (Exception e) {
            LOG.error("GPU::generateCUDA generic exception" + e.getMessage());
            return false;
        }

        int result;

        result = cudalib.cuInit(0);
        if (result != CUresult.CUDA_SUCCESS) {
            LOG.info("GPU::generateCUDA cuInit failed (ret: " + result + ")");
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
            LOG.debug("GPU::generateCUDA cuDeviceGetCount failed (ret: " + CUresult.stringFor(result) + ")");
            return false;
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
                return false;
            }

            devices.add(new GPUDevice(new String(name).trim(), ram.getValue(), "CUDA_" + Integer.toString(num), false, true));
        }
        return true;
    }

    public static boolean generateOpenCL() {
        LOG.debug("Generate OPENCL Called");
        int numPlatforms[] = new int[1];
        clGetPlatformIDs(0, null, numPlatforms);
        System.out.println("Number of platforms: " + numPlatforms[0]);

        // Obtain the platform IDs
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms[0]];
        clGetPlatformIDs(platforms.length, platforms, null);

        // Collect all devices of all platforms
        List<cl_device_id> devices = new ArrayList<cl_device_id>();
        for (int i = 0; i < platforms.length; i++) {
            String platformName = JOCLSupport.getString(platforms[i], CL_PLATFORM_NAME);

            // Obtain the number of devices for the current platform
            int numDevices[] = new int[1];
            clGetDeviceIDs(platforms[i], CL_DEVICE_TYPE_GPU, 0, null, numDevices);

            System.out.println("Number of devices in platform " + platformName + ": " + numDevices[0]);

            cl_device_id devicesArray[] = new cl_device_id[numDevices[0]];
            clGetDeviceIDs(platforms[i], CL_DEVICE_TYPE_GPU, numDevices[0], devicesArray, null);

            devices.addAll(Arrays.asList(devicesArray));
        }

        for (cl_device_id device : devices) {
            // CL_DEVICE_NAME
            String deviceName = JOCLSupport.getString(device, CL_DEVICE_NAME);
            System.out.println("--- Info for device " + deviceName + ": ---");
            System.out.printf("CL_DEVICE_NAME: \t\t\t%s\n", deviceName);

            // CL_DEVICE_VENDOR
            String deviceVendor = JOCLSupport.getString(device, CL_DEVICE_VENDOR);
            System.out.printf("CL_DEVICE_VENDOR: \t\t\t%s\n", deviceVendor);

            // CL_DRIVER_VERSION
            String driverVersion = JOCLSupport.getString(device, CL_DRIVER_VERSION);
            System.out.printf("CL_DRIVER_VERSION: \t\t\t%s\n", driverVersion);


            // CL_DEVICE_TYPE
            long deviceType = JOCLSupport.getLong(device, CL_DEVICE_TYPE);
            if ((deviceType & CL_DEVICE_TYPE_CPU) != 0)
                System.out.printf("CL_DEVICE_TYPE:\t\t\t\t%s\n", "CL_DEVICE_TYPE_CPU");
            if ((deviceType & CL_DEVICE_TYPE_GPU) != 0)
                System.out.printf("CL_DEVICE_TYPE:\t\t\t\t%s\n", "CL_DEVICE_TYPE_GPU");
            if ((deviceType & CL_DEVICE_TYPE_ACCELERATOR) != 0)
                System.out.printf("CL_DEVICE_TYPE:\t\t\t\t%s\n", "CL_DEVICE_TYPE_ACCELERATOR");
            if ((deviceType & CL_DEVICE_TYPE_DEFAULT) != 0)
                System.out.printf("CL_DEVICE_TYPE:\t\t\t\t%s\n", "CL_DEVICE_TYPE_DEFAULT");

            // CL_DEVICE_MAX_COMPUTE_UNITS
            int maxComputeUnits = JOCLSupport.getInt(device, CL_DEVICE_MAX_COMPUTE_UNITS);
            System.out.printf("CL_DEVICE_MAX_COMPUTE_UNITS:\t\t%d\n", maxComputeUnits);

            // CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS
            long maxWorkItemDimensions = JOCLSupport.getLong(device, CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS);
            System.out.printf("CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS:\t%d\n", maxWorkItemDimensions);

            // CL_DEVICE_MAX_WORK_ITEM_SIZES
            long maxWorkItemSizes[] = JOCLSupport.getSizes(device, CL_DEVICE_MAX_WORK_ITEM_SIZES, 3);
            System.out.printf("CL_DEVICE_MAX_WORK_ITEM_SIZES:\t\t%d / %d / %d \n",
                    maxWorkItemSizes[0], maxWorkItemSizes[1], maxWorkItemSizes[2]);

            // CL_DEVICE_MAX_WORK_GROUP_SIZE
            long maxWorkGroupSize = JOCLSupport.getSize(device, CL_DEVICE_MAX_WORK_GROUP_SIZE);
            System.out.printf("CL_DEVICE_MAX_WORK_GROUP_SIZE:\t\t%d\n", maxWorkGroupSize);

            // CL_DEVICE_MAX_CLOCK_FREQUENCY
            long maxClockFrequency = JOCLSupport.getLong(device, CL_DEVICE_MAX_CLOCK_FREQUENCY);
            System.out.printf("CL_DEVICE_MAX_CLOCK_FREQUENCY:\t\t%d MHz\n", maxClockFrequency);

            // CL_DEVICE_ADDRESS_BITS
            int addressBits = JOCLSupport.getInt(device, CL_DEVICE_ADDRESS_BITS);
            System.out.printf("CL_DEVICE_ADDRESS_BITS:\t\t\t%d\n", addressBits);

            // CL_DEVICE_MAX_MEM_ALLOC_SIZE
            long maxMemAllocSize = JOCLSupport.getLong(device, CL_DEVICE_MAX_MEM_ALLOC_SIZE);
            System.out.printf("CL_DEVICE_MAX_MEM_ALLOC_SIZE:\t\t%d MByte\n", (int) (maxMemAllocSize / (1024 * 1024)));

            // CL_DEVICE_GLOBAL_MEM_SIZE
            long globalMemSize = JOCLSupport.getLong(device, CL_DEVICE_GLOBAL_MEM_SIZE);
            System.out.printf("CL_DEVICE_GLOBAL_MEM_SIZE:\t\t%d MByte\n", (int) (globalMemSize / (1024 * 1024)));

            // CL_DEVICE_ERROR_CORRECTION_SUPPORT
            int errorCorrectionSupport = JOCLSupport.getInt(device, CL_DEVICE_ERROR_CORRECTION_SUPPORT);
            System.out.printf("CL_DEVICE_ERROR_CORRECTION_SUPPORT:\t%s\n", errorCorrectionSupport != 0 ? "yes" : "no");

            // CL_DEVICE_LOCAL_MEM_TYPE
            int localMemType = JOCLSupport.getInt(device, CL_DEVICE_LOCAL_MEM_TYPE);
            System.out.printf("CL_DEVICE_LOCAL_MEM_TYPE:\t\t%s\n", localMemType == 1 ? "local" : "global");

            // CL_DEVICE_LOCAL_MEM_SIZE
            long localMemSize = JOCLSupport.getLong(device, CL_DEVICE_LOCAL_MEM_SIZE);
            System.out.printf("CL_DEVICE_LOCAL_MEM_SIZE:\t\t%d KByte\n", (int) (localMemSize / 1024));

            // CL_DEVICE_MAX_CONSTANT_BUFFER_SIZE
            long maxConstantBufferSize = JOCLSupport.getLong(device, CL_DEVICE_MAX_CONSTANT_BUFFER_SIZE);
            System.out.printf("CL_DEVICE_MAX_CONSTANT_BUFFER_SIZE:\t%d KByte\n", (int) (maxConstantBufferSize / 1024));

            // CL_DEVICE_QUEUE_PROPERTIES
            long queueProperties = JOCLSupport.getLong(device, CL_DEVICE_QUEUE_PROPERTIES);
            if ((queueProperties & CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE) != 0)
                System.out.printf("CL_DEVICE_QUEUE_PROPERTIES:\t\t%s\n", "CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE");
            if ((queueProperties & CL_QUEUE_PROFILING_ENABLE) != 0)
                System.out.printf("CL_DEVICE_QUEUE_PROPERTIES:\t\t%s\n", "CL_QUEUE_PROFILING_ENABLE");

            // CL_DEVICE_IMAGE_SUPPORT
            int imageSupport = JOCLSupport.getInt(device, CL_DEVICE_IMAGE_SUPPORT);
            System.out.printf("CL_DEVICE_IMAGE_SUPPORT:\t\t%d\n", imageSupport);

            // CL_DEVICE_MAX_READ_IMAGE_ARGS
            int maxReadImageArgs = JOCLSupport.getInt(device, CL_DEVICE_MAX_READ_IMAGE_ARGS);
            System.out.printf("CL_DEVICE_MAX_READ_IMAGE_ARGS:\t\t%d\n", maxReadImageArgs);

            // CL_DEVICE_MAX_WRITE_IMAGE_ARGS
            int maxWriteImageArgs = JOCLSupport.getInt(device, CL_DEVICE_MAX_WRITE_IMAGE_ARGS);
            System.out.printf("CL_DEVICE_MAX_WRITE_IMAGE_ARGS:\t\t%d\n", maxWriteImageArgs);

            // CL_DEVICE_SINGLE_FP_CONFIG
            long singleFpConfig = JOCLSupport.getLong(device, CL_DEVICE_SINGLE_FP_CONFIG);
            System.out.printf("CL_DEVICE_SINGLE_FP_CONFIG:\t\t%s\n",
                    stringFor_cl_device_fp_config(singleFpConfig));

            // CL_DEVICE_IMAGE2D_MAX_WIDTH
            long image2dMaxWidth = JOCLSupport.getSize(device, CL_DEVICE_IMAGE2D_MAX_WIDTH);
            System.out.printf("CL_DEVICE_2D_MAX_WIDTH\t\t\t%d\n", image2dMaxWidth);

            // CL_DEVICE_IMAGE2D_MAX_HEIGHT
            long image2dMaxHeight = JOCLSupport.getSize(device, CL_DEVICE_IMAGE2D_MAX_HEIGHT);
            System.out.printf("CL_DEVICE_2D_MAX_HEIGHT\t\t\t%d\n", image2dMaxHeight);

            // CL_DEVICE_IMAGE3D_MAX_WIDTH
            long image3dMaxWidth = JOCLSupport.getSize(device, CL_DEVICE_IMAGE3D_MAX_WIDTH);
            System.out.printf("CL_DEVICE_3D_MAX_WIDTH\t\t\t%d\n", image3dMaxWidth);

            // CL_DEVICE_IMAGE3D_MAX_HEIGHT
            long image3dMaxHeight = JOCLSupport.getSize(device, CL_DEVICE_IMAGE3D_MAX_HEIGHT);
            System.out.printf("CL_DEVICE_3D_MAX_HEIGHT\t\t\t%d\n", image3dMaxHeight);

            // CL_DEVICE_IMAGE3D_MAX_DEPTH
            long image3dMaxDepth = JOCLSupport.getSize(device, CL_DEVICE_IMAGE3D_MAX_DEPTH);
            System.out.printf("CL_DEVICE_3D_MAX_DEPTH\t\t\t%d\n", image3dMaxDepth);

            // CL_DEVICE_PREFERRED_VECTOR_WIDTH_<type>
            System.out.printf("CL_DEVICE_PREFERRED_VECTOR_WIDTH_<t>\t");
            int preferredVectorWidthChar = JOCLSupport.getInt(device, CL_DEVICE_PREFERRED_VECTOR_WIDTH_CHAR);
            int preferredVectorWidthShort = JOCLSupport.getInt(device, CL_DEVICE_PREFERRED_VECTOR_WIDTH_SHORT);
            int preferredVectorWidthInt = JOCLSupport.getInt(device, CL_DEVICE_PREFERRED_VECTOR_WIDTH_INT);
            int preferredVectorWidthLong = JOCLSupport.getInt(device, CL_DEVICE_PREFERRED_VECTOR_WIDTH_LONG);
            int preferredVectorWidthFloat = JOCLSupport.getInt(device, CL_DEVICE_PREFERRED_VECTOR_WIDTH_FLOAT);
            int preferredVectorWidthDouble = JOCLSupport.getInt(device, CL_DEVICE_PREFERRED_VECTOR_WIDTH_DOUBLE);
            System.out.printf("CHAR %d, SHORT %d, INT %d, LONG %d, FLOAT %d, DOUBLE %d\n\n\n",
                    preferredVectorWidthChar, preferredVectorWidthShort,
                    preferredVectorWidthInt, preferredVectorWidthLong,
                    preferredVectorWidthFloat, preferredVectorWidthDouble);
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