/*
 * Copyright (c) 2020. Dryad and Naiad Software LLC.
 *   This program is free software; you can redistribute it and/or
 *   modify it under the terms of the GNU General Public License
 *   as published by the Free Software Foundation; either version 2
 *   of the License, or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package com.dryadandnaiad.sethlans.utils;

import org.apache.commons.lang3.SystemUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Mario Estrella on 4/19/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
class QueryUtilsTest {

//    @BeforeEach
//    void setUp() {
//    }
//
//    @AfterEach
//    void tearDown() {
//    }

    @Test
    void getShortUUID() {
        assertThat(QueryUtils.getShortUUID()).hasSize(13);
        assertThat(QueryUtils.getShortUUID()).contains("-");
    }

    @Test
    void getOS() {
        assertNotNull(QueryUtils.getOS());
        if (SystemUtils.IS_OS_WINDOWS) {
            assertThat(QueryUtils.getOS()).contains("Windows");
        }
        if (SystemUtils.IS_OS_LINUX) {
            assertThat(QueryUtils.getOS()).contains("Linux");
        }
        if (SystemUtils.IS_OS_MAC) {
            assertThat(QueryUtils.getOS()).contains("MacOS");
        }

    }
}
