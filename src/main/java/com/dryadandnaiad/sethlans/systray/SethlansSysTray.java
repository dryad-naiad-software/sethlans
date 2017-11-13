package com.dryadandnaiad.sethlans.systray;

import com.dryadandnaiad.sethlans.utils.SethlansUtils;

import java.awt.*;
import java.net.MalformedURLException;

/**
 * Created Mario Estrella on 11/12/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class SethlansSysTray extends TrayIcon {
    private static final String IMAGE = "images/sethlans_systray.png";
    private static final String TOOLTIP = "Sethlans";

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
        exitItem = new MenuItem("Exit Sethlans");
        openBrowser = new MenuItem("Show Sethlans");
        openBrowser.addActionListener(e -> {
            try {
                OpenBrowser.start();
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            }
        });

        exitItem.addActionListener(e -> {
            System.exit(0);
        });
        popup.add(openBrowser);
        popup.add(exitItem);
    }

}
