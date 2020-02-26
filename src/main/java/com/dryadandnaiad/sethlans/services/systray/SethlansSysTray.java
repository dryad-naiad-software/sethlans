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
    private Image activeImage = createImage(IMAGE_ACTIVE, "Sethlans");
    private Image defaultImage = createImage(IMAGE, "Sethlans");

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
        menuConfig();
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

    void nodeActive() {
        setImage(activeImage);
    }

    void nodeInactive() {
        setImage(defaultImage);
    }

    void updateToolTip(String tooltip) {
        setToolTip(tooltip);
    }

    private void menuConfig() {
        MenuItem openBrowser = new MenuItem("Open Sethlans");
        openBrowser.addActionListener(e -> OpenBrowser.openSethlansLink("/"));
        popup.add(openBrowser);

        if (mode != SethlansMode.SETUP) {
            Menu administration = new Menu("Administration");
            Menu tools = new Menu("Tools");
            MenuItem sethlansConfig = new MenuItem("Settings");
            sethlansConfig.addActionListener(e -> OpenBrowser.openSethlansLink("/admin/sethlans_settings"));
            administration.add(sethlansConfig);
            if (mode != SethlansMode.NODE) {
                Menu projects = new Menu("Projects");
                MenuItem addProjects = new MenuItem("Add New Project");
                MenuItem displayProjects = new MenuItem("Project List");
                addProjects.addActionListener(e -> OpenBrowser.openSethlansLink("/projects/add"));
                displayProjects.addActionListener(e -> OpenBrowser.openSethlansLink("/projects"));

                projects.add(addProjects);
                projects.add(displayProjects);
                popup.add(projects);

                MenuItem addNode = new MenuItem("Add Node(s)");
                MenuItem displayNodePage = new MenuItem("Manage Nodes");
                MenuItem blenderVersion = new MenuItem("Configure Blender Versions");
                displayNodePage.addActionListener(e -> OpenBrowser.openSethlansLink("/admin/nodes"));
                addNode.addActionListener(e -> OpenBrowser.openSethlansLink("/admin/nodes/add"));
                blenderVersion.addActionListener(e -> OpenBrowser.openSethlansLink("/admin/blender_version_admin"));

                MenuItem nodeRenderHistory = new MenuItem("Node Render History");
                nodeRenderHistory.addActionListener(e -> OpenBrowser.openSethlansLink("/admin/render_history"));

                tools.add(nodeRenderHistory);
                administration.add(addNode);
                administration.add(displayNodePage);
                administration.add(blenderVersion);
            }
            if (mode != SethlansMode.SERVER) {
                MenuItem configureNode = new MenuItem("Change Compute Settings");
                MenuItem displayServerPage = new MenuItem("Authorize Server / Add Access Key");

                configureNode.addActionListener(e -> OpenBrowser.openSethlansLink("/admin/compute_settings"));
                displayServerPage.addActionListener(e -> OpenBrowser.openSethlansLink("/admin/servers"));

                administration.add(configureNode);
                administration.add(displayServerPage);
            }

            MenuItem logs = new MenuItem("Logs");
            MenuItem displayHelp = new MenuItem("Help");

            logs.addActionListener(e -> OpenBrowser.openSethlansLink("/admin/logs"));
            displayHelp.addActionListener(e -> OpenBrowser.openHelp());

            tools.add(logs);
            popup.add(administration);
            popup.add(tools);
            popup.add(displayHelp);
        }

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        popup.add(exitItem);
    }

}
