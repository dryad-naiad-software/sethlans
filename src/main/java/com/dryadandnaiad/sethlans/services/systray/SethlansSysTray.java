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

import com.dryadandnaiad.sethlans.enums.SethlansMode;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

import static com.dryadandnaiad.sethlans.utils.SethlansFileUtils.createImage;

/**
 * Created Mario Estrella on 11/12/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
class SethlansSysTray extends TrayIcon {
    private static final String IMAGE = "images/sethlans_systray.png";
    private static final String IMAGE_ACTIVE = "images/systray_active.png";
    private static final String TOOLTIP = "Sethlans";
    private static final Logger LOG = LoggerFactory.getLogger(SethlansSysTray.class);
    private PopupMenu popup;
    private SystemTray tray;
    private SethlansMode mode;
    SethlansSysTray() {
        super(createImage(IMAGE, TOOLTIP), TOOLTIP);
    }

    void setup(SethlansMode mode) {
        this.mode = mode;
        LOG.debug("Starting Sethlans System Tray");
        popup = new PopupMenu();
        menuItems();
        tray = SystemTray.getSystemTray();
        this.setImageAutoSize(true);
        setPopupMenu(popup);
        try {
            tray.add(this);
        } catch (AWTException e) {
            LOG.error(Throwables.getStackTraceAsString(e));
        }
    }

    void remove() {
        LOG.debug("Stopping Sethlans System Tray");
        tray.remove(this);

    }

    void changeIcon() {

    }

    private void menuItems() {
        MenuItem openBrowser = new MenuItem("Open Sethlans");
        openBrowser.addActionListener(e -> OpenBrowser.openSethlansLink("/"));
        popup.add(openBrowser);
        if (mode != SethlansMode.SETUP) {
            if (mode != SethlansMode.NODE) {
                Menu projects = new Menu("Projects");
                MenuItem addProjects = new MenuItem("Add New Project");
                addProjects.addActionListener(e -> OpenBrowser.openSethlansLink("/projects/add"));
                projects.add(addProjects);
                MenuItem displayProjects = new MenuItem("Project List");
                displayProjects.addActionListener(e -> OpenBrowser.openSethlansLink("/projects"));
                projects.add(displayProjects);
                popup.add(projects);
            }
            Menu administration = new Menu("Administration");
            MenuItem logs = new MenuItem("Logs");
            logs.addActionListener(e -> OpenBrowser.openSethlansLink("/admin/logs"));
            administration.add(logs);
            MenuItem sethlansConfig = new MenuItem("Settings");
            sethlansConfig.addActionListener(e -> OpenBrowser.openSethlansLink("/admin/sethlans_settings"));
            administration.add(sethlansConfig);
            if (mode != SethlansMode.NODE) {
                Menu servers = new Menu("Server");
                MenuItem displayNodePage = new MenuItem("Node Administration");
                displayNodePage.addActionListener(e -> OpenBrowser.openSethlansLink("/admin/nodes"));
                servers.add(displayNodePage);
                MenuItem blenderVersion = new MenuItem("Configure Blender Versions");
                blenderVersion.addActionListener(e -> OpenBrowser.openSethlansLink("/admin/blender_version_admin"));
                servers.add(blenderVersion);
                administration.add(servers);
            }
            if (mode != SethlansMode.SERVER) {
                Menu nodes = new Menu("Node");
                MenuItem configureNode = new MenuItem("Change Compute Settings");
                configureNode.addActionListener(e -> OpenBrowser.openSethlansLink("/admin/compute_settings"));
                nodes.add(configureNode);
                MenuItem displayServerPage = new MenuItem("Authorize Server(s)/Add Access Key");
                displayServerPage.addActionListener(e -> OpenBrowser.openSethlansLink("/admin/servers"));
                nodes.add(displayServerPage);
                administration.add(nodes);
            }
            popup.add(administration);
            MenuItem displayHelp = new MenuItem("Help");
            displayHelp.addActionListener(e -> OpenBrowser.openHelp());
            popup.add(displayHelp);

        }


        MenuItem exitItem = new MenuItem("Exit");


        exitItem.addActionListener(e -> System.exit(0));
        popup.add(exitItem);
    }

}
