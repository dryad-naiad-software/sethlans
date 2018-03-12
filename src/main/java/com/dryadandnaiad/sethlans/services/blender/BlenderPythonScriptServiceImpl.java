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
import com.dryadandnaiad.sethlans.enums.PythonImports;
import com.dryadandnaiad.sethlans.enums.RenderOutputFormat;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * Created Mario Estrella on 12/17/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class BlenderPythonScriptServiceImpl implements BlenderPythonScriptService {
    private static final Logger LOG = LoggerFactory.getLogger(BlenderPythonScriptServiceImpl.class);


    @Override
    public String writeBenchmarkPythonScript(ComputeType computeType, String renderLocation, String deviceId, boolean cuda,
                                             String tileSize, int resolutionX, int resolutionY, int resPercentage) {
        try {
            File script = new File(renderLocation + File.separator + "script-" + deviceId + ".py");
            FileWriter scriptWriter = new FileWriter(script);

            // Write Imports
            scriptWriter.write(PythonImports.BPY.toString() + "\n\n");

            //noinspection ConstantConditions
            if (SethlansUtils.getOS().contains("Windows")) {
                scriptWriter.write("bpy.context.user_preferences.filepaths.temporary_directory = " + "r\"" + renderLocation + "\"" + "\n");
            } else {
                scriptWriter.write("bpy.context.user_preferences.filepaths.temporary_directory = " + "\"" + renderLocation + "\"" + "\n");
            }
            //Temp Directory


            // Set Device
            scriptWriter.write("bpy.context.scene.cycles.device = " + "\"" + computeType + "\"" + "\n");

            if (computeType.equals(ComputeType.GPU)) {
                if (cuda) {
                    // CUDA Setting
                    scriptWriter.write("\n");
                    scriptWriter.write("bpy.context.user_preferences.addons['cycles'].preferences.compute_device_type = \"CUDA\"" + "\n");
                    scriptWriter.write("devices = bpy.context.user_preferences.addons['cycles'].preferences.get_devices()" + "\n");
                    //CUDA = 0
                    scriptWriter.write("render_devices = devices[0]" + "\n");
                    scriptWriter.write("device_id = " + deviceId + "\n");
                    scriptWriter.write("\n");
                    // Sets the CUDA device defined in device_id to true.
                    scriptWriter.write("for i in range(len(render_devices)):" + "\n");
                    scriptWriter.write("\trender_devices[i].use = (i == device_id)" + "\n");

                    // Disable all OpenCL
                    scriptWriter.write("\n");
                    scriptWriter.write("for dev in devices[1]:" + "\n");
                    scriptWriter.write("\tdev.use = False" + "\n");
                } else {
                    // OpenCL
                    scriptWriter.write("\n");
                    scriptWriter.write("bpy.context.user_preferences.addons['cycles'].preferences.compute_device_type = \"OPENCL\"" + "\n");
                    scriptWriter.write("devices = bpy.context.user_preferences.addons['cycles'].preferences.get_devices()" + "\n");

                    //OpenCL = 1
                    scriptWriter.write("render_devices = devices[1]" + "\n");
                    scriptWriter.write("device_id = " + deviceId + "\n");
                    scriptWriter.write("\n");

                    // Sets the OpenCL device defined in device_id to true.
                    scriptWriter.write("for i in range(len(render_devices)):" + "\n");
                    scriptWriter.write("\trender_devices[i].use = (i == device_id)" + "\n");
                }

            }


            // Set Resolution
            scriptWriter.write("\n");
            scriptWriter.write("for scene in bpy.data.scenes:" + "\n");
            scriptWriter.write("\tscene.render.resolution_x = " + resolutionX + "\n");
            scriptWriter.write("\tscene.render.resolution_y = " + resolutionY + "\n");
            scriptWriter.write("\tscene.render.resolution_percentage = " + resPercentage + "\n");
            //scriptWriter.write("\tscene.render.use_border = False");

            // Tile Sizes
            scriptWriter.write("\n");
            scriptWriter.write("bpy.context.scene.render.tile_x = " + tileSize + "\n");
            scriptWriter.write("bpy.context.scene.render.tile_y = " + tileSize + "\n");
            scriptWriter.flush();
            scriptWriter.close();

            return script.toString();
        } catch (java.io.IOException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return null;
    }

    @Override
    public String writeRenderPythonScript(ComputeType computeType, String renderLocation, List<String> selectedDeviceIds, List<String> unselectedIds,
                                          boolean cuda,
                                          RenderOutputFormat renderOutputFormat,
                                          String tileSize, int resolutionX, int resolutionY, int resPercentage, int samples,
                                          double partMaxY, double partMinY) {
        try {
            File script;
            if (selectedDeviceIds.size() == 1) {
                script = new File(renderLocation + File.separator + "script-" + selectedDeviceIds.get(0) + ".py");
            } else if (selectedDeviceIds.size() > 1) {
                script = new File(renderLocation + File.separator + "script-MULTI_DEV.py");
            } else {
                script = new File(renderLocation + File.separator + "script-CPU.py");
            }

            FileWriter scriptWriter = new FileWriter(script);

            // Write Imports
            scriptWriter.write(PythonImports.BPY.toString() + "\n\n");

            //Temp Directory
            //noinspection ConstantConditions
            if (SethlansUtils.getOS().contains("Windows")) {
                scriptWriter.write("bpy.context.user_preferences.filepaths.temporary_directory = " + "r\"" + renderLocation + "\"" + "\n");
            } else {
                scriptWriter.write("bpy.context.user_preferences.filepaths.temporary_directory = " + "\"" + renderLocation + "\"" + "\n");
            }

            // Set Device
            scriptWriter.write("\n");
            scriptWriter.write("bpy.context.scene.cycles.device = " + "\"" + computeType + "\"" + "\n");

            if (computeType.equals(ComputeType.GPU)) {
                if (cuda) {
                    // CUDA Setting
                    scriptWriter.write("\n");
                    scriptWriter.write("bpy.context.user_preferences.addons['cycles'].preferences.compute_device_type = \"CUDA\"" + "\n");
                    scriptWriter.write("devices = bpy.context.user_preferences.addons['cycles'].preferences.get_devices()" + "\n");
                    if (selectedDeviceIds.size() == 1) {
                        scriptWriter.write("cuda_devices = devices[0]" + "\n");
                        scriptWriter.write("cuda_id = " + selectedDeviceIds.get(0) + "\n");
                        scriptWriter.write("\n");

                        // Sets the CUDA device defined in cuda_id to true.
                        scriptWriter.write("for i in range(len(cuda_devices)):" + "\n");
                        scriptWriter.write("\tcuda_devices[i].use = (i == cuda_id)" + "\n");
                    } else {
                        scriptWriter.write("cuda_devices = devices[0]" + "\n");
                        scriptWriter.write("\n");

                        // Sets the CUDA device defined in cuda_id to true.
                        for (String deviceId : selectedDeviceIds) {
                            scriptWriter.write("cuda_devices[" + deviceId + "].use = True" + "\n");
                        }
                        if (unselectedIds.size() > 0) {
                            for (String unselectedId : unselectedIds) {
                                scriptWriter.write("cuda_devices[" + unselectedId + "].use = False" + "\n");
                            }
                        }

                    }

                    // Disable all OpenCL for now - OpenCL will be implemented in March.
                    scriptWriter.write("\n");
                    scriptWriter.write("for dev in devices[1]:" + "\n");
                    scriptWriter.write("\tdev.use = False" + "\n");
                } else {
                    // OpenCL
                    scriptWriter.write("\n");
                    scriptWriter.write("bpy.context.user_preferences.addons['cycles'].preferences.compute_device_type = \"OPENCL\"" + "\n");
                    scriptWriter.write("devices = bpy.context.user_preferences.addons['cycles'].preferences.get_devices()" + "\n");

                    if (selectedDeviceIds.size() == 1) {
                        //OpenCL = 1
                        scriptWriter.write("render_devices = devices[1]" + "\n");
                        scriptWriter.write("device_id = " + selectedDeviceIds.get(0) + "\n");
                        scriptWriter.write("\n");

                        // Sets the OpenCL device defined in device_id to true.
                        scriptWriter.write("for i in range(len(render_devices)):" + "\n");
                        scriptWriter.write("\trender_devices[i].use = (i == device_id)" + "\n");
                    } else {
                        scriptWriter.write("render_devices = devices[1]" + "\n");
                        scriptWriter.write("\n");

                        // Sets the OpenCL device defined in device_id to true.
                        for (String deviceId : selectedDeviceIds) {
                            scriptWriter.write("render_devices[" + deviceId + "].use = True" + "\n");
                        }
                        if (unselectedIds.size() > 0) {
                            for (String unselectedId : unselectedIds) {
                                scriptWriter.write("render_devices[" + unselectedId + "].use = False" + "\n");
                            }
                        }
                    }

                }

            }


            // Set Resolution and Samples
            scriptWriter.write("\n");
            scriptWriter.write("for scene in bpy.data.scenes:" + "\n");
            scriptWriter.write("\tscene.render.resolution_x = " + resolutionX + "\n");
            scriptWriter.write("\tscene.render.resolution_y = " + resolutionY + "\n");
            scriptWriter.write("\tscene.render.resolution_percentage = " + resPercentage + "\n");
            scriptWriter.write("\tscene.cycles.samples = " + samples);

            // Set Part
            // X is the width of the image, parts are then slices from top to bottom along Y axis.
            scriptWriter.write("\n");
            scriptWriter.write("\tscene.render.use_border = True" + "\n");
            scriptWriter.write("\tscene.render.use_crop_to_border = True" + "\n");
            scriptWriter.write("\tscene.render.border_min_x = 0" + "\n");
            scriptWriter.write("\tscene.render.border_max_x = 1.0" + "\n");
            scriptWriter.write("\tscene.render.border_max_y = " + partMaxY + "\n");
            scriptWriter.write("\tscene.render.border_min_y = " + partMinY + "\n");

            // Tile Sizes
            scriptWriter.write("\n");
            scriptWriter.write("bpy.context.scene.render.tile_x = " + tileSize + "\n");
            scriptWriter.write("bpy.context.scene.render.tile_y = " + tileSize + "\n");
            if (renderOutputFormat.equals(RenderOutputFormat.PNG) || renderOutputFormat.equals(RenderOutputFormat.AVI)) {
                scriptWriter.write("bpy.context.scene.render.image_settings.file_format = 'PNG'" + "\n");
            } else if (renderOutputFormat.equals(RenderOutputFormat.EXR)) {
                scriptWriter.write("bpy.context.scene.render.image_settings.file_format = 'OPEN_EXR'" + "\n");
            }

            scriptWriter.flush();
            scriptWriter.close();

            return script.toString();
        } catch (java.io.IOException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }
        return null;
    }

}
