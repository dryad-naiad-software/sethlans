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
import com.dryadandnaiad.sethlans.models.blender.tasks.RenderTask;
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
class BlenderScripts {

    boolean writeBaseScript(RenderTask renderTask) {
        try {
            File script = new File(renderTask.getTaskDir() + File.separator + "script-" +
                    renderTask.getDeviceID() + ".py");

            var location = renderTask.getTaskDir().replace("\\", "/");

            PrintStream scriptWriter = new PrintStream(script);

            // Write Imports
            scriptWriter.println("import bpy");

            // Temp Directory
            scriptWriter.println("bpy.context.user_preferences.filepaths.temporary_directory = "
                    + "\"" + location + "\"");

            // Set Device
            if (renderTask.getBlenderEngine().equals(BlenderEngine.CYCLES)) {
                scriptWriter.println();
                scriptWriter.println("bpy.context.scene.cycles.device = " + "\"" + renderTask.getComputeOn() + "\"");
            }


            // Set Resolution
            scriptWriter.println();
            scriptWriter.println("for scene in bpy.data.scenes:");
            scriptWriter.println("\tscene.render.resolution_x = " + renderTask.getTaskResolutionX());
            scriptWriter.println("\tscene.render.resolution_y = " + renderTask.getTaskResolutionY());
            scriptWriter.println("\tscene.render.resolution_percentage = " + renderTask.getTaskResPercentage());

            // Tile Sizes
            scriptWriter.println();
            scriptWriter.println("bpy.context.scene.render.tile_x = " + renderTask.getTaskTileSize());
            scriptWriter.println("bpy.context.scene.render.tile_y = " + renderTask.getTaskTileSize());

            // Final Settings
            scriptWriter.println();
            scriptWriter.println("bpy.context.scene.render.use_border = True");
            scriptWriter.println("bpy.context.scene.render.use_crop_to_border = True");


            return false;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }


}
