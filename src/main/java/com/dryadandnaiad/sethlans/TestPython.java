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

package com.dryadandnaiad.sethlans;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created Mario Estrella on 3/29/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class TestPython {

    public static void test(String pythonBin) {
        try {

            ProcessBuilder pb = new ProcessBuilder(pythonBin, "/Users/mestrella/.sethlans/bin/python/bin/test1.py");
            Process p = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String output = in.readLine();
            System.out.println("(Python)value is : " + output);
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
}

