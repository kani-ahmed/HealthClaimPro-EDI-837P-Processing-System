package com.billing.webapp;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZipUtil {
    /*
        * This class is used to zip files into a single zip file
        * It uses the java.util.zip package
     */
    private static final Logger loggerr = LoggerFactory.getLogger(LegacyController.class);

    public static byte[] zipFiles(List<String> filePaths) throws IOException {
        // Create a ByteArrayOutputStream to hold the zip file
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             // Create a ZipOutputStream to write to the ByteArrayOutputStream
             ZipOutputStream zos = new ZipOutputStream(baos)) {

            // Loop through the list of files
            for (String filePath : filePaths) {
                // Create a File object for the file to be zipped
                File file = new File(filePath);
                // Check if the file exists
                if (file.exists()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        // Create a ZipEntry for the file
                        ZipEntry zipEntry = new ZipEntry(file.getName());
                        // Add the ZipEntry to the ZipOutputStream
                        zos.putNextEntry(zipEntry);

                        // Read the file and write it to the ZipOutputStream in 1024 byte chunks to avoid memory issues with large files
                        byte[] bytes = new byte[1024];
                        int length;
                        while ((length = fis.read(bytes)) >= 0) {
                            zos.write(bytes, 0, length);
                        }
                        // Close the ZipEntry
                        zos.closeEntry();
                    } catch (IOException e) {
                        // Log and/or handle the exception for a specific file
                        loggerr.error("Error reading or zipping file: " + filePath, e);
                    }
                } else {
                    // Log or handle the scenario when a file doesn't exist
                    loggerr.error("File does not exist: " + filePath);
                }
            }
            zos.finish();
            // Return the zip file as a byte array from the ByteArrayOutputStream by calling its toByteArray() method
            // The toByteArray() method returns a copy of the ByteArrayOutputStream's internal byte array which is the zip file we want to return
            return baos.toByteArray();
        }
    }
}