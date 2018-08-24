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

package com.dryadandnaiad.sethlans.services.ffmpeg;

import com.dryadandnaiad.sethlans.domains.database.blender.BlenderProject;
import com.dryadandnaiad.sethlans.enums.ProjectStatus;
import com.dryadandnaiad.sethlans.enums.RenderOutputFormat;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.services.database.BlenderProjectDatabaseService;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.concurrent.TimeUnit;

import static com.dryadandnaiad.sethlans.utils.SethlansConfigUtils.getProperty;

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
        String truncatedProjectName = StringUtils.left(blenderProject.getProjectName(), 10);
        String truncatedUUID = StringUtils.left(blenderProject.getProject_uuid(), 4);
        String cleanedProjectName = truncatedProjectName.replaceAll(" ", "").replaceAll("[^a-zA-Z0-9_-]", "").toLowerCase();

        String error;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);
        CommandLine ffmpeg = new CommandLine(getProperty(SethlansConfigKeys.FFMPEG_BIN.toString()));

        ffmpeg.addArgument("-framerate");
        ffmpeg.addArgument(blenderProject.getFrameRate());

        ffmpeg.addArgument("-i");
        for (String frameFileName : blenderProject.getFrameFileNames()) {
            try {
                FileUtils.copyFileToDirectory(new File(frameFileName), new File(blenderProject.getProjectRootDir() + File.separator + "temp" + File.separator));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        ffmpeg.addArgument(blenderProject.getProjectRootDir() + File.separator + "temp" + File.separator + cleanedProjectName + "-" + truncatedUUID + "-" + "%d.png");
        ffmpeg.addArgument("-c:v");
        if (blenderProject.getRenderOutputFormat() == RenderOutputFormat.AVI) {
            ffmpeg.addArgument("utvideo");
            ffmpeg.addArgument("-pix_fmt");
            ffmpeg.addArgument("yuv422p");

        }
        if (blenderProject.getRenderOutputFormat() == RenderOutputFormat.MP4) {
            ffmpeg.addArgument("libx264");
            ffmpeg.addArgument("-crf");
            ffmpeg.addArgument("17");
            ffmpeg.addArgument("-preset");
            ffmpeg.addArgument("medium");
            ffmpeg.addArgument("-pix_fmt");
            ffmpeg.addArgument("yuv420p");
        }

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
            in.close();

            BufferedReader err = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray())));
            while ((error = err.readLine()) != null) {
                LOG.debug(error);
            }
            err.close();

            BlenderProject projectToUpdate = blenderProjectDatabaseService.getById(blenderProject.getId());
            projectToUpdate.setProjectStatus(ProjectStatus.Finished);
            projectToUpdate.setMovieFileLocation(blenderProject.getMovieFileLocation());
            if (!blenderProject.isReEncode()) {
                projectToUpdate.setProjectEnd(TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS));
            }
            projectToUpdate.setReEncode(false);
            blenderProjectDatabaseService.saveOrUpdate(projectToUpdate);
            FileUtils.deleteDirectory(new File(blenderProject.getProjectRootDir() + File.separator + "temp"));
        } catch (IOException | InterruptedException e) {
            LOG.error(e.getMessage());
        }
    }

    @Autowired
    public void setBlenderProjectDatabaseService(BlenderProjectDatabaseService blenderProjectDatabaseService) {
        this.blenderProjectDatabaseService = blenderProjectDatabaseService;
    }

}
