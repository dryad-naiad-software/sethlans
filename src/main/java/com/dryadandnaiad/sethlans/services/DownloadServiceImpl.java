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
import com.dryadandnaiad.sethlans.enums.Role;
import com.dryadandnaiad.sethlans.models.blender.BlenderArchive;
import com.dryadandnaiad.sethlans.models.system.Notification;
import com.dryadandnaiad.sethlans.models.user.User;
import com.dryadandnaiad.sethlans.repositories.BlenderArchiveRepository;
import com.dryadandnaiad.sethlans.repositories.NotificationRepository;
import com.dryadandnaiad.sethlans.repositories.UserRepository;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.UUID;

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
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public DownloadServiceImpl(BlenderArchiveRepository blenderArchiveRepository, UserRepository userRepository,
                               NotificationRepository notificationRepository) {
        this.blenderArchiveRepository = blenderArchiveRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Async
    public void downloadBlenderFilesAsync() {
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            log.debug(e.getMessage());
        }
        while(true) {
            log.info("Attempting to download any needed Blender Binaries.");
            var downloadDir = ConfigUtils.getProperty(ConfigKeys.DOWNLOAD_DIR);
            var blenderBinaries = blenderArchiveRepository.findAll();
            for (BlenderArchive blenderArchive : blenderBinaries) {
                if (!blenderArchive.isDownloaded()) {
                    var downloadedFile = BlenderUtils.downloadBlenderToServer(blenderArchive.getBlenderVersion(),
                            downloadDir, blenderArchive.getBlenderOS());
                    if (downloadedFile != null) {
                        if (blenderArchive.getBlenderFileMd5().equals(FileUtils.getMD5ofFile(downloadedFile))) {
                            blenderArchive.setBlenderFile(downloadedFile.toString());
                            blenderArchive.setDownloaded(true);
                            blenderArchiveRepository.save(blenderArchive);
                            var notification = Notification.builder()
                                    .notificationID(UUID.randomUUID().toString())
                                    .messageDate(LocalDateTime.now())
                                    .message("Blender " + blenderArchive.getBlenderVersion() + " for "
                                            + blenderArchive.getBlenderOS().name() + " has been downloaded.")
                                    .build();
                            var super_administrators =
                                    userRepository.findAllByRolesContaining(Role.SUPER_ADMINISTRATOR);
                            var administrators = userRepository.findAllByRolesContaining(Role.ADMINISTRATOR);
                            var admins = new LinkedHashSet<>(super_administrators);
                            admins.addAll(administrators);
                            for (User user : admins) {
                                notification.setUserID(user.getUserID());
                                notificationRepository.save(notification);
                            }
                        } else {
                            FileSystemUtils.deleteRecursively(downloadedFile);
                        }

                    }
                }
            }
            try {
                Thread.sleep(600000);
            } catch (InterruptedException e) {
                log.debug(e.getMessage());
            }
        }

    }
}
