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

package com.dryadandnaiad.sethlans.services.system;

import com.dryadandnaiad.sethlans.domains.info.Log;
import com.dryadandnaiad.sethlans.enums.SethlansConfigKeys;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created Mario Estrella on 4/17/2018.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class SethlansLogRetrievalServiceImpl implements SethlansLogRetrievalService {
    private static final Logger LOG = LoggerFactory.getLogger(SethlansLogRetrievalServiceImpl.class);

    @Override
    public List<Log> sethlansLogList() {
        List<Log> logList = new ArrayList<>();
        String sethlansLog = SethlansUtils.getProperty(SethlansConfigKeys.LOGGING_FILE.toString());
        LOG.debug("Reading log entries from " + sethlansLog);
        populateLogList(sethlansLog, logList);
        return logList;
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
        } catch (IOException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }
    }

}
