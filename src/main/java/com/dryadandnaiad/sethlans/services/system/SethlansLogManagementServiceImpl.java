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

package com.dryadandnaiad.sethlans.services.system;

import com.dryadandnaiad.sethlans.domains.info.Log;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.utils.SethlansFileUtils;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.dryadandnaiad.sethlans.utils.SethlansConfigUtils.getProperty;

/**
 * Created Mario Estrella on 4/17/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class SethlansLogManagementServiceImpl implements SethlansLogManagementService {
    private static final Logger LOG = LoggerFactory.getLogger(SethlansLogManagementServiceImpl.class);

    @Override
    @Async
    public void checkAndArchiveLogFiles(){
        while(true) {
            LOG.debug("Checking to see if logs are at max capacity");
            if(isLogDirectoryAtMax()) {
                archiveLogFiles();
            }
            try {
                Thread.sleep(900000);
            } catch (InterruptedException e) {
                LOG.debug("Stopping Log Management Service");
                break;
            }
        }

    }

    @Override
    public List<Log> sethlansLogList() {
        List<Log> logList = new ArrayList<>();
        String sethlansLog = getProperty(SethlansConfigKeys.LOGGING_FILE);
        LOG.debug("Reading log entries from " + sethlansLog);
        populateLogList(sethlansLog, logList);
        return logList;
    }

    @Override
    public File retrieveLogFiles() {
        archiveLogFiles();
        LOG.debug("Retrieving Sethlans Log bundle.");
        String archiveName = "sethlans_log_bundle_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        String logDir = getProperty(SethlansConfigKeys.LOGGING_DIR);
        String tempDir = getProperty(SethlansConfigKeys.TEMP_DIR);
        try {
            String mainLog = getProperty(SethlansConfigKeys.LOGGING_FILE);
            File logArchiveDir = new File(logDir + File.separator + "archive");
            File[] logArchives = logArchiveDir.listFiles();
            List<String> archiveList = new ArrayList<>();
            archiveList.add(mainLog);
            for (File archive : logArchives) {
                archiveList.add(archive.toString());
            }
            return SethlansFileUtils.createArchive(archiveList, tempDir, archiveName);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return null;
    }

    @Override
    public boolean archiveLogFiles() {
        LOG.debug("Attempting to archive log files.");
        try {
            String archiveName = "sethlans_log_archive_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
            List<String> filesToArchive = new ArrayList<>();
            File logDir = new File(getProperty(SethlansConfigKeys.LOGGING_DIR));
            File logArchiveDir = new File(logDir.toString() + File.separator + "archive");
            if (logArchiveDir.mkdirs()) {
                File[] logFiles = logDir.listFiles();
                for (File log : logFiles) {
                    List<String> logFileName = Arrays.asList(log.toString().split("\\.(?=[^.]+$)"));
                    if (!logFileName.get(1).contains("log")) {
                        filesToArchive.add(log.toString());
                    }
                }
                File archive = SethlansFileUtils.createArchive(filesToArchive, logArchiveDir.toString(), archiveName);
                if (archive.exists()) {
                    for (File log : logFiles) {
                        List<String> logFileName = Arrays.asList(log.toString().split("\\.(?=[^.]+$)"));
                        if (!logFileName.get(1).contains("log")) {
                            log.delete();
                        }
                    }
                    LOG.debug("Log files successfully archived.");
                    return true;
                } else {
                    return false;
                }
            }


        } catch (Exception e) {
            LOG.error(e.getMessage());

        }
        return false;
    }

    private boolean isLogDirectoryAtMax(){
        File logDir = new File(getProperty(SethlansConfigKeys.LOGGING_DIR));
        File[] logFiles = logDir.listFiles();
        for (File log : logFiles) {
            List<String> logFileName = Arrays.asList(log.toString().split("\\.(?=[^.]+$)"));
            if (!logFileName.get(1).contains("7")) {
                return true;
            }
        }
        return false;
    }

    private void populateLogList(String logFile, List<Log> logList) {
        LOG.debug(logFile);
        try {
            BufferedReader input = new BufferedReader(new FileReader(logFile));
            String line;
            while ((line = input.readLine()) != null) {
                List<String> logEntry = Arrays.asList(line.split(","));
                Log log = new Log();
                if (logEntry.size() >= 5) {
                    // This if statement ensures we only get complete log entries
                    log.setDate(logEntry.get(0));
                    log.setLevel(logEntry.get(1));
                    log.setThread(logEntry.get(2));
                    log.setLoggingClass(logEntry.get(3));
                    if (logEntry.size() == 5) {
                        log.setMessage(logEntry.get(4));
                    } else {
                        // Log entries might contain comma's there should be only 5 fields so this ensures that messages are combined.
                        StringBuilder message = new StringBuilder();
                        for (int i = 4; i < logEntry.size(); i++) {
                            if (message.length() != 0) {
                                message.append(", ");
                            }
                            message.append(logEntry.get(i));
                        }
                        log.setMessage(message.toString());
                    }
                    logList.add(log);
                }

            }
            input.close();
        } catch (IOException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }
    }

}
