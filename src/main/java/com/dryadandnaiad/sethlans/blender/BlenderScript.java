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

package com.dryadandnaiad.sethlans.blender;

import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.dryadandnaiad.sethlans.enums.ComputeOn;
import com.dryadandnaiad.sethlans.enums.DeviceType;
import com.dryadandnaiad.sethlans.enums.ImageOutputFormat;
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;


/**
 * File created by Mario Estrella on 5/3/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class BlenderScript {


    public static boolean writeRenderScript(RenderTask renderTask) {
        new File(renderTask.getTaskDir()).mkdir();
        File script = new File(renderTask.getTaskDir() + File.separator + renderTask.getTaskID() + ".py");
        try {
            var location = renderTask.getTaskDir().replace("\\", "/");
            var versionAsFloat = QueryUtils.versionAsFloat(renderTask.getBlenderVersion());

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
                log.error("Device IDs cannot be null");
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
            scriptWriter.println("scene = bpy.data.scenes[0]");

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
                    scriptWriter.println();
                    scriptWriter.println("cycles_prefs = prefs.addons['cycles'].preferences");
                    if (renderTask.getScriptInfo().getComputeOn().equals(ComputeOn.GPU) || renderTask.getScriptInfo().getComputeOn().equals(ComputeOn.HYBRID)) {
                        if (!cyclesScript(scriptWriter, renderTask, versionAsFloat)) {
                            return false;
                        }
                    } else {
                        scriptWriter.println("scene.cycles.device = 'CPU'");
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
            scriptWriter.println("\tscene.render.resolution_percentage = " +
                    renderTask.getScriptInfo().getTaskResPercentage());
            if (renderTask.getScriptInfo().getBlenderEngine().equals(BlenderEngine.CYCLES)) {
                scriptWriter.println("\tscene.cycles.samples = " + renderTask.getScriptInfo().getSamples());
            }

            if (renderTask.getScriptInfo().getImageOutputFormat() == null) {
                log.error("ImageOutputFormat cannot be null.");
                scriptWriter.flush();
                scriptWriter.close();
                script.delete();
                return false;
            }

            // Set Part
            if (!renderTask.isBenchmark() && renderTask.isUseParts()) {
                if (renderTask.getFrameInfo().getPartMaxX() == null |
                        renderTask.getFrameInfo().getPartMinX() == null ||
                        renderTask.getFrameInfo().getPartMaxY() == null ||
                        renderTask.getFrameInfo().getPartMinY() == null) {
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
            if (renderTask.getScriptInfo().getComputeOn().equals(ComputeOn.CPU) ||
                    renderTask.getScriptInfo().getComputeOn().equals(ComputeOn.HYBRID)) {
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


            if (versionAsFloat < 2.99) {
                scriptWriter.println("\tscene.render.tile_x = " + renderTask.getScriptInfo().getTaskTileSize());
                scriptWriter.println("\tscene.render.tile_y = " + renderTask.getScriptInfo().getTaskTileSize());
            }


            // Final Settings
            if (renderTask.getScriptInfo().getImageOutputFormat().equals(ImageOutputFormat.PNG)) {
                scriptWriter.println("\tscene.render.image_settings.file_format = 'PNG'");
                scriptWriter.println("\tscene.render.image_settings.color_mode = 'RGBA'");
                scriptWriter.println("\tscene.render.image_settings.color_depth = '16'");
            }

            if (renderTask.getScriptInfo().getImageOutputFormat().equals(ImageOutputFormat.HDR)) {
                scriptWriter.println("\tscene.render.image_settings.file_format = 'HDR'");
                scriptWriter.println("\tscene.render.image_settings.color_mode = 'RGB'");
                scriptWriter.println("\tscene.render.image_settings.color_depth = '32'");
            }

            if (renderTask.getScriptInfo().getImageOutputFormat().equals(ImageOutputFormat.TIFF)) {
                scriptWriter.println("\tscene.render.image_settings.file_format = 'TIFF'");
                scriptWriter.println("\tscene.render.image_settings.color_mode = 'RGBA'");
                scriptWriter.println("\tscene.render.image_settings.color_depth = '16'");
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


    private static boolean cyclesScript(PrintStream scriptWriter, RenderTask renderTask, Float versionAsFloat) {
        scriptWriter.println("bpy.context.scene.cycles.device = 'GPU'");
        switch (renderTask.getScriptInfo().getDeviceType()) {
            case OPTIX:
                scriptWriter.println("cycles_prefs.compute_device_type = 'OPTIX'");
                if (versionAsFloat < 2.99) {
                    scriptWriter.println("hardware_devices = cycles_prefs.get_devices_for_type('OPTIX')");
                } else {
                    scriptWriter.println("cycles_prefs.refresh_devices()");
                    scriptWriter.println("hardware_devices = cycles_prefs['devices']");
                }
                break;
            case CUDA:
                scriptWriter.println("cycles_prefs.compute_device_type = 'CUDA'");
                if (versionAsFloat < 2.99) {
                    scriptWriter.println("hardware_devices = cycles_prefs.get_devices_for_type('CUDA')");
                } else {
                    scriptWriter.println("cycles_prefs.refresh_devices()");
                    scriptWriter.println("hardware_devices = cycles_prefs['devices']");
                }
                break;
            case OPENCL:
                scriptWriter.println("cycles_prefs.compute_device_type = 'OPENCL'");
                scriptWriter.println("hardware_devices = cycles_prefs.get_devices_for_type('OPENCL')");
                break;
            default:
                return false;
        }

        scriptWriter.println("selected_hardware = []");
        for (String id : renderTask.getScriptInfo().getDeviceIDs()) {
            if (renderTask.getScriptInfo().getDeviceType() == DeviceType.OPENCL) {
                if (versionAsFloat < 2.99) {
                    scriptWriter.println("hardware_devices[" + id + "]['use'] = True");
                    scriptWriter.println("selected_hardware.append(hardware_devices[" + id + "])");
                }
            } else {
                scriptWriter.println("for gpu in hardware_devices:");
                if (versionAsFloat < 2.99) {
                    scriptWriter.println("\tif '" + id + "' in gpu['id']:");
                } else {
                    if (renderTask.getScriptInfo().getDeviceType() == DeviceType.OPTIX) {
                        scriptWriter.println("\tif '" + id + "' in gpu['id'] and 'OptiX' in gpu['id']:");
                    } else {
                        scriptWriter.println("\tif '" + id + "' in gpu['id'] and 'OptiX' not in gpu['id']:");
                    }
                }
                scriptWriter.println("\t\tgpu['use'] = True");
                scriptWriter.println("\t\tselected_hardware.append(gpu)");
            }

            if (renderTask.getScriptInfo().getComputeOn().equals(ComputeOn.HYBRID) && !renderTask.getBlenderVersion().contains("2.7")) {
                scriptWriter.println("\tif 'CPU' in gpu['id']:");
                scriptWriter.println("\t\tgpu['use'] = True");
                scriptWriter.println("\t\tselected_hardware.append(gpu)");
            }
            scriptWriter.println();
            scriptWriter.println("print(\"Selected Devices: \")");
            scriptWriter.println("for gpu in selected_hardware:");
            scriptWriter.println("\tprint(gpu['id'])");
            scriptWriter.println();
            scriptWriter.println("unselected_hardware = list(set(hardware_devices) - set(selected_hardware))");
            scriptWriter.println("print(\"Unselected Devices: \")");
            scriptWriter.println("for gpu in unselected_hardware:");
            scriptWriter.println("\tgpu['use'] = False");
            scriptWriter.println("\tprint(gpu['id'])");
            if (versionAsFloat < 2.99) {
                switch (renderTask.getScriptInfo().getDeviceType()) {
                    case OPTIX -> {
                        scriptWriter.println("other_devices = cycles_prefs.get_devices_for_type('CUDA')");
                        scriptWriter.println("for gpu in other_devices:");
                        scriptWriter.println("\tgpu['use'] = False");
                        scriptWriter.println("other_devices = cycles_prefs.get_devices_for_type('OPENCL')");
                        scriptWriter.println("for gpu in other_devices:");
                        scriptWriter.println("\tgpu['use'] = False");
                    }
                    case CUDA -> {
                        scriptWriter.println("other_devices = cycles_prefs.get_devices_for_type('OPTIX')");
                        scriptWriter.println("for gpu in other_devices:");
                        scriptWriter.println("\tgpu['use'] = False");
                        scriptWriter.println("other_devices = cycles_prefs.get_devices_for_type('OPENCL')");
                        scriptWriter.println("for gpu in other_devices:");
                        scriptWriter.println("\tgpu['use'] = False");
                    }
                    case OPENCL -> {
                        scriptWriter.println("other_devices = cycles_prefs.get_devices_for_type('CUDA')");
                        scriptWriter.println("for gpu in other_devices:");
                        scriptWriter.println("\tgpu['use'] = False");
                        scriptWriter.println("other_devices = cycles_prefs.get_devices_for_type('OPTIX')");
                        scriptWriter.println("for gpu in other_devices:");
                        scriptWriter.println("\tgpu['use'] = False");
                    }
                }


            }
            return true;
        }
        return false;
    }


}
