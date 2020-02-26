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

import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.utils.Resources;
import com.google.common.base.Throwables;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.dryadandnaiad.sethlans.utils.SethlansConfigUtils.writeProperty;
import static com.dryadandnaiad.sethlans.utils.SethlansFileUtils.archiveExtract;

/**
 * Created Mario Estrella on 4/6/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class FFmpegSetupServiceImpl implements FFmpegSetupService {
    private static final Logger LOG = LoggerFactory.getLogger(FFmpegSetupServiceImpl.class);


    @Override
    public boolean installFFmpeg(String binaryDir) {
        String ffmpegFile = copyFFmpeg(binaryDir);
        if (archiveExtract(ffmpegFile, new File(binaryDir), true)) {
            if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_LINUX) {
                try {
                    ProcessBuilder pb = new ProcessBuilder("chmod", "-R", "+x", binaryDir + "ffmpeg" + File.separator + "bin");
                    pb.start();
                } catch (IOException e) {
                    LOG.error(Throwables.getStackTraceAsString(e));
                    return false;
                }
            }
            if (SystemUtils.IS_OS_WINDOWS) {
                writeProperty(SethlansConfigKeys.FFMPEG_BIN, binaryDir + "ffmpeg" + File.separator + "bin" + File.separator + "ffmpeg.exe");
            }
            if (SystemUtils.IS_OS_MAC || SystemUtils.IS_OS_LINUX) {
                writeProperty(SethlansConfigKeys.FFMPEG_BIN, binaryDir + "ffmpeg" + File.separator + "bin" + File.separator + "ffmpeg");
            }

            return true;
        }
        return false;
    }

    private String copyFFmpeg(String binaryDir) {
        try {
            if (SystemUtils.IS_OS_WINDOWS) {
                String arch = System.getenv("PROCESSOR_ARCHITECTURE");
                String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");

                String realArch = arch.endsWith("64")
                        || wow64Arch != null && wow64Arch.endsWith("64")
                        ? "64" : "32";
                if (realArch.equals("64")) {
                    String filename = "ffmpeg-windows64.txz";
                    String windows64 = "archives/ffmpeg/" + filename;
                    LOG.debug("Preparing FFmpeg binaries for Windows");
                    InputStream inputStream = new Resources(windows64).getResource();
                    String path = binaryDir + filename;
                    Files.copy(inputStream, Paths.get(path));
                    inputStream.close();
                    return filename;
                }
            }
            if (SystemUtils.IS_OS_LINUX) {
                if (SystemUtils.OS_ARCH.contains("64")) {
                    String filename = "ffmpeg-linux64.txz";
                    String linux64 = "archives/ffmpeg/" + filename;
                    LOG.debug("Preparing FFmpeg binaries for Linux");
                    InputStream inputStream = new Resources(linux64).getResource();
                    String path = binaryDir + filename;
                    Files.copy(inputStream, Paths.get(path));
                    inputStream.close();
                    return filename;

                }
            }
            if (SystemUtils.IS_OS_MAC) {
                String filename = "ffmpeg-macOS.txz";
                String macOS = "archives/ffmpeg/" + filename;
                LOG.debug("Preparing FFmpeg binaries for Mac");
                InputStream inputStream = new Resources(macOS).getResource();
                String path = binaryDir + filename;
                Files.copy(inputStream, Paths.get(path));
                return filename;

            }
        } catch (IOException e) {
            LOG.error("Error during resource copy" + Throwables.getStackTraceAsString(e));
        }
        return null;
    }
}
