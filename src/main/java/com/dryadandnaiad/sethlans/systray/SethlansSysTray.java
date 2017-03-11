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

import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.awt.*;

/**
 * Created Mario Estrella on 3/10/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@org.springframework.stereotype.Component
public class SethlansSysTray extends TrayIcon {
    private static final Logger LOG = LoggerFactory.getLogger(SethlansSysTray.class);

    private static final String IMAGE = "images/sethlans_systray.png";
    private static final String TOOLTIP = "Text";

    private MenuItem exitItem;
    private MenuItem aboutItem;

    private PopupMenu popup;
    private SystemTray tray;

    public SethlansSysTray() {
        super(SethlansUtils.createImage(IMAGE, TOOLTIP), TOOLTIP);
        LOG.debug("Test");
        popup = new PopupMenu();
        menuItems();
        tray = SystemTray.getSystemTray();
    }

    @PostConstruct
    private void setup() throws AWTException {
        setPopupMenu(popup);
        tray.add(this);
    }

    private void menuItems() {
        exitItem = new MenuItem("Exit");
        aboutItem = new MenuItem("About Sethlans");
        exitItem.addActionListener(e -> {
            LOG.debug("Shutdown Initiated from System Tray");
            System.exit(0);
        });


        popup.add(aboutItem);
        popup.add(exitItem);
    }


}