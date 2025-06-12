package com.ioinnovate.infoorigin.code_executor.service;

import com.ioinnovate.infoorigin.code_executor.dto.ExecutionResponse;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.SECONDS;

@Service
public class PythonExecutionService {

    private final ExecutorService executorService;

    public PythonExecutionService() {
        int poolSize = Runtime.getRuntime().availableProcessors(); // e.g., 4 threads for 4 cores
        this.executorService = Executors.newFixedThreadPool(poolSize);
    }

    public ExecutionResponse executePythonScript(String filePath) {
        Instant start = Instant.now();
        boolean success = false;
        String message = "Execution completed";
        String errors = null;
        String output = null;

        try {
            File pythonFile = new File(filePath);
            if (!pythonFile.exists()) {
                return new ExecutionResponse(false, "File not found", 0, "File does not exist at path: " + filePath);
            }

            ProcessBuilder processBuilder = new ProcessBuilder("python", filePath);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder outputBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                outputBuilder.append(line).append("\n");
            }
            output = outputBuilder.toString().trim();

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                success = output.equals("True");
                message = success
                        ? "Python script executed successfully with passing tests"
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

    public ExecutionResponse parallelExecutePythonScript(String filePath) {
        Callable<ExecutionResponse> task = () -> {
            Instant start = Instant.now();
            boolean success = false;
            String message = "Execution completed";
            String errors = null;
            String output = null;

            try {
                File pythonFile = new File(filePath);
                if (!pythonFile.exists()) {
                    return new ExecutionResponse(false, "File not found", 0, "File does not exist at path: " + filePath);
                }

                ProcessBuilder processBuilder = new ProcessBuilder("python", filePath);
                processBuilder.redirectErrorStream(true);

                Process process = processBuilder.start();

                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder outputBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    outputBuilder.append(line).append("\n");
                }
                output = outputBuilder.toString().trim();

                int exitCode = process.waitFor();

                if (exitCode == 0) {
                    success = output.equals("True");
                    message = success
                            ? "Python script executed successfully with passing tests"
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
        };

        try {
            Future<ExecutionResponse> future = executorService.submit(task);
            return future.get();
        } catch (Exception e) {
            return new ExecutionResponse(false, "Internal server error", 0, e.getMessage());
        }
    }

    public ExecutionResponse executePythonFile(byte[] pythonScriptBytes) {
        Instant start = Instant.now();
        boolean success = false;
        String message = "Execution completed";
        String errors = null;
        StringBuilder output = new StringBuilder();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python", "-");
            Process process = processBuilder.start();

            // Create InputStream from byte array
            InputStream scriptInputStream = new ByteArrayInputStream(pythonScriptBytes);

            // Write the byte array to process's stdin
            Thread inputThread = new Thread(() -> {
                try (OutputStream os = process.getOutputStream()) {
                    scriptInputStream.transferTo(os);
                } catch (IOException e) {
                    output.append("Error writing to process input: ").append(e.getMessage());
                }
            });

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

            inputThread.start();
            outputThread.start();
            errorThread.start();

            boolean finished = process.waitFor(30, SECONDS);
            if (!finished) {
                process.destroyForcibly();
                message = "Timeout exceeded";
                errors = "Execution took longer than 30 seconds";
            } else {
                inputThread.join(1000);
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

    @PreDestroy
    public void shutdown() {
        executorService.shutdown(); // Graceful shutdown
    }
}
