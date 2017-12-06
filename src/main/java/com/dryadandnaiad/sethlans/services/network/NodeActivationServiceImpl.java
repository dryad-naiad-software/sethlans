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

import com.dryadandnaiad.sethlans.domains.database.node.SethlansNode;
import com.dryadandnaiad.sethlans.utils.SSLUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Created Mario Estrella on 12/5/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class NodeActivationServiceImpl implements NodeActivationService {
    private static final Logger LOG = LoggerFactory.getLogger(NodeActivationServiceImpl.class);

    @Async
    public void sendActivationRequest(SethlansNode sethlansNode) {
        LOG.debug("Sending Activation Request to Node");
        String ip = sethlansNode.getIpAddress();
        String port = sethlansNode.getNetworkPort();
        String activateURL = "https://" + ip + ":" + port + "/nodeactivate/request";
        LOG.debug("Connecting to " + activateURL);
        HttpsURLConnection connection;
        String params = "serverhostname=" + sethlansNode.getHostname() + "&ipAddress=" + sethlansNode.getIpAddress()
                + "&port=" + sethlansNode.getNetworkPort() + "&uuid=" + sethlansNode.getRequestUUID();

        try {
            URL url = new URL(activateURL);
            SSLUtilities.trustAllHostnames();
            SSLUtilities.trustAllHttpsCertificates();
            connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            connection.setRequestMethod("POST");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(params);
            wr.flush();
            wr.close();

            int response = connection.getResponseCode();
            LOG.debug("HTTP Response code " + response);


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendActivationResponse() {

    }
}
