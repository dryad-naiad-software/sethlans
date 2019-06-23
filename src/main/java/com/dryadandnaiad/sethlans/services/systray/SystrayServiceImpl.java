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
import com.dryadandnaiad.sethlans.utils.SethlansQueryUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.awt.*;

/**
 * Created Mario Estrella on 3/25/2019.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Service
public class SystrayServiceImpl implements SystrayService {
    private SethlansSysTray sysTray;
    @Value("${sethlans.firsttime}")
    private boolean firstTime;

    @Async
    @Override
    public void start() {
        if (!GraphicsEnvironment.isHeadless()) {
            SethlansMode mode = null;
            if (firstTime) {
                mode = SethlansMode.SETUP;
            } else {
                mode = SethlansQueryUtils.getMode();
            }
            sysTray = new SethlansSysTray();
            sysTray.setup(mode);
        }
    }

    @Override
    public void nodeState(boolean active) {
        if (!GraphicsEnvironment.isHeadless()) {
            if (SethlansQueryUtils.getMode() != SethlansMode.SERVER) {
                if (active) {
                    sysTray.nodeActive();
                    sysTray.setToolTip("Sethlans - Rendering");
                } else {
                    sysTray.nodeInactive();
                    sysTray.setToolTip("Sethlans");
                }
            }
        }
    }

    public void stop() {
        if (!GraphicsEnvironment.isHeadless()) {
            sysTray.remove();
        }

    }

}
