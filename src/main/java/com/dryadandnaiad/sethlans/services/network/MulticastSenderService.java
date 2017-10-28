package com.dryadandnaiad.sethlans.services.network;

/**
 * Created Mario Estrella on 10/27/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public interface MulticastSenderService {
    void sendSethlansIPAndPort(String ip, String port);
}
