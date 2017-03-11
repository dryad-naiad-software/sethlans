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

package com.dryadandnaiad.sethlans.utils;

import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.BooleanOptionHandler;
import org.kohsuke.args4j.spi.Setter;

/**
 * @author Rolf Wendolsky
 *         http://stackoverflow.com/questions/30288934/remove-default-value-in-printusage-method-args4j-library
 */
public class HelpOptionHandler extends BooleanOptionHandler {
    public HelpOptionHandler(CmdLineParser parser, OptionDef option, Setter<Boolean> setter) {
        super(parser, option, setter);
    }

    public String printDefaultValue() {
        return null;  // this prevents the default value to be printed in usage info
    }
}