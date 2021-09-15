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

package com.dryadandnaiad.sethlans.services;

import com.dryadandnaiad.sethlans.blender.BlenderUtils;
import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.models.blender.BlenderArchive;
import com.dryadandnaiad.sethlans.repositories.BlenderArchiveRepository;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * File created by Mario Estrella on 6/2/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
@Service
@Profile({"SERVER", "DUAL"})
public class DownloadServiceImpl implements DownloadService {
    private final BlenderArchiveRepository blenderArchiveRepository;

    public DownloadServiceImpl(BlenderArchiveRepository blenderArchiveRepository) {
        this.blenderArchiveRepository = blenderArchiveRepository;
    }

    @Override
    @Async
    @Scheduled(fixedDelay=50000)
    public void downloadBlenderFilesAsync() {
        log.info("Attempting to download any needed Blender Binaries.");
        var downloadDir = ConfigUtils.getProperty(ConfigKeys.DOWNLOAD_DIR);
        var blenderBinaries = blenderArchiveRepository.findAll();
        for (BlenderArchive blenderArchive : blenderBinaries) {
            if (!blenderArchive.isDownloaded()) {
                var downloadedFile = BlenderUtils.downloadBlenderToServer(blenderArchive.getBlenderVersion(),
                        downloadDir, blenderArchive.getBlenderOS());
                if (downloadedFile != null) {
                    blenderArchive.setBlenderFile(downloadedFile.toString());
                    blenderArchive.setBlenderFileMd5(FileUtils.getMD5ofFile(downloadedFile));
                    blenderArchive.setDownloaded(true);
                    blenderArchiveRepository.save(blenderArchive);
                }
            }
        }
    }
}
