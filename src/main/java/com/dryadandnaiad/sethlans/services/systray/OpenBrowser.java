/*
 * Copyright (c) 2019 Dryad and Naiad Software LLC
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

package com.dryadandnaiad.sethlans.services.systray;

import com.dryadandnaiad.sethlans.utils.SethlansQueryUtils;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created Mario Estrella on 11/12/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class OpenBrowser {
    private static final Logger LOG = LoggerFactory.getLogger(OpenBrowser.class);

    public static void start() throws MalformedURLException {
        String ip = SethlansQueryUtils.getIP();
        String port = SethlansQueryUtils.getPort();
        URL url = new URL("https://" + ip + ":" + port + "/");
        openWebpage(url);

    }

    public static void openWebpage(URL url) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(url.toURI());
            } catch (Exception e) {
                LOG.error("Unable to Open Web page" + e.getMessage());
                LOG.error(Throwables.getStackTraceAsString(e));
            }
        }
    }


    public static void settings() throws MalformedURLException {
        String ip = SethlansQueryUtils.getIP();
        String port = SethlansQueryUtils.getPort();
        URL url = new URL("https://" + ip + ":" + port + "/" + "settings");
        openWebpage(url);
    }
}
