package com.dryadandnaiad.sethlans.services.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
                Thread.sleep(10000);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            LOG.debug("Ending multicast");
        }


    }
}
