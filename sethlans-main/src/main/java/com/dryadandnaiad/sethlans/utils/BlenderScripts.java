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

package com.dryadandnaiad.sethlans.utils;

import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.dryadandnaiad.sethlans.enums.ComputeOn;
import com.dryadandnaiad.sethlans.enums.ImageOutputFormat;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * File created by Mario Estrella on 5/3/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class BlenderScripts {


    public static boolean writeRenderScript(RenderTask renderTask) {
        File script = new File(renderTask.getTaskDir() + File.separator + renderTask.getTaskID() + ".py");
        try {
            var location = renderTask.getTaskDir().replace("\\", "/");

            PrintStream scriptWriter = new PrintStream(script);

            // Write Imports
            scriptWriter.println("import bpy");
            scriptWriter.println();

            if (renderTask.getBlenderVersion() == null) {
                log.error("BlenderVersion cannot be null.");
                scriptWriter.flush();
                scriptWriter.close();
                script.delete();
                return false;
            }

            if (renderTask.getScriptInfo().getDeviceIDs() == null) {
                log.error("Device IDs cannot be null.");
                scriptWriter.flush();
                scriptWriter.close();
                script.delete();
                return false;
            }

            // Temp Directory
            if (renderTask.getBlenderVersion().contains("2.7")) {
                scriptWriter.println("prefs = bpy.context.user_preferences");
            } else {
                scriptWriter.println("prefs = bpy.context.preferences");
            }
            scriptWriter.println("prefs.filepaths.temporary_directory = "
                    + "\"" + location + "\"");

            scriptWriter.println();
            scriptWriter.println("scene = bpy.context.scene");

            if (renderTask.getScriptInfo().getBlenderEngine() == null) {
                log.error("BlenderEngine cannot be null.");
                scriptWriter.flush();
                scriptWriter.close();
                script.delete();
                return false;
            }

            // Set Device
            switch (renderTask.getScriptInfo().getBlenderEngine()) {
                case CYCLES:
                    scriptWriter.println("scene.render.engine = 'CYCLES'");
                    scriptWriter.println("scene.cycles.device = " + "'" + renderTask.getScriptInfo().getComputeOn() + "'");
                    scriptWriter.println();
                    scriptWriter.println("cycles_prefs = prefs.addons['cycles'].preferences");
                    if (renderTask.getScriptInfo().getComputeOn().equals(ComputeOn.GPU)) {
                        if (renderTask.getScriptInfo().getDeviceIDs().get(0).contains("CUDA")) {
                            cyclesCUDA(scriptWriter, renderTask);
                            break;
                        }
                        if (renderTask.getScriptInfo().getDeviceIDs().get(0).contains("OPENCL")) {
                            cyclesOPENCL(scriptWriter, renderTask);
                            break;
                        }
                        if (renderTask.getScriptInfo().getDeviceIDs().get(0).contains("OPTIX")) {
                            if (renderTask.getBlenderVersion().contains("2.7")) {
                                cyclesCUDA(scriptWriter, renderTask);
                            } else {
                                cyclesOPTIX(scriptWriter, renderTask);
                            }
                            break;
                        }

                    } else {
                        scriptWriter.println("cycles_prefs.compute_device_type = 'NONE'");
                    }
                    break;
                case BLENDER_EEVEE:
                    if (renderTask.getBlenderVersion().contains("2.7")) {
                        log.error("EEVEE is not supported on Blender versions less than 2.80");
                        scriptWriter.flush();
                        scriptWriter.close();
                        script.delete();
                        return false;
                    }
                    scriptWriter.println("scene.render.engine = 'BLENDER_EEVEE'");
                    break;
                case BLENDER_RENDER:
                    if (!renderTask.getBlenderVersion().contains("2.7")) {
                        log.error("Blender Render is not supported on Blender versions greater than 2.79b");
                        scriptWriter.flush();
                        scriptWriter.close();
                        script.delete();
                        return false;
                    }
                    scriptWriter.println("scene.render.engine = 'BLENDER_RENDER'");
                    break;
            }

            if (renderTask.getScriptInfo().getTaskResolutionX() == null ||
                    renderTask.getScriptInfo().getTaskResolutionY() == null ||
                    renderTask.getScriptInfo().getTaskResPercentage() == null) {
                log.error("TaskResolutionX, TaskResolutionY and TaskResPercentage cannot be null.");
                scriptWriter.flush();
                scriptWriter.close();
                script.delete();
                return false;
            }

            // Set Resolution and Samples
            scriptWriter.println();
            scriptWriter.println("for scene in bpy.data.scenes:");
            scriptWriter.println("\tscene.render.resolution_x = " + renderTask.getScriptInfo().getTaskResolutionX());
            scriptWriter.println("\tscene.render.resolution_y = " + renderTask.getScriptInfo().getTaskResolutionY());
            scriptWriter.println("\tscene.render.resolution_percentage = " + renderTask.getScriptInfo().getTaskResPercentage());
            if (renderTask.getScriptInfo().getBlenderEngine().equals(BlenderEngine.CYCLES)) {
                scriptWriter.println("\tscene.cycles.samples = " + renderTask.getScriptInfo().getSamples());
            }

            // Set Part
            if (!renderTask.isBenchmark() && renderTask.isUseParts()) {
                if (renderTask.getFrameInfo().getPartMaxX() == null |
                        renderTask.getFrameInfo().getPartMinX() == null ||
                        renderTask.getFrameInfo().getPartMaxY() == null || renderTask.getFrameInfo().getPartMinY() == null) {
                    log.error("PartMaxX, PartMinX, PartMaxY, PartMinY cannot be null when parts are being used.");
                    scriptWriter.flush();
                    scriptWriter.close();
                    script.delete();
                    return false;
                }
                scriptWriter.println("\tscene.render.use_border = True");
                scriptWriter.println("\tscene.render.use_crop_to_border = True");
                scriptWriter.println("\tscene.render.border_min_x = " + renderTask.getFrameInfo().getPartMinX());
                scriptWriter.println("\tscene.render.border_max_x = " + renderTask.getFrameInfo().getPartMaxX());
                scriptWriter.println("\tscene.render.border_min_y = " + renderTask.getFrameInfo().getPartMinY());
                scriptWriter.println("\tscene.render.border_max_y = " + renderTask.getFrameInfo().getPartMaxY());
            }

            //Set Cores (CPU rendering)
            if (renderTask.getScriptInfo().getComputeOn().equals(ComputeOn.CPU)) {
                if (renderTask.getScriptInfo().getCores() == null) {
                    log.error("Cores cannot be null when computing on CPU.");
                    scriptWriter.flush();
                    scriptWriter.close();
                    script.delete();
                    return false;
                }
                scriptWriter.println("\tscene.render.threads_mode = 'FIXED'");
                scriptWriter.println("\tscene.render.threads = " + renderTask.getScriptInfo().getCores());
            }


            if (renderTask.getScriptInfo().getTaskTileSize() == null) {
                log.error("TaskTileSize cannot be null.");
                scriptWriter.flush();
                scriptWriter.close();
                script.delete();
                return false;
            }
            // Tile Sizes
            scriptWriter.println();
            scriptWriter.println("scene.render.tile_x = " + renderTask.getScriptInfo().getTaskTileSize());
            scriptWriter.println("scene.render.tile_y = " + renderTask.getScriptInfo().getTaskTileSize());

            if (renderTask.getScriptInfo().getImageOutputFormat() == null) {
                log.error("ImageOutputFormat cannot be null.");
                scriptWriter.flush();
                scriptWriter.close();
                script.delete();
                return false;
            }
            // Final Settings
            scriptWriter.println();
            scriptWriter.println("scene.render.use_border = True");
            scriptWriter.println("scene.render.use_crop_to_border = True");
            if (renderTask.getScriptInfo().getImageOutputFormat().equals(ImageOutputFormat.PNG)) {
                scriptWriter.println("scene.render.image_settings.color_mode = 'RGBA'");
                scriptWriter.println("scene.render.image_settings.color_depth = '16'");
            }

            scriptWriter.flush();
            scriptWriter.close();

            return script.exists();

        } catch (IOException e) {
            log.error("Error creating render script!");
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
            return false;
        }
    }

    private static List<String> stripDeviceTypeFromID(List<String> deviceIDs) {
        List<String> strippedIDs = new ArrayList<>();
        for (String deviceID : deviceIDs) {
            strippedIDs.add(StringUtils.substringAfter(deviceID, "_"));
        }
        return strippedIDs;
    }

    private static void cyclesOPTIX(PrintStream scriptWriter, RenderTask renderTask) {
        var strippedIDs = stripDeviceTypeFromID(renderTask.getScriptInfo().getDeviceIDs());
    }

    private static void cyclesCUDA(PrintStream scriptWriter, RenderTask renderTask) {
        // Devices = 0, CUDA Devices
        var strippedIDs = stripDeviceTypeFromID(renderTask.getScriptInfo().getDeviceIDs());
        scriptWriter.println("cycles_prefs.compute_device_type = 'CUDA'");
        scriptWriter.println();
        scriptWriter.println("devices = cycles_prefs.get_devices()");
        scriptWriter.println("cuda_devices = devices[0]");
        scriptWriter.println("selected_cuda = []");
        for (String id : strippedIDs) {
            scriptWriter.println("selected_cuda.append(" + id + ")");
        }
        scriptWriter.println();
        scriptWriter.println("for i in range(len(selected_cuda)):");
        scriptWriter.println("\tif(cuda_devices[i] == selected_cuda[i]):");
        scriptWriter.println("\t\tcuda_devices[i].use = True");
        scriptWriter.println();
        scriptWriter.println("unselected_cuda = list(set(cuda_devices) - set(selected_cuda))");
        scriptWriter.println();
        scriptWriter.println("for i in range(len(unselected_cuda)):");
        scriptWriter.println("\tif(cuda_devices[i] == unselected_cuda[i]):");
        scriptWriter.println("\t\tcuda_devices[i].use = False");
        scriptWriter.println();
        scriptWriter.println("for dev in devices[1]:");
        scriptWriter.println("\tdev.use = False");
    }

    private static void cyclesOPENCL(PrintStream scriptWriter, RenderTask renderTask) {
        // Devices = 1, OPENCL Devices
        var strippedIDs = stripDeviceTypeFromID(renderTask.getScriptInfo().getDeviceIDs());

        scriptWriter.println("cycles_prefs.compute_device_type = 'OPENCL'");
        scriptWriter.println("devices = cycles_prefs.get_devices()");
        scriptWriter.println("opencl_devices = devices[1]");
        scriptWriter.println("selected_opencl = []");
        for (String id : strippedIDs) {
            scriptWriter.println("selected_opencl.append(" + id + ")");
        }
        scriptWriter.println();
        scriptWriter.println("for i in range(len(selected_opencl)):");
        scriptWriter.println("\tif(opencl_devices[i] == selected_opencl[i]):");
        scriptWriter.println("\t\topencl_devices[i].use = True");
        scriptWriter.println();
        scriptWriter.println("unselected_opencl = list(set(opencl_devices) - set(selected_opencl))");
        scriptWriter.println();
        scriptWriter.println("for i in range(len(unselected_opencl)):");
        scriptWriter.println("\tif(opencl_devices[i] == unselected_opencl[i]):");
        scriptWriter.println("\t\topencl_devices[i].use = False");
        scriptWriter.println();
        scriptWriter.println("for dev in devices[0]:");
        scriptWriter.println("\tdev.use = False");
    }


}
