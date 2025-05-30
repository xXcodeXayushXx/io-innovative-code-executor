package com.ioinnovate.infoorigin.code_executor.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Python script execution results")
public class ExecutionResponse {
    @Schema(description = "Whether execution succeeded")
    private boolean success;

    @Schema(description = "Execution status message")
    private String message;

    @Schema(description = "Execution time in milliseconds")
    private long executionTime;

    @Schema(description = "Error details if execution failed", nullable = true)
    private String errors;

    public ExecutionResponse(boolean success, String message, long executionTime, String errors) {
        this.success = success;
        this.message = message;
        this.executionTime = executionTime;
        this.errors = errors;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }
}
