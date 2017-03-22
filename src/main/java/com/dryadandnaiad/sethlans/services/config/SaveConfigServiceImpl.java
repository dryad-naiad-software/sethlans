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

package com.dryadandnaiad.sethlans.services.config;

import com.dryadandnaiad.sethlans.services.interfaces.SaveConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created Mario Estrella on 3/18/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class SaveConfigServiceImpl implements SaveConfigService {
    private static final Logger LOG = LoggerFactory.getLogger(SaveConfigServiceImpl.class);

    @Override
    public boolean saveSethlansSettings() {
        return false;
    }

    //    private void setProperty(Enum key, Enum value) {
//        try {
//            Properties properties = new Properties();
//            try (FileInputStream fileIn = new FileInputStream(defaultConfigFile)) {
//                properties.loadFromXML(fileIn);
//            }
//            properties.setProperty(SethlansUtils.enumToString(key), SethlansUtils.enumToString(value));
//            try (FileOutputStream fileOut = new FileOutputStream(defaultConfigFile)) {
//                properties.storeToXML(fileOut, SethlansUtils.updaterTimeStamp());
//            }
//        } catch (FileNotFoundException e) {
//            LOG.error(e.getMessage());
//        } catch (IOException e) {
//            LOG.error(e.getMessage());
//        }
//    }
//    private void setProperty(Enum key, String value) {
//        try {
//            Properties properties = new Properties();
//            try (FileInputStream fileIn = new FileInputStream(defaultConfigFile)) {
//                properties.loadFromXML(fileIn);
//            }
//            properties.setProperty(SethlansUtils.enumToString(key), value);
//            try (FileOutputStream fileOut = new FileOutputStream(defaultConfigFile)) {
//                properties.storeToXML(fileOut, SethlansUtils.updaterTimeStamp());
//            }
//        } catch (FileNotFoundException e) {
//            LOG.error(e.getMessage());
//        } catch (IOException e) {
//            LOG.error(e.getMessage());
//        }
//    }
//    private String getProperty(Enum key) {
//        try {
//            Properties properties = new Properties();
//            try (FileInputStream fileIn = new FileInputStream(defaultConfigFile)) {
//                properties.loadFromXML(fileIn);
//            }
//            return properties.getProperty(SethlansUtils.enumToString(key));
//        } catch (FileNotFoundException e) {
//            LOG.error(e.getMessage());
//        } catch (IOException e) {
//            LOG.error(e.getMessage());
//            System.exit(1);
//        }
//        return null;
//    }
}
