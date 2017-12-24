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

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.HashSet;
import java.util.Set;

/**
 * Created Mario Estrella on 10/27/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class MulticastReceiverServiceImpl implements  MulticastReceiverService{
    private static final Logger LOG = LoggerFactory.getLogger(MulticastReceiverServiceImpl.class);

    @Value("${sethlans.multicast}")
    private String multicastIP;

    @Value("${sethlans.multicast.port}")
    private String multicastPort;

    @Override
    public Set<String> currentSethlansClients() {
        Set<String> currentClients = new HashSet<>();
        byte[] buffer = new byte[256];
        try {
            MulticastSocket clientSocket = new MulticastSocket(Integer.parseInt(multicastPort));
            clientSocket.joinGroup(InetAddress.getByName(multicastIP));
            long start_time = System.currentTimeMillis();
            long wait_time = 12000;
            long end_time = start_time + wait_time;

            while (System.currentTimeMillis() < end_time){
                DatagramPacket msgPacket = new DatagramPacket(buffer, buffer.length);
                clientSocket.receive(msgPacket);

                String msg = new String(msgPacket.getData(), 0, msgPacket.getLength());
                currentClients.add(msg);
                LOG.debug(msg);
            }
        } catch (IOException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }

        LOG.debug("Number of clients detected: " + currentClients.size());

        return currentClients;
    }
}
