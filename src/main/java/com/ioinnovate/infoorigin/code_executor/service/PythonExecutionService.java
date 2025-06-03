package com.ioinnovate.infoorigin.code_executor.service;

import com.ioinnovate.infoorigin.code_executor.dto.ExecutionResponse;
import org.springframework.stereotype.Service;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.Duration;
import java.time.Instant;

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
}