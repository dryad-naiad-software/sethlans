/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC.
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

package com.dryadandnaiad.sethlans.services.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Created Mario Estrella on 10/27/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
@Profile({"NODE", "DUAL"})
public class MulticastSenderServiceImpl extends Thread implements MulticastSenderService {
    private static final Logger LOG = LoggerFactory.getLogger(MulticastSenderServiceImpl.class);

    @Value("${sethlans.multicast}")
    private String multicastIP;

    @Value("${sethlans.multicast.port}")
    private String multicastPort;

    @Async
    @Override
    public void sendSethlansIPAndPort(String ip, String port) {
        String message = ip + ":" + port;
        int multicastSocketPort = Integer.parseInt(multicastPort);
        try {
            byte[] buffer = message.getBytes();
            MulticastSocket multicastSocket = new MulticastSocket(multicastSocketPort);
            multicastSocket.setReuseAddress(true);
            InetAddress group = InetAddress.getByName(multicastIP);
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, multicastSocketPort);
                multicastSocket.send(packet);
                LOG.debug("Sent Multicast");
                Thread.sleep(5000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            LOG.debug("Ending multicast");
        }


    }
}
