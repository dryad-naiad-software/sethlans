/*
 * Copyright (c) 2017. Dryad and Naiad Software LLC.
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

package com.dryadandnaiad.sethlans.application;

import com.dryadandnaiad.sethlans.enums.StringKey;
import com.dryadandnaiad.sethlans.ui.SystemTrayIconListener;
import com.dryadandnaiad.sethlans.ui.SystemTrayIconMenu;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.*;

/**
 * Created Mario Estrella on 3/9/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@SpringBootApplication
public class Sethlans {
    private static final Logger LOG = LoggerFactory.getLogger(Sethlans.class);
    private static SystemTrayIconMenu sysTrayMenu;

    public static void main(String[] args) {

        // TODO System Tray is going to need some work. right now it opens two instances.
        if (SystemTray.isSupported()) {
            sysTrayMenu = SystemTrayIconMenu.getInstance();
            sysTrayMenu.setMenuListener(new SystemTrayIconListener() {
                @Override
                public void aboutDialogRequested() {
                    LOG.debug("About dialog selected");
                }
                @Override
                public void startServerEventRequested() {
                    LOG.debug("Starting tomcat server");
                }
                @Override
                public void stopServerEventRequested() {
                    LOG.debug("Stopping tomcat server");
                }
                @Override
                public void exitEventOccurred() {
                    stopServerEventRequested();
                    LOG.info("********************* " + SethlansUtils.getString(StringKey.APP_NAME) + " Shutdown" + " *********************");
                    System.exit(0);
                }
            });
        }

            SpringApplication.run(Sethlans.class, args);


    }
}

