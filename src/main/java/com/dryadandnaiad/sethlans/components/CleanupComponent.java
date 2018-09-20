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

package com.dryadandnaiad.sethlans.components;

import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.dryadandnaiad.sethlans.services.system.SethlansLogManagementService;
import com.dryadandnaiad.sethlans.utils.SethlansConfigUtils;
import com.google.common.base.Throwables;
import org.apache.commons.io.FileUtils;
import org.hibernate.envers.Audited;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;

/**
 * Created Mario Estrella on 1/11/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Component
@Profile({"SERVER", "NODE", "DUAL"})
public class CleanupComponent {
    private SethlansLogManagementService sethlansLogManagementService;

    private static final Logger LOG = LoggerFactory.getLogger(CleanupComponent.class);

    @PostConstruct
    public void cleanFiles() {
        SethlansMode sethlansMode = SethlansMode.valueOf(SethlansConfigUtils.getProperty(SethlansConfigKeys.MODE));
        sethlansLogManagementService.checkAndArchiveLogFiles();
        String tempDir = SethlansConfigUtils.getProperty(SethlansConfigKeys.TEMP_DIR);
        File tempDirToClean = new File(tempDir);
        try {
            if (tempDirToClean.exists()) {
                FileUtils.cleanDirectory(tempDirToClean);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage() + Throwables.getStackTraceAsString(e));
        }

        if (!sethlansMode.equals(SethlansMode.SERVER)) {
            String cacheDir = SethlansConfigUtils.getProperty(SethlansConfigKeys.CACHE_DIR);
            String blendFileCache = SethlansConfigUtils.getProperty(SethlansConfigKeys.BLEND_FILE_CACHE_DIR);

            File cacheDirToClean = new File(cacheDir);
            File blendFileDirToClean = new File(blendFileCache);
            try {
                if (cacheDirToClean.exists()) {
                    FileUtils.cleanDirectory(cacheDirToClean);
                }
                if (blendFileDirToClean.exists()) {
                    FileUtils.cleanDirectory(blendFileDirToClean);
                }
            } catch (IOException e) {
                LOG.error(e.getMessage() + Throwables.getStackTraceAsString(e));
            }
        }

    }

    @Autowired
    public void setSethlansLogManagementService(SethlansLogManagementService sethlansLogManagementService) {
        this.sethlansLogManagementService = sethlansLogManagementService;
    }
}
