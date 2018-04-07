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

package com.dryadandnaiad.sethlans.services.ffmpeg;

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.enums.ProjectStatus;
import com.dryadandnaiad.sethlans.enums.RenderOutputFormat;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;

/**
 * Created Mario Estrella on 4/6/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class FFmpegEncodeServiceImpl implements FFmpegEncodeService {
    private static final Logger LOG = LoggerFactory.getLogger(FFmpegEncodeServiceImpl.class);
    private BlenderProjectDatabaseService blenderProjectDatabaseService;

    @Async
    @Override
    public void encodeImagesToVideo(BlenderProject blenderProject) {
        String error;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);
        CommandLine ffmpeg = new CommandLine(SethlansUtils.getProperty(SethlansConfigKeys.FFMPEG_BIN.toString()));
        try {
            PrintWriter printWriter = new PrintWriter(new FileWriter(blenderProject.getProjectRootDir() +
                    File.separator + "imageList.txt"));
            for (String frameFileName : blenderProject.getFrameFileNames()) {
                printWriter.println("file '" + frameFileName + "'");
            }
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ffmpeg.addArgument("-i");
        ffmpeg.addArgument(blenderProject.getProjectRootDir() + File.separator + "imageList.txt");
        ffmpeg.addArgument("-c:v");
        if (blenderProject.getRenderOutputFormat() == RenderOutputFormat.AVI) {
            ffmpeg.addArgument("huffyuv");

        }
        if (blenderProject.getRenderOutputFormat() == RenderOutputFormat.MP4) {
            ffmpeg.addArgument("libx264");
            ffmpeg.addArgument("-crf");
            ffmpeg.addArgument("18");
            ffmpeg.addArgument("-preset");
            ffmpeg.addArgument("fast");
            ffmpeg.addArgument("-pix_fmt");
            ffmpeg.addArgument("yuv420p");
        }
        ffmpeg.addArgument("-r");
        ffmpeg.addArgument(blenderProject.getFrameRate());
        ffmpeg.addArgument(blenderProject.getMovieFileLocation());
        LOG.debug("Running following ffmpeg command " + ffmpeg.toString());
        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(pumpStreamHandler);
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
        try {
            executor.execute(ffmpeg, resultHandler);
            resultHandler.waitFor();
            BufferedReader in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray())));
            String output;
            while ((output = in.readLine()) != null) {
                LOG.debug(output);
            }
            error = errorStream.toString();
            LOG.debug(error);
            BlenderProject projectToUpdate = blenderProjectDatabaseService.getById(blenderProject.getId());
            projectToUpdate.setProjectStatus(ProjectStatus.Finished);
            blenderProjectDatabaseService.saveOrUpdate(projectToUpdate);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Autowired
    public void setBlenderProjectDatabaseService(BlenderProjectDatabaseService blenderProjectDatabaseService) {
        this.blenderProjectDatabaseService = blenderProjectDatabaseService;
    }
}
