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

package com.dryadandnaiad.sethlans.unit.comparators;

import com.dryadandnaiad.sethlans.comparators.AlphaNumericComparator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * File created by Mario Estrella on 4/26/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
class AlphaNumericComparatorTest {

    @Test
    void compare() {
        var testList = new ArrayList<String>();
        testList.add("Delta4");
        testList.add("Alpha12");
        testList.add("Charlie3");
        testList.add("Bravo77");
        testList.add("Echo2");
        testList.add("Bravo10");
        testList.add("Charlie1");
        testList.add("Alpha5");
        var sortedList = new ArrayList<>(testList);
        sortedList.sort(new AlphaNumericComparator());
        assertThat(sortedList.size()).isEqualTo(testList.size());
        assertThat(sortedList.get(0)).isEqualTo("Alpha5");
        assertThat(sortedList.get(7)).isEqualTo("Echo2");
        Collections.reverse(sortedList);
        assertThat(sortedList.get(7)).isEqualTo("Alpha5");
    }
}
