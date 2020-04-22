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

import com.dryadandnaiad.sethlans.devices.ScanGPU;
import com.dryadandnaiad.sethlans.enums.ComputeOn;
import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Mario Estrella on 4/19/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class QueryUtils {

    private static final DecimalFormat ROUNDED_DOUBLE_DECIMALFORMAT;

    static {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        otherSymbols.setDecimalSeparator('.');
        otherSymbols.setGroupingSeparator(',');
        ROUNDED_DOUBLE_DECIMALFORMAT = new DecimalFormat("####0.00", otherSymbols);
        ROUNDED_DOUBLE_DECIMALFORMAT.setGroupingUsed(false);
    }

    /**
     * Creates a short UUID of 13 characters in the format of xxxxxxxx-xxxx
     *
     * @return String output of short UUID
     */
    public static String getShortUUID() {
        return UUID.randomUUID().toString().substring(0, 13);
    }

    /**
     * Returns the operating system name (Windows, Linux, MacOS) as well as if it's 32bits or 64bits
     *
     * @return String in the form of "Windows64"
     */
    public static String getOS() {
        SystemInfo si = new SystemInfo();
        HardwareAbstractionLayer hal = si.getHardware();
        OperatingSystem os = si.getOperatingSystem();
        int bits = os.getBitness();
        if (SystemUtils.IS_OS_WINDOWS) {
            if (bits == 64) {
                return "Windows64";
            } else {
                return "Windows32";
            }
        }
        if (SystemUtils.IS_OS_MAC) {
            return "MacOS";
        }
        if (SystemUtils.IS_OS_LINUX) {
            if (bits == 64) {
                return "Linux64";
            } else {
                return "Linux32";
            }
        }
        return null;
    }

    /**
     * Returns the currently configured mode for Sethlans.
     *
     * @return SethlansMode enum
     */
    public static SethlansMode getMode() {
        return SethlansMode.valueOf(ConfigUtils.getProperty(ConfigKeys.MODE));
    }

    /**
     * Returns the hostname for the current system
     *
     * @return String hostname
     */
    public static String getHostname() {
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            log.error(Throwables.getStackTraceAsString(e));
        }
        int indexEnd = hostname.indexOf(".");
        if (indexEnd != -1) {
            log.debug(hostname + " contains a domain name. Removing it.");
            hostname = hostname.substring(0, indexEnd);
        }
        return hostname.toUpperCase();
    }


    /**
     * Returns the IP address for the current system
     *
     * @return String ip
     */
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

    public static boolean getFirstTime() {
        return Boolean.parseBoolean(ConfigUtils.getProperty(ConfigKeys.FIRST_TIME));
    }

    public static Set<ComputeOn> getAvailableMethods() {
        Set<ComputeOn> availableMethods = new HashSet<>();
        if (ScanGPU.listDevices().size() != 0) {
            availableMethods.add(ComputeOn.CPU_GPU);
            availableMethods.add(ComputeOn.GPU);
            availableMethods.add(ComputeOn.CPU);
            availableMethods.add(ComputeOn.HYBRID);
        } else {
            availableMethods.add(ComputeOn.CPU);
        }
        return availableMethods;

    }
}
