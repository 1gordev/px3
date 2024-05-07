package com.id.px3.utils.proc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

@Service
@EnableAsync
@Slf4j
public class ProcessUtils {

    @Async
    public CompletableFuture<Boolean> run(String workingFolder, String ...commandAndParams) {
        ProcessBuilder processBuilder = new ProcessBuilder(commandAndParams);
        processBuilder.directory(new File(workingFolder));

        try {
            Process process = processBuilder.start();

            //  Read the output stream to ensure all output is captured
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug(line);
                }
            }

            //  Wait for the process to terminate and check the exit value
            int exitCode = process.waitFor();
            return CompletableFuture.completedFuture(exitCode == 0);
        } catch (IOException e) {
            log.error("Error executing Docker Compose: %s".formatted(e.getMessage()));
        } catch (InterruptedException e) {
            log.error("Process was interrupted: %s".formatted(e.getMessage()));
            Thread.currentThread().interrupt();
        }
        return CompletableFuture.completedFuture(false);
    }
}
