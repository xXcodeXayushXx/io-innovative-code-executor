package com.ioinnovate.infoorigin.code_executor.service;

import com.ioinnovate.infoorigin.code_executor.dto.ExecutionResult;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

@Service
public class CodeExecutionService {

    private static final Logger logger = LoggerFactory.getLogger(CodeExecutionService.class);
    private static final int MAX_OUTPUT_LENGTH = 10000;
    private static final int MAX_INPUT_LENGTH = 5000;
    private static final int MAX_CODE_LENGTH = 100000;

    public ExecutionResult executeAndVerify(String code, List<String> inputs,
                                            List<String> expectedOutputs, int timeoutSeconds) {
        // Validate sizes
        if (code.length() > MAX_CODE_LENGTH) {
            return new ExecutionResult(false, "Code too large", null, -1, "Code exceeds maximum allowed size");
        }

        if (inputs != null) {
            for (String input : inputs) {
                if (input.length() > MAX_INPUT_LENGTH) {
                    return new ExecutionResult(false, "Input too large", null, -1, "Input exceeds maximum allowed size");
                }
            }
        }

        // Create temp file
        File pythonFile;
        try {
            pythonFile = File.createTempFile("exec_", ".py");
            pythonFile.deleteOnExit();

            // Write code to file with UTF-8 encoding
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(pythonFile), StandardCharsets.UTF_8))) {
                writer.write(code);
            }
        } catch (IOException e) {
            logger.error("File creation failed: {}", e.getMessage());
            return new ExecutionResult(false, "File creation failed", null, -1, e.getMessage());
        }

        // Prepare process
        ProcessBuilder processBuilder = new ProcessBuilder("python", pythonFile.getAbsolutePath());
        processBuilder.redirectErrorStream(true);

        long startTime = System.currentTimeMillis();
        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            logger.error("Process start failed: {}", e.getMessage());
            return new ExecutionResult(false, "Execution failed", null, -1, e.getMessage());
        }

        // Handle inputs and outputs
        try (OutputStream stdin = process.getOutputStream();
             InputStream stdout = process.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(stdout))) {

            // Write inputs if provided
            if (inputs != null && !inputs.isEmpty()) {
                try (BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(stdin, StandardCharsets.UTF_8))) {
                    for (String input : inputs) {
                        writer.write(input);
                        writer.newLine();
                    }
                    writer.flush();
                }
            }

            // Read output with timeout
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Future<List<String>> future = executor.submit(() -> {
                List<String> outputLines = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (outputLines.stream().mapToInt(String::length).sum() + line.length() > MAX_OUTPUT_LENGTH) {
                        outputLines.add("[OUTPUT TRUNCATED DUE TO SIZE LIMIT]");
                        break;
                    }
                    outputLines.add(line);
                }
                return outputLines;
            });

            List<String> outputLines;
            try {
                outputLines = future.get(timeoutSeconds, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                process.destroyForcibly();
                long executionTime = System.currentTimeMillis() - startTime;
                return new ExecutionResult(false, "Timeout exceeded", null, executionTime,
                        "Execution took longer than " + timeoutSeconds + " seconds");
            } catch (Exception e) {
                process.destroy();
                long executionTime = System.currentTimeMillis() - startTime;
                return new ExecutionResult(false, "Execution failed", null, executionTime, e.getMessage());
            } finally {
                executor.shutdownNow();
            }

            // Verify output
            long executionTime = System.currentTimeMillis() - startTime;
            String actualOutput = String.join("\n", outputLines);

            if (expectedOutputs != null && !expectedOutputs.isEmpty()) {
                String expectedOutput = String.join("\n", expectedOutputs);
                boolean success = actualOutput.equals(expectedOutput);
                String message = success ? "Execution successful" : "Output mismatch";

                return new ExecutionResult(
                        success,
                        message,
                        outputLines,
                        executionTime,
                        success ? null : "Expected: " + expectedOutput + "\nActual: " + actualOutput
                );
            }

            return new ExecutionResult(
                    true,
                    "Execution completed (no expected output provided)",
                    outputLines,
                    executionTime,
                    null
            );

        } catch (IOException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            return new ExecutionResult(false, "IO Error", null, executionTime, e.getMessage());
        } finally {
            process.destroy();
        }
    }
}