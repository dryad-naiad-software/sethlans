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

import com.dryadandnaiad.sethlans.domains.blender.BlendFile;
import com.dryadandnaiad.sethlans.enums.BlenderEngine;
import com.google.common.base.Throwables;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * Created Mario Estrella on 3/29/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class BlenderParseBlendFileServiceImpl implements BlenderParseBlendFileService {
    private static final Logger LOG = LoggerFactory.getLogger(BlenderParseBlendFileServiceImpl.class);
    @Value("${sethlans.python.binary}")
    private String pythonBinary;

    @Value("${sethlans.scriptsDir}")
    private String scriptsDir;

    @Override
    public BlendFile parseBlendFile(String blendFile) {
        String error = null;
        try {
            LOG.debug("Parsing blend file: " + blendFile);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);
            CommandLine commandLine = new CommandLine(pythonBinary);

            commandLine.addArgument(scriptsDir + File.separator + "blend_info.py");
            commandLine.addArgument(blendFile);
            LOG.debug(commandLine.toString());

            DefaultExecutor executor = new DefaultExecutor();
            executor.setStreamHandler(pumpStreamHandler);
            DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
            executor.execute(commandLine, resultHandler);
            resultHandler.waitFor();

            BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray())));

            String output;
            while ((output = in.readLine()) != null) {
                LOG.debug(output);
            }
            error = errorStream.toString();

            List<String> values;
            values = Arrays.asList(output.split("\\s*,\\s*"));
            LOG.debug(values.toString());

            String sceneName = values.get(0);
            BlenderEngine engine = BlenderEngine.valueOf(values.get(1));
            int frameStart = Integer.parseInt(values.get(2));
            int frameEnd = Integer.parseInt(values.get(3));
            int frameSkip = Integer.parseInt(values.get(4));
            int resPercent = Integer.parseInt(values.get(5));
            int resolutionX = Integer.parseInt(values.get(6));
            int resolutionY = Integer.parseInt(values.get(7));
            String cameraName = values.get(8);
            int cyclesSamples;
            if (engine == BlenderEngine.CYCLES) {
                if (values.get(9).equals("None")) {
                    cyclesSamples = 0;
                } else {
                    cyclesSamples = Integer.parseInt(values.get(9));
                }

            } else {
                cyclesSamples = 0;
            }
            BlendFile parsedBlend = new BlendFile(sceneName.substring(2), engine, frameStart, frameEnd, frameSkip, resPercent, resolutionX, resolutionY, cameraName.substring(2), cyclesSamples);
            return parsedBlend;
        } catch (IOException | NullPointerException e) {
            LOG.error("Error parsing " + blendFile + "\n" + error);
            LOG.error(Throwables.getStackTraceAsString(e));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
