/*
 * Copyright (c) 2018 Dryad and Naiad Software LLC.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
/**
 * Created Mario Estrella on 1/1/18.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class RandomCollection<E> {
    private final NavigableMap<Integer, E> map = new TreeMap<>();
    private final Random random;
    private static final Logger LOG = LoggerFactory.getLogger(RandomCollection.class);
    private int total = 0;

    public RandomCollection() {
        this(new Random());
    }

    private RandomCollection(Random random) {
        this.random = random;
    }

    public RandomCollection<E> add(int benchmark, E result) {
        map.put(benchmark, result);
        return this;
    }

    public E next() {
        int maximum = map.lastKey();
        int minimum = map.firstKey() + 1000;
        int value = random.nextInt((maximum - minimum) + 1) + minimum + total;
        total = value / 2;
        LOG.debug("Random Value :" + value);
        LOG.debug("Total :" + total);
        return map.lowerEntry(value).getValue();
    }

    @Override
    public String toString() {
        return "RandomCollection{" +
                "map=" + map +
                '}';
    }
}