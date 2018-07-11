/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
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


    public static void settings() throws MalformedURLException {
        String ip = SethlansUtils.getIP();
        String port = SethlansUtils.getPort();
        URL url = new URL("https://" + ip + ":" + port + "/" + "settings");
        SethlansUtils.openWebpage(url);
    }
}
