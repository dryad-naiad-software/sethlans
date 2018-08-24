/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC
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

import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.net.MalformedURLException;

import static com.dryadandnaiad.sethlans.utils.SethlansFileUtils.createImage;

/**
 * Created Mario Estrella on 11/12/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class SethlansSysTray extends TrayIcon implements Runnable {
    private static final String IMAGE = "images/sethlans_systray.png";
    private static final String TOOLTIP = "Sethlans";
    private static final Logger LOG = LoggerFactory.getLogger(SethlansSysTray.class);
    private PopupMenu popup;
    private SystemTray tray;

    public SethlansSysTray() {
        super(createImage(IMAGE, TOOLTIP), TOOLTIP);
    }

    private void setup() {
        setPopupMenu(popup);
        try {
            tray.add(this);
        } catch (AWTException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }
    }

    private void menuItems() {
        MenuItem exitItem = new MenuItem("Exit");
        MenuItem openBrowser = new MenuItem("Open Sethlans in browser");

        openBrowser.addActionListener(e -> {
            try {
                OpenBrowser.start();
            } catch (MalformedURLException e1) {
                LOG.error("Malformed URL" + e1.getMessage());
            }
        });

        exitItem.addActionListener(e -> {
            System.exit(0);
        });
        popup.add(openBrowser);
        popup.add(exitItem);
    }

    @Override
    public void run() {
        popup = new PopupMenu();
        menuItems();
        tray = SystemTray.getSystemTray();
        this.setImageAutoSize(true);
        setup();

    }
}
