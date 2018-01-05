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

package com.dryadandnaiad.sethlans.services.blender;

import com.dryadandnaiad.sethlans.enums.ComputeType;

import java.util.List;

/**
 * Created Mario Estrella on 3/24/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public interface BlenderPythonScriptService {

    String writeBenchmarkPythonScript(ComputeType computeType, String renderLocation,
                                      String deviceId, String tileSize, int resolution_x,
                                      int resolution_y, int res_percentage);

    String writeRenderPythonScript(ComputeType computeType, String renderLocation, List<String> selectedDeviceIds, List<String> unselectedIds,
                                   String tileSize, int resolutionX, int resolutionY, int resPercentage, int samples, double partMaxY, double partMinY);
}
