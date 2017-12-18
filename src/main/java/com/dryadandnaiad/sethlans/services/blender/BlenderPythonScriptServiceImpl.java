/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC.
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
import com.dryadandnaiad.sethlans.enums.PythonImports;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;

/**
 * Created Mario Estrella on 12/17/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class BlenderPythonScriptServiceImpl implements BlenderPythonScriptService {

    @Override
    public void writePythonScript(ComputeType computeType, String renderLocation, int deviceId, int tileSize) {
        try {
            File file = new File(renderLocation + File.separator + "script-" + deviceId + ".py");
            FileWriter fileWriter = new FileWriter(file);

            // Write Imports
            fileWriter.write(PythonImports.BPY.toString() + "\n\n");

            //Temp Directory
            fileWriter.write("bpy.context.user_preferences.filepaths.temporary_directory = " + "\"" + renderLocation + "\"" + "\n");

            // Set Device
            fileWriter.write("bpy.context.scene.cycles.device = " + computeType + "\n");

            if (computeType.equals(ComputeType.GPU)) {
                // CUDA Setting
                fileWriter.write("\n");
                fileWriter.write("bpy.context.user_preferences.addons['cycles'].preferences.compute_device_type = CUDA" + "\n");
                fileWriter.write("devices = bpy.context.user_preferences.addons['cycles'].preferences.get_devices()" + "\n");
                //CUDA = 0, OpenCL = 1
                fileWriter.write("cuda_devices = devices[0]" + "\n");
                fileWriter.write("cuda_id = " + deviceId + "\n");
                fileWriter.write("\n");
                fileWriter.write("for i in range(len(cuda_devices)):" + "\n");
                fileWriter.write("\tcuda_devices[i].use = (i == cuda_id)" + "\n");

                // Disable all OpenCL
                fileWriter.write("\n");
                fileWriter.write("for dev in devices[1]:" + "\n");
                fileWriter.write("\tdev.use = False" + "\n");
            }

            if (computeType.equals(ComputeType.CPU)) {
                fileWriter.write("\n");
                fileWriter.write("bpy.context.user_preferences.system.compute_device_type = CPU" + "\n");
                fileWriter.write("bpy.context.user_preferences.system.compute_device = CPU" + "\n");
            }

//            // Disable GPU Devices
//            fileWriter.write("\n");
//            fileWriter.write("for dev in devices[0]:" + "\n");
//            fileWriter.write("\tdev.use = False" + "\n");
//            fileWriter.write("\n");
//            fileWriter.write("for dev in devices[1]:" + "\n");
//            fileWriter.write("\tdev.use = False" + "\n");


            // Tile Sizes
            fileWriter.write("\n");
            fileWriter.write("bpy.context.scene.render.tile_x = " + tileSize + "\n");
            fileWriter.write("bpy.context.scene.render.tile_y = " + tileSize + "\n");
            fileWriter.flush();
            fileWriter.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }


}
