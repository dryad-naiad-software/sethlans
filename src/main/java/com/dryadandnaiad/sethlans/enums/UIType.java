/*
 * Copyright (C) 2017 Dryad and Naiad Software LLC
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
 */
package com.dryadandnaiad.sethlans.enums;

/**
 *
 * @author Mario Estrella <mestrella@dryadandnaiad.com>
 */
public enum UIType {
    CLI(Constants.CLI_VALUE),
    CLI_ONELINE(Constants.CLI_ONELINE_VALUE),
    GUI(Constants.GUI_VALUE); 
        
    UIType(String uiType) {
    }
    
    public static class Constants {
        public static final String CLI_VALUE = "CLI";
        public static final String CLI_ONELINE_VALUE = "CLI oneline";
        public static final String GUI_VALUE = "GUI";
    }   
}
