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
    public String writePythonScript(ComputeType computeType, String renderLocation, int deviceId, int tileSize) {
        try {
            File script = new File(renderLocation + File.separator + "script-" + deviceId + ".py");
            FileWriter scriptWriter = new FileWriter(script);

            // Write Imports
            scriptWriter.write(PythonImports.BPY.toString() + "\n\n");

            //Temp Directory
            scriptWriter.write("bpy.context.user_preferences.filepaths.temporary_directory = " + "\"" + renderLocation + "\"" + "\n");

            // Set Device
            scriptWriter.write("bpy.context.scene.cycles.device = " + "\"" + computeType + "\"" + "\n");

            if (computeType.equals(ComputeType.GPU)) {
                // CUDA Setting
                scriptWriter.write("\n");
                scriptWriter.write("bpy.context.user_preferences.addons['cycles'].preferences.compute_device_type = \"CUDA\"" + "\n");
                scriptWriter.write("devices = bpy.context.user_preferences.addons['cycles'].preferences.get_devices()" + "\n");
                //CUDA = 0, OpenCL = 1
                scriptWriter.write("cuda_devices = devices[0]" + "\n");
                scriptWriter.write("cuda_id = " + deviceId + "\n");
                scriptWriter.write("\n");
                scriptWriter.write("for i in range(len(cuda_devices)):" + "\n");
                scriptWriter.write("\tcuda_devices[i].use = (i == cuda_id)" + "\n");

                // Disable all OpenCL
                scriptWriter.write("\n");
                scriptWriter.write("for dev in devices[1]:" + "\n");
                scriptWriter.write("\tdev.use = False" + "\n");
            }

            if (computeType.equals(ComputeType.CPU)) {
                scriptWriter.write("\n");
                scriptWriter.write("bpy.context.user_preferences.system.compute_device_type = CPU" + "\n");
                scriptWriter.write("bpy.context.user_preferences.system.compute_device = CPU" + "\n");
            }

//            // Disable GPU Devices
//            scriptWriter.write("\n");
//            scriptWriter.write("for dev in devices[0]:" + "\n");
//            scriptWriter.write("\tdev.use = False" + "\n");
//            scriptWriter.write("\n");
//            scriptWriter.write("for dev in devices[1]:" + "\n");
//            scriptWriter.write("\tdev.use = False" + "\n");


            // Tile Sizes
            scriptWriter.write("\n");
            scriptWriter.write("bpy.context.scene.render.tile_x = " + tileSize + "\n");
            scriptWriter.write("bpy.context.scene.render.tile_y = " + tileSize + "\n");
            scriptWriter.flush();
            scriptWriter.close();
            return script.toString();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
