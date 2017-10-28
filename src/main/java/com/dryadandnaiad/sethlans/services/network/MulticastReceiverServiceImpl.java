package com.dryadandnaiad.sethlans.services.network;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.MulticastSocket;
import java.util.Set;

/**
 * Created Mario Estrella on 10/27/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class MulticastReceiverServiceImpl implements  MulticastReceiverService{

    private MulticastSocket socket = null;
    private byte[] buffer = new byte[256];

    @Value("${sethlans.multicast}")
    private String multicastIP;

    @Override
    public Set<String> currentSethlansClients() {
        return null;
    }
}
