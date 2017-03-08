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

package com.dryadandnaiad.sethlans.webui.systray;

/**
 * Created Mario Estrella on 3/6/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */

import com.dryadandnaiad.sethlans.enums.StringKey;
import com.dryadandnaiad.sethlans.utils.SethlansUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class SystemTrayIconMenu implements ActionListener {
    private SystemTray sysTray;
    private TrayIcon trayIcon;
    private PopupMenu popupMenu;
    private Image sethlansImage;
    private MenuItem exitItem;
    private MenuItem aboutItem;
    private MenuItem startTomcatItem;
    private MenuItem stopTomcatItem;
    private SystemTrayIconListener systemListener;
    private static final Logger LOG = LogManager.getLogger(SystemTrayIconMenu.class);

    public SystemTrayIconMenu() {
        sethlansImage = SethlansUtils.createIcon(SethlansUtils.getString(StringKey.APP_SYSTRAY_IMAGE)).getImage();
        trayIcon = new TrayIcon(sethlansImage, SethlansUtils.getString(StringKey.APP_NAME));
        sysTray = SystemTray.getSystemTray();
        exitItem = new MenuItem("Exit " + SethlansUtils.getString(StringKey.APP_NAME));
        aboutItem = new MenuItem("About " + SethlansUtils.getString(StringKey.APP_NAME));
        startTomcatItem = new MenuItem("Start Sethlans");
        stopTomcatItem = new MenuItem("Stop Sethlans");
        aboutItem.addActionListener(this);
        exitItem.addActionListener(this);
        startTomcatItem.addActionListener(this);
        stopTomcatItem.addActionListener(this);

    }

    public void setMenuListener(SystemTrayIconListener listener) {
        this.systemListener = listener;
    }

    public void refresh() {

        sysTray.remove(trayIcon);
        trayIcon.setPopupMenu(null);

        try {
            sysTray.add(trayIcon);
            trayIcon.setImageAutoSize(true);
            popupMenu = new PopupMenu();

            // TODO checks need to be implemented here in order to disable the appropriate option i.e.
            // TODO Sethlans Start should be disabled if server is running.
            popupMenu.add(startTomcatItem);
            popupMenu.add(stopTomcatItem);
            popupMenu.add(aboutItem);
            popupMenu.add(exitItem);


            trayIcon.setPopupMenu(popupMenu);


        } catch (AWTException e) {
            LOG.error("Exception occurred during systray menu setup" + e.getMessage());
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        MenuItem selected = (MenuItem) e.getSource();
        if (selected == startTomcatItem) {
            if (systemListener != null) {
                systemListener.startServerEventRequested();
            }
        }

        if (selected == stopTomcatItem) {
            if (systemListener != null) {
                systemListener.stopServerEventRequested();
            }
        }


        if (selected == aboutItem) {
            if (systemListener != null) {
                systemListener.aboutDialogRequested();
            }
        }

        if (selected == exitItem) {
            if (systemListener != null) {
                systemListener.exitEventOccurred();
            }
        }

    }

}