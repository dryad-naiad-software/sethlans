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

import com.sun.jna.Library;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;

/**
 * File created by Mario Estrella on 4/22/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public interface CUDA extends Library {
    /**
     * @param flags
     * @return CUDA_SUCCESS, CUDA_ERROR_INVALID_VALUE,
     * CUDA_ERROR_INVALID_DEVICE, CUDA_ERROR_SYSTEM_DRIVER_MISMATCH,
     * CUDA_ERROR_COMPAT_NOT_SUPPORTED_ON_DEVICE
     */
    int cuInit(int flags);

    int cuDeviceGetCount(IntByReference count);

    int cuDeviceGetName(byte[] name, int len, int dev);

    int cuDeviceTotalMem_v2(LongByReference bytes, int dev);

    int cuDeviceTotalMem(LongByReference bytes, int dev);

    int cuDeviceGetAttribute(int[] array, int attribute, int dev);
}
