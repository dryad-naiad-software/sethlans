/*
 * Copyright (c) 2017 Dryad and Naiad Software LLC.
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

import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;

/**
 * Created Mario Estrella on 11/24/17.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
public class SevenZipUtil {
    private static final Logger LOG = LoggerFactory.getLogger(SevenZipUtil.class);

    private final String archiveName;

    public SevenZipUtil(String archive) {
        this.archiveName = archive;
    }

    public void extract() throws FileNotFoundException, SevenZipException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(archiveName, "r");

        IInArchive inArchive = SevenZip.openInArchive(null, // Choose format automatically
                new RandomAccessFileInStream(randomAccessFile));
        // Getting simple interface of the archive inArchive
        ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();
        LOG.debug(archiveName);

        System.out.println("   Hash   |    Size    | Filename");
        System.out.println("----------+------------+---------");


        for (final ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
            final int[] hash = new int[]{0};
            if (!item.isFolder()) {
                ExtractOperationResult result;

                final long[] sizeArray = new long[1];
                result = item.extractSlow(data -> {

                    //Write to file
                    FileOutputStream fos;
                    try {
                        File file = new File(item.getPath());
                        file.getParentFile().mkdirs();
                        fos = new FileOutputStream(file);
                        fos.write(data);
                        fos.close();

                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    hash[0] ^= Arrays.hashCode(data); // Consume data
                    sizeArray[0] += data.length;
                    return data.length; // Return amount of consumed data
                });
                if (result == ExtractOperationResult.OK) {
                    System.out.println(String.format("%9X | %10s | %s", //
                            hash[0], sizeArray[0], item.getPath()));
                } else {
                    System.err.println("Error extracting item: " + result);
                }
            }
        }

    }
}
