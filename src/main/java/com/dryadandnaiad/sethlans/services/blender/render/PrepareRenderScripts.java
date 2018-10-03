/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC
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

package com.dryadandnaiad.sethlans.services.blender.render;

import com.dryadandnaiad.sethlans.domains.database.render.RenderTask;
import com.dryadandnaiad.sethlans.domains.hardware.GPUDevice;
import com.dryadandnaiad.sethlans.domains.info.NodeInfo;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.osnative.hardware.gpu.GPU;
import com.dryadandnaiad.sethlans.services.blender.BlenderPythonScriptService;
import com.dryadandnaiad.sethlans.utils.SethlansQueryUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.dryadandnaiad.sethlans.utils.SethlansConfigUtils.getProperty;

/**
 * Created Mario Estrella on 9/16/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
class PrepareRenderScripts {

    private static final Logger LOG = LoggerFactory.getLogger(PrepareRenderScripts.class);

    static String setCyclesGPURenderScript(BlenderPythonScriptService blenderPythonScriptService, String tileSizeGPU, RenderTask renderTask, NodeInfo nodeInfo) {
        String script;
        String deviceID = getProperty(SethlansConfigKeys.GPU_DEVICE);
        List<String> deviceList = Arrays.asList(deviceID.split(","));
        List<String> deviceIDList = new ArrayList<>();
        if (nodeInfo.isCombined()) {
            boolean isCuda = false;
            LOG.info("Running render task using " + deviceID);
            for (String device : deviceList) {
                deviceIDList.add(StringUtils.substringAfter(device, "_"));
                isCuda = SethlansQueryUtils.isCuda(device);
            }
            script = blenderPythonScriptService.writeCyclesRenderPythonScript(renderTask.getComputeType(),
                    renderTask.getRenderDir(), deviceIDList,
                    getUnselectedIds(deviceList), isCuda,
                    renderTask.getRenderOutputFormat(),
                    tileSizeGPU,
                    renderTask.getTaskResolutionX(),
                    renderTask.getTaskResolutionY(),
                    renderTask.getPartResPercentage(),
                    renderTask.getSamples(),
                    renderTask.getBlenderFramePart().getPartPositionMaxX(),
                    renderTask.getBlenderFramePart().getPartPositionMinX(),
                    renderTask.getBlenderFramePart().getPartPositionMaxY(),
                    renderTask.getBlenderFramePart().getPartPositionMinY());
        } else {
            LOG.info("Running render task using " + renderTask.getDeviceID());
            boolean isCuda = SethlansQueryUtils.isCuda(renderTask.getDeviceID());
            deviceIDList.add(StringUtils.substringAfter(renderTask.getDeviceID(), "_"));
            script = blenderPythonScriptService.writeCyclesRenderPythonScript(renderTask.getComputeType(),
                    renderTask.getRenderDir(), deviceIDList,
                    getUnselectedIds(deviceList), isCuda,
                    renderTask.getRenderOutputFormat(),
                    tileSizeGPU,
                    renderTask.getTaskResolutionX(),
                    renderTask.getTaskResolutionY(),
                    renderTask.getPartResPercentage(),
                    renderTask.getSamples(),
                    renderTask.getBlenderFramePart().getPartPositionMaxX(),
                    renderTask.getBlenderFramePart().getPartPositionMinX(),
                    renderTask.getBlenderFramePart().getPartPositionMaxY(),
                    renderTask.getBlenderFramePart().getPartPositionMinY());
        }
        return script;
    }

    static String setCyclesCPURenderScript(BlenderPythonScriptService blenderPythonScriptService, String tileSizeCPU, RenderTask renderTask) {
        List<String> emptyList = new ArrayList<>();
        String script = blenderPythonScriptService.writeCyclesRenderPythonScript(renderTask.getComputeType(),
                renderTask.getRenderDir(), emptyList,
                emptyList, false, renderTask.getRenderOutputFormat(), tileSizeCPU,
                renderTask.getTaskResolutionX(),
                renderTask.getTaskResolutionY(),
                renderTask.getPartResPercentage(),
                renderTask.getSamples(),
                renderTask.getBlenderFramePart().getPartPositionMaxX(),
                renderTask.getBlenderFramePart().getPartPositionMinX(),
                renderTask.getBlenderFramePart().getPartPositionMaxY(),
                renderTask.getBlenderFramePart().getPartPositionMinY());
        return script;
    }

    static String setBlenderRenderScript(BlenderPythonScriptService blenderPythonScriptService, String tileSizeCPU, RenderTask renderTask) {
        String script = blenderPythonScriptService.writeBlenderRenderPythonScript(renderTask.getRenderDir(), renderTask.getRenderOutputFormat(), tileSizeCPU,
                renderTask.getTaskResolutionX(), renderTask.getTaskResolutionY(), renderTask.getPartResPercentage(),
                renderTask.getBlenderFramePart().getPartPositionMaxX(), renderTask.getBlenderFramePart().getPartPositionMinX(),
                renderTask.getBlenderFramePart().getPartPositionMaxY(),
                renderTask.getBlenderFramePart().getPartPositionMinY());
        return script;
    }

    private static List<String> getUnselectedIds(List<String> deviceList) {
        List<GPUDevice> gpuDeviceList = GPU.listDevices();
        List<String> gpusToCompare = new ArrayList<>();
        List<String> unselectedIds = new ArrayList<>();
        for (GPUDevice gpuDevice : gpuDeviceList) {
            gpusToCompare.add(gpuDevice.getDeviceID());
        }
        gpusToCompare.removeAll(deviceList);

        for (String gpUs : gpusToCompare) {
            unselectedIds.add(StringUtils.substringAfter(gpUs, "_"));
        }
        LOG.debug("The following devices are unselected: " + gpusToCompare);
        return unselectedIds;
    }
}
