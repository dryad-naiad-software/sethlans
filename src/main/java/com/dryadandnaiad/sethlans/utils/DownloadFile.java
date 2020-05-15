package com.dryadandnaiad.sethlans.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;

/**
 * Utility class taken from example by Baeldung located at:
 * <p>
 * https://github.com/eugenp/tutorials/tree/master/core-java-modules/core-java-networking-2
 * <p>
 * File File created by Mario Estrella on 4/26/2020.
 * Dryad and Naiad Software LLC
 * mestrella@dryadandnaiad.com
 * Project: sethlans
 */
@Slf4j
public class DownloadFile {

    public static long downloadFile(String downloadUrl, String saveAsFileName) throws IOException, URISyntaxException {
        log.info("Downloading " + saveAsFileName + " from " + downloadUrl);
        var outputFile = new File(saveAsFileName);
        var downloadFileConnection = new URI(downloadUrl).toURL()
                .openConnection();
        return transferDataAndGetBytesDownloaded(downloadFileConnection, outputFile);
    }

    private static long transferDataAndGetBytesDownloaded(URLConnection downloadFileConnection, File outputFile) throws IOException {
        long bytesDownloaded = 0;
        try (var is = downloadFileConnection.getInputStream();
             var os = new FileOutputStream(outputFile, true)) {

            var buffer = new byte[1024];

            int bytesCount;
            while ((bytesCount = is.read(buffer)) > 0) {
                os.write(buffer, 0, bytesCount);
                bytesDownloaded += bytesCount;
            }
        }
        log.info(outputFile.toString() + " Downloaded");
        return bytesDownloaded;
    }

    public static long downloadFileWithResume(String downloadUrl, String saveAsFileName) throws IOException,
            URISyntaxException {
        log.info("Downloading " + saveAsFileName + " from " + downloadUrl);
        var outputFile = new File(saveAsFileName);
        var downloadFileConnection = addFileResumeFunctionality(downloadUrl, outputFile);
        return transferDataAndGetBytesDownloaded(downloadFileConnection, outputFile);
    }

    private static URLConnection addFileResumeFunctionality(String downloadUrl, File outputFile) throws IOException,
            URISyntaxException {
        var existingFileSize = 0L;
        URLConnection downloadFileConnection = new URI(downloadUrl).toURL()
                .openConnection();

        if (outputFile.exists() && downloadFileConnection instanceof HttpURLConnection) {
            var httpFileConnection = (HttpURLConnection) downloadFileConnection;

            var tmpFileConn = (HttpURLConnection) new URI(downloadUrl).toURL()
                    .openConnection();
            tmpFileConn.setRequestMethod("HEAD");
            var fileLength = tmpFileConn.getContentLengthLong();
            existingFileSize = outputFile.length();

            if (existingFileSize < fileLength) {
                httpFileConnection.setRequestProperty("Range", "bytes=" + existingFileSize + "-" + fileLength);
            } else {
                throw new IOException("File Download already completed.");
            }
        }
        return downloadFileConnection;
    }
}
