package com.ioinnovate.infoorigin.code_executor.service;

import com.ioinnovate.infoorigin.code_executor.dto.ExecutionResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class PythonExecutionService {

    public ExecutionResponse executePythonScript(String filePath) {
        Instant start = Instant.now();
        boolean success = false;
        String message = "Execution completed";
        String errors = null;
        String output = null;

        try {
            // Validate file exists
            File pythonFile = new File(filePath);
            if (!pythonFile.exists()) {
                return new ExecutionResponse(false, "File not found", 0, "File does not exist at path: " + filePath);
            }

            // Build the process
            ProcessBuilder processBuilder = new ProcessBuilder("python", filePath);
            processBuilder.redirectErrorStream(true); // merge error and output streams

            // Start the process
            Process process = processBuilder.start();

            // Read the output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder outputBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                outputBuilder.append(line).append("\n");
            }
            output = outputBuilder.toString().trim();

            // Wait for process to complete and get exit code
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                // Check if the Python tests actually passed
                success = output.equals("True");
                message = success ? "Python script executed successfully with passing tests"
                        : "Python script executed but tests failed";
                if (!success) {
                    errors = output;
                }
            } else {
                message = "Python script execution failed";
                errors = output;
            }

        } catch (Exception e) {
            message = "Error during execution";
            errors = e.getMessage() + (output != null ? "\nOutput:\n" + output : "");
        }

        long executionTime = Duration.between(start, Instant.now()).toMillis();
        return new ExecutionResponse(success, message, executionTime, errors);
    }

    public ExecutionResponse executePythonFile(MultipartFile file) {
        Instant start = Instant.now();
        boolean success = false;
        String message = "Execution completed";
        String errors = null;
        StringBuilder output = new StringBuilder();

        try {
            // Create temp file
            File pythonFile = File.createTempFile("script_", ".py");
            file.transferTo(pythonFile);
            pythonFile.deleteOnExit();

            // Execute process
            ProcessBuilder processBuilder = new ProcessBuilder("python", pythonFile.getAbsolutePath());
            Process process = processBuilder.start();

            // Read output streams in separate threads to prevent deadlock
            Thread outputThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (IOException e) {
                    output.append("Error reading output: ").append(e.getMessage());
                }
            });

            Thread errorThread = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                } catch (IOException e) {
                    output.append("Error reading errors: ").append(e.getMessage());
                }
            });

            outputThread.start();
            errorThread.start();

            // Wait with timeout
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                message = "Timeout exceeded";
                errors = "Execution took longer than 30 seconds";
            } else {
                outputThread.join(1000);
                errorThread.join(1000);

                int exitCode = process.exitValue();
                success = exitCode == 0;
                message = success ? "Success" : "Failed";
                errors = success ? null : output.toString();
            }

        } catch (Exception e) {
            message = "Error during execution";
            errors = e.getMessage() + "\n" + output.toString();
        }

        long executionTime = Duration.between(start, Instant.now()).toMillis();
        return new ExecutionResponse(success, message, executionTime, errors);
    }
}