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

import org.junit.Assert;
import org.junit.Test;

/**
 * Created Mario Estrella on 8/28/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class BlenderUtilsTest {

    @Test
    public void test_binaries_list_not_null() {
        Assert.assertNotNull(BlenderUtils.listBinaries());

    }

    @Test
    public void test_version_list_not_null() {
        Assert.assertNotNull(BlenderUtils.listVersions());
    }

    @Test
    public void test_refresh() {
        BlenderUtils.refresh();
        Assert.assertNotNull(BlenderUtils.listVersions());
        Assert.assertNotNull(BlenderUtils.listBinaries());
    }

}