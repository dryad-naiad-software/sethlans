/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC
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

package com.dryadandnaiad.sethlans.services.server;

import com.dryadandnaiad.sethlans.domains.BlenderFile;
import com.dryadandnaiad.sethlans.services.database.BlenderFileService;
import com.google.common.base.Throwables;
import org.apache.commons.io.FileUtils;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created Mario Estrella on 3/27/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class ServerBlenderSetupServiceImpl implements ServerBlenderSetupService {
    private static final Logger LOG = LoggerFactory.getLogger(ServerBlenderSetupServiceImpl.class);
    private BlenderFileService blenderFileService;
    private List<BlenderFile> blenderFiles;

    @Value("${sethlans.serverDir}")
    private String serverDir;

    @Autowired
    public void setBlenderFileService(BlenderFileService blenderFileService) {
        this.blenderFileService = blenderFileService;
    }


    private boolean extractBlender() throws Exception {
        File extractLocation = new File(serverDir + File.separator + "blender");
        extractLocation.mkdirs();
        BlenderFile toExtract = null;
        for (BlenderFile blenderFile : blenderFiles) {
            if (blenderFile.isServerBinary()) {
                toExtract = blenderFile;
            }
        }

        if (toExtract == null) {
            throw new Exception("No server blender binary found.");
        }

        File archive = new File(toExtract.getBlenderFile());
        Archiver archiver = null;

        if (extractLocation.list().length > 0) {
            FileUtils.deleteDirectory(extractLocation);

        } else {
            if (!toExtract.getBlenderBinaryOS().contains("Linux")) {
                archiver = ArchiverFactory.createArchiver(ArchiveFormat.ZIP);
                LOG.debug("Extracting zip file.");

            } else {
                archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.BZIP2);
                LOG.debug("Extracting tar.bz2 file.");
            }
            LOG.debug("Extracting " + archive + " to " + extractLocation);
            try {
                archiver.extract(archive, extractLocation);
            } catch (IOException e) {
                LOG.error("Error during extraction " + e.getMessage());
                LOG.error(Throwables.getStackTraceAsString(e));
                System.exit(1);

            }


        }
        return false;
    }

    @Override
    public boolean updateBlender() {
        blenderFiles = (List<BlenderFile>) blenderFileService.listAll();
        try {
            extractBlender();
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return false;
    }

    @Override
    public boolean installBlender(String serverDir) {
        blenderFiles = (List<BlenderFile>) blenderFileService.listAll();
        this.serverDir = serverDir;
        try {
            extractBlender();
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return false;
    }
}
