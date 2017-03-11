/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC
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

import com.dryadandnaiad.sethlans.utils.OpenBrowser;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.MalformedURLException;

/**
 * Created Mario Estrella on 3/10/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class SethlansSysTray extends TrayIcon {
    private static final Logger LOG = LoggerFactory.getLogger(SethlansSysTray.class);

    private static final String IMAGE = "images/sethlans_systray.png";
    private static final String TOOLTIP = "Text";

    private MenuItem openBrowser;
    private MenuItem exitItem;
    private MenuItem aboutItem;

    private PopupMenu popup;
    private SystemTray tray;

    public SethlansSysTray() {
        super(SethlansUtils.createImage(IMAGE, TOOLTIP), TOOLTIP);
        popup = new PopupMenu();
        menuItems();
        tray = SystemTray.getSystemTray();
    }

    public void setup() {
        setPopupMenu(popup);
        try {
            tray.add(this);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    private void menuItems() {
        openBrowser = new MenuItem("Show Sethlans");
        exitItem = new MenuItem("Exit");
        aboutItem = new MenuItem("About Sethlans");
        openBrowser.addActionListener(e -> {
            LOG.debug("Displaying Homepage");
            try {
                OpenBrowser.start();
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }
        });

        exitItem.addActionListener(e -> {
            LOG.debug("Shutdown Initiated from System Tray");
            System.exit(0);
        });

        popup.add(openBrowser);
        popup.add(aboutItem);
        popup.add(exitItem);
    }


}