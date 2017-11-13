package com.dryadandnaiad.sethlans.systray;

import com.dryadandnaiad.sethlans.utils.SethlansUtils;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created Mario Estrella on 11/12/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class OpenBrowser {
    public static void start() throws MalformedURLException {
        String ip = SethlansUtils.getIP();
        String port = SethlansUtils.getPort();
        URL url = new URL("https://" + ip + ":" + port + "/");
        SethlansUtils.openWebpage(url);

    }
}
