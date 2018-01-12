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

package com.dryadandnaiad.sethlans.components;

import com.google.common.base.Throwables;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
    @Value("${sethlans.tempDir}")
    private String tempDir;
    @Value("${sethlans.cacheDir}")
    private String cacheDir;

    private static final Logger LOG = LoggerFactory.getLogger(CleanupComponent.class);

    @PostConstruct
    public void cleanFiles() {
        File tempDirToClean = new File(tempDir);
        File cacheDirToClean = new File(cacheDir);
        try {
            if (tempDirToClean.exists()) {
                FileUtils.cleanDirectory(tempDirToClean);
            }
            if (cacheDirToClean.exists()) {
                FileUtils.cleanDirectory(cacheDirToClean);
            }
        } catch (IOException e) {
            LOG.error(e.getMessage() + Throwables.getStackTraceAsString(e));
        }
    }
}
