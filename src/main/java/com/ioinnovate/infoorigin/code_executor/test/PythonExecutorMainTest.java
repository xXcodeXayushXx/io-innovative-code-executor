package com.ioinnovate.infoorigin.code_executor.test;

import com.ioinnovate.infoorigin.code_executor.dto.ExecutionResponse;
import com.ioinnovate.infoorigin.code_executor.service.PythonExecutionService;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class PythonExecutorMainTest {

    public static void main(String[] args) {
        PythonExecutionService service = new PythonExecutionService();

        // Define multiple test scripts
        List<String> scriptContents = List.of(
                "print(\"True\")",
                "print(\"False\")",
                "raise Exception(\"Intentional Error\")",
                "import time\ntime.sleep(2)\nprint(\"True\")"
        );

        // Save scripts to temp files
        List<File> scriptFiles = new ArrayList<>();
        for (int i = 0; i < scriptContents.size(); i++) {
            try {
                File file = File.createTempFile("test_script_" + i, ".py");
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(scriptContents.get(i));
                }
                file.deleteOnExit();
                scriptFiles.add(file);
            } catch (Exception e) {
                System.err.println("Error creating temp file: " + e.getMessage());
            }
        }

        // Run scripts in parallel using executor
        ExecutorService threadPool = Executors.newFixedThreadPool(4);
        List<Future<ExecutionResponse>> futureResults = new ArrayList<>();

        System.out.println("\n=== Executing Python Scripts in Parallel ===\n");

        for (File file : scriptFiles) {
            futureResults.add(threadPool.submit(() -> service.parallelExecutePythonScript(file.getAbsolutePath())));
        }

        // Print results
        for (int i = 0; i < futureResults.size(); i++) {
            try {
                ExecutionResponse result = futureResults.get(i).get();
                System.out.println("Script #" + (i + 1));
                System.out.println("Success       : " + result.getSuccess());
                System.out.println("Message       : " + result.getMessage());
                System.out.println("Execution Time: " + result.getExecutionTime() + " ms");
                System.out.println("Errors        : " + (result.getErrors() == null ? "None" : result.getErrors()));
                System.out.println("--------------------------------------------------\n");
            } catch (Exception e) {
                System.out.println("Failed to execute script #" + (i + 1) + ": " + e.getMessage());
            }
        }

        // Cleanup
        threadPool.shutdown();
        service.shutdown(); // Shut down internal executor

        System.out.println("All scripts processed.");
    }
}
