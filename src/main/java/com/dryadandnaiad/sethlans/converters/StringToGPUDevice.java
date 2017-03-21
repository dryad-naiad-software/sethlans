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

package com.dryadandnaiad.sethlans.converters;

import com.dryadandnaiad.sethlans.client.hardware.gpu.GPUDevice;
import org.springframework.core.convert.converter.Converter;

/**
 * Created Mario Estrella on 3/21/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class StringToGPUDevice implements Converter<String, GPUDevice> {
    @Override
    public GPUDevice convert(String source) {
        String[] parts = source.split(",");
        String model = parts[0];
        long ram = Long.parseLong(parts[1]);
        String cuda = parts[2];
        return new GPUDevice(model, ram, cuda);
    }
}
