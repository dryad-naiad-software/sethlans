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

package com.dryadandnaiad.sethlans.utils;

import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Static methods that write and retrieve information from the sethlans.properties file
 * <p>
 * File created by Mario Estrella on 4/22/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class PropertiesUtils {


    public static String getSelectedCores() {
        return ConfigUtils.getProperty(ConfigKeys.CPU_CORES);
    }


    public static String getPort() {
        return ConfigUtils.getProperty(ConfigKeys.HTTPS_PORT);
    }


    public static boolean getFirstTime() {
        return Boolean.parseBoolean(ConfigUtils.getProperty(ConfigKeys.FIRST_TIME));
    }


    public static String getIP() {
        String ip = null;
        try {
            ip = ConfigUtils.getProperty(ConfigKeys.SETHLANS_IP);
            if (ip == null) {
                if (SystemUtils.IS_OS_LINUX) {
                    // Make a connection to 8.8.8.8 DNS in order to get IP address
                    Socket s = new Socket("8.8.8.8", 53);
                    ip = s.getLocalAddress().getHostAddress();
                    s.close();
                } else {
                    ip = InetAddress.getLocalHost().getHostAddress();
                }
            }
        } catch (IOException e) {
            log.error(Throwables.getStackTraceAsString(e));
        }
        return ip;
    }

    public static SethlansMode getMode() {
        return SethlansMode.valueOf(ConfigUtils.getProperty(ConfigKeys.MODE));
    }


}
