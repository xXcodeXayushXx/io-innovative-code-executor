package com.ioinnovate.infoorigin.code_executor.dto;

import java.util.List;

public class CodeExecutionResult {
    private boolean success;
    private String message;
    private List<String> actualOutputs;
    private long executionTime;
    private String errorOutput;

    public CodeExecutionResult(boolean success, String message, List<String> actualOutputs,
                               long executionTime, String errorOutput) {
        this.success = success;
        this.message = message;
        this.actualOutputs = actualOutputs;
        this.executionTime = executionTime;
        this.errorOutput = errorOutput;
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<String> getActualOutputs() { return actualOutputs; }
    public long getExecutionTime() { return executionTime; }
    public String getErrorOutput() { return errorOutput; }
}
