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

package com.dryadandnaiad.sethlans.services.network;

import com.dryadandnaiad.sethlans.domains.python.PythonDownloadFile;
import org.apache.commons.lang3.SystemUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created Mario Estrella on 9/8/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class PythonDownloadServiceTest {
    PythonDownloadService pythonDownloadService;
    PythonDownloadFile pythonDownloadFile;

    @Before
    public void setUp() throws Exception {
        pythonDownloadService = new PythonDownloadServiceImpl();
        pythonDownloadFile = pythonDownloadService.getPythonDownloadFile();
    }

    @Test
    public void test_python_download_not_null() {
        Assert.assertNotNull(pythonDownloadFile);
    }

    @Test
    public void test_python_mac_file() {
        if (SystemUtils.IS_OS_MAC) {
            Assert.assertThat(pythonDownloadFile.getFilename(), CoreMatchers.containsString("mac"));

        } else {
            System.out.println("System is not running macOS, skipping Assert in test_python_mac_file");
        }

    }

    @Test
    public void test_python_windows_file() {
        if (SystemUtils.IS_OS_WINDOWS) {
            String arch = System.getenv("PROCESSOR_ARCHITECTURE");
            String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
            String realArch = arch.endsWith("64") || wow64Arch != null && wow64Arch.endsWith("64")
                    ? "64" : "32";
            if (realArch.equals("64")) {
                Assert.assertThat(pythonDownloadFile.getFilename(), CoreMatchers.containsString("windows64"));

            } else {
                Assert.assertThat(pythonDownloadFile.getFilename(), CoreMatchers.containsString("windows32"));
            }

        } else {
            System.out.println("System is not running Windows, skipping Assert in test_python_windows_file");
        }
    }

    @Test
    public void test_python_linux_file() {
        if (SystemUtils.IS_OS_LINUX) {
            String arch = System.getProperty("os.arch");
            if (arch.equals("x86")) {
                Assert.assertThat(pythonDownloadFile.getFilename(), CoreMatchers.containsString("linux32"));
            } else {
                Assert.assertThat(pythonDownloadFile.getFilename(), CoreMatchers.containsString("linux64"));
            }
        } else {
            System.out.println("System is not running Linux, skipping Assert in test_python_linux_file");
        }

    }

}