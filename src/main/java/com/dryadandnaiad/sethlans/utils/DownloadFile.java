package com.dryadandnaiad.sethlans.utils;

import com.dryadandnaiad.sethlans.enums.ConfigKeys;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.HttpsURLConnection;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.*;

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

    public static boolean downloadFile(String downloadUrl, String saveAsFileName) throws IOException, URISyntaxException {
        log.info("Downloading " + saveAsFileName + " from " + downloadUrl);
        var outputFile = new File(saveAsFileName);
        var downloadFileConnection = new URI(downloadUrl).toURL()
                .openConnection();
        return transferDataAndGetBytesDownloaded(downloadFileConnection, outputFile);
    }

    private static boolean transferDataAndGetBytesDownloaded(URLConnection downloadFileConnection, File outputFile) throws IOException {
        try (var is = downloadFileConnection.getInputStream();
             var os = new FileOutputStream(outputFile, true)) {

            var buffer = new byte[1024];

            int bytesCount;
            while ((bytesCount = is.read(buffer)) > 0) {
                os.write(buffer, 0, bytesCount);
            }
        }
        log.info(outputFile.toString() + " Downloaded");
        return true;
    }

    public static boolean downloadFileWithResume(String downloadUrl, String saveAsFileName) throws IOException,
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

    public static File downloadFileBetweenSethlans(URL downloadURL, String saveAsFileName) {
        try {
            log.info("Downloading " + saveAsFileName + " from " + downloadURL);

            var outputFile = new File(saveAsFileName);

            var existingFileSize = 0L;
            var downloadFileConnection = (HttpsURLConnection) downloadURL.openConnection();
            if (Boolean.parseBoolean(ConfigUtils.getProperty(ConfigKeys.USE_SETHLANS_CERT))) {
                downloadFileConnection.setSSLSocketFactory(SSLUtilities.buildSSLSocketFactory());
                downloadFileConnection.setHostnameVerifier(SSLUtilities.allHostsValid());
            }


            if (outputFile.exists() && outputFile.length() > 0) {

                var tmpFileConn = (HttpsURLConnection) downloadURL.openConnection();
                if (Boolean.parseBoolean(ConfigUtils.getProperty(ConfigKeys.USE_SETHLANS_CERT))) {
                    tmpFileConn.setSSLSocketFactory(SSLUtilities.buildSSLSocketFactory());
                    tmpFileConn.setHostnameVerifier(SSLUtilities.allHostsValid());
                }

                tmpFileConn.setRequestMethod("HEAD");
                var fileLength = tmpFileConn.getContentLengthLong();
                existingFileSize = outputFile.length();

                if (existingFileSize < fileLength) {
                    downloadFileConnection.setRequestProperty("Range", "bytes=" + existingFileSize + "-" + fileLength);
                } else {
                    throw new IOException("File Download already completed.");
                }
            }

            var is = downloadFileConnection.getInputStream();
            var os = new FileOutputStream(outputFile, true);

            var buffer = new byte[1024];

            int bytesCount;
            while ((bytesCount = is.read(buffer)) > 0) {
                os.write(buffer, 0, bytesCount);
            }

            if (outputFile.exists() && outputFile.length() > 0) {
                log.info(outputFile + " Downloaded");
                return outputFile;
            } else {
                log.error(outputFile + " failed to download.");
                return null;
            }

        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(Throwables.getStackTraceAsString(e));
        }
        return null;
    }

}
