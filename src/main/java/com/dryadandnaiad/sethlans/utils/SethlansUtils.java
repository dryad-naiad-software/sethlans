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

package com.dryadandnaiad.sethlans.utils;

import com.dryadandnaiad.sethlans.enums.GitPropertyKey;
import com.dryadandnaiad.sethlans.enums.StringKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Properties;

/**
 * Created Mario Estrella on 3/9/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class SethlansUtils {
    private static final Logger LOG = LoggerFactory.getLogger(SethlansUtils.class);

    public static String getString(StringKey sentEnum) {
        String newKey = sentEnum.toString();
        String value = null;
        try {
            Properties properties;
            InputStream input = new Resources("properties/strings.xml").getResource();
            properties = new Properties();
            properties.loadFromXML(input);

            value = properties.getProperty(newKey);

        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage());

        } catch (IOException e) {
            LOG.error(e.getMessage());

        }
        return value;
    }

    public static String getGitProperties(GitPropertyKey sentEnum) {
        return null;
    }

    public static String updaterTimeStamp(){
        Date currentDate = GregorianCalendar.getInstance().getTime();
        return String.format("Updated: %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", currentDate);
    }

    public static String enumToString(Enum value){
        return value.toString().toLowerCase();
    }
    public static URL setURL(String fileName) throws FileNotFoundException {
        URL url = SethlansUtils.class.getResource("/static/images/" + fileName);
        if (url == null) {
            throw new FileNotFoundException();
        }
        return url;
    }
    public static ImageIcon createIcon(String iconName) {
        URL url = null;
        try {
            url = setURL(iconName);
        } catch (FileNotFoundException | NullPointerException e) {
            LOG.error("Image File not found " + url + e.getMessage());
        }
        ImageIcon imageIcon = new ImageIcon(url);
        return imageIcon;
    }
    public static void openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
            } catch (Exception e) {
                LOG.error("Unable to Open Web page" + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    public static void openWebpage(URL url) {
        try {
            openWebpage(url.toURI());
        } catch (URISyntaxException e) {
            LOG.error("Unable to Open Web page" + e.getMessage());
        }
    }
}
