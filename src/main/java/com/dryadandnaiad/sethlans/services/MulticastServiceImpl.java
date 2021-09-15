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

import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.dryadandnaiad.sethlans.utils.ConfigUtils;
import com.dryadandnaiad.sethlans.utils.QueryUtils;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Objects;

/**
 * File created by Mario Estrella on 6/11/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
@Service
public class MulticastServiceImpl implements MulticastService {

    @Async
    @Override
    @Scheduled(fixedDelay = 3000)
    public void sendSethlansMulticast() {
        var ip = QueryUtils.getIP();
        var port = ConfigUtils.getProperty(ConfigKeys.HTTPS_PORT);
        var multicastSocketPort =
                Integer.parseInt(Objects.requireNonNull(ConfigUtils.getProperty(ConfigKeys.MULTICAST_PORT)));
        var message = "Sethlans:" + ip + ":" + port;
        try {
            byte[] buffer = message.getBytes();
            var multicastSocket = new MulticastSocket(multicastSocketPort);
            multicastSocket.setReuseAddress(true);
            var multicastGroup = InetAddress.getByName(ConfigUtils.getProperty(ConfigKeys.MULTICAST_IP));
            var datagramPacket = new DatagramPacket(buffer, buffer.length, multicastGroup, multicastSocketPort);
            multicastSocket.send(datagramPacket);

        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
        }

    }
}
