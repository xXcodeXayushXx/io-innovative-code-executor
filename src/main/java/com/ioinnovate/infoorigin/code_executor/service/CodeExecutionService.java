package com.ioinnovate.infoorigin.code_executor.service;

import org.springframework.stereotype.Service;
import java.io.*;
import java.util.concurrent.TimeUnit;

@Service
public class CodeExecutionService {

    public boolean executeAndVerify(String code, String input, String expectedOutput) {
        try {
            // Create a temporary Python file
            File pythonFile = File.createTempFile("script", ".py");
            pythonFile.deleteOnExit();

            // Write the code to the file
            try (FileWriter writer = new FileWriter(pythonFile)) {
                writer.write(code);
            }

            // Set up the process builder
            ProcessBuilder processBuilder = new ProcessBuilder("python", pythonFile.getAbsolutePath());
            processBuilder.redirectErrorStream(true);

            // Start the process
            Process process = processBuilder.start();

            // Write input to the process if provided
            if (input != null && !input.isEmpty()) {
                try (OutputStream out = process.getOutputStream();
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {
                    writer.write(input);
                    writer.flush();
                }
            }

            // Read the output
            StringBuilder output = new StringBuilder();
            try (InputStream in = process.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // Wait for process to complete
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroy();
                return false;
            }

            // Clean up output by removing trailing newline
            String actualOutput = output.toString().trim();

            // Compare with expected output
            return actualOutput.equals(expectedOutput);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
}
