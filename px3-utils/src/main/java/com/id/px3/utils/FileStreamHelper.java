package com.id.px3.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileStreamHelper {

    /**
     * Opens a file or resource from the filesystem or the classpath.
     *
     * @param path The path of the file or resource to open.
     * @return The InputStream of the file or resource.
     * @throws IOException
     */
    public static InputStream openFileOrResource(String path) throws IOException {
        //  try to open the file from the filesystem
        Path filePath = Paths.get(path);
        if (Files.exists(filePath)) {
            return Files.newInputStream(filePath);
        }

        //  if the file doesn't exist on the filesystem, try to open it from the classpath resources
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream resourceStream = classLoader.getResourceAsStream(path.substring("classpath:".length()));

        if (resourceStream != null) {
            return resourceStream;
        }

        //  if the file doesn't exist in both locations, throw an exception
        throw new IOException("File not found: " + path);
    }


    /**
     * Saves an InputStream to a file.
     *
     * @param inputStream The InputStream to save.
     * @param filePath    The path of the file where to save the InputStream.
     * @param createDirs  If true, creates the directories of the file if they don't exist.
     * @throws IOException If an I/O error occurs.
     */
    public static void saveInputStreamToFile(InputStream inputStream, String filePath, boolean createDirs) throws IOException {
        if (createDirs) {
            Files.createDirectories(Paths.get(filePath).getParent());
        }

        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(filePath))) {
            byte[] buffer = new byte[1024 * 1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}
