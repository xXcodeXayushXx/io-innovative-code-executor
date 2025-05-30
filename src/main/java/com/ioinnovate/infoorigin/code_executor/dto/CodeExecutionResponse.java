package com.ioinnovate.infoorigin.code_executor.dto;

import java.util.List;

public class CodeExecutionResponse {
    private boolean success;
    private String message;
    private List<String> outputs;
    private Long executionTimeMs;
    private String error;

    public CodeExecutionResponse(boolean success, String message, List<String> outputs,
                                 Long executionTimeMs, String error) {
        this.success = success;
        this.message = message;
        this.outputs = outputs;
        this.executionTimeMs = executionTimeMs;
        this.error = error;
    }

    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<String> getOutputs() { return outputs; }
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public String getError() { return error; }
}
