package com.ioinnovate.infoorigin.code_executor.dto;

import java.util.List;

public class CodeExecutionRequest {
    private String code;
    private List<String> inputs;
    private List<String> expectedOutputs;
    private Integer timeoutSeconds;

    // Getters and setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public List<String> getInputs() { return inputs; }
    public void setInputs(List<String> inputs) { this.inputs = inputs; }
    public List<String> getExpectedOutputs() { return expectedOutputs; }
    public void setExpectedOutputs(List<String> expectedOutputs) { this.expectedOutputs = expectedOutputs; }
    public Integer getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(Integer timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
}