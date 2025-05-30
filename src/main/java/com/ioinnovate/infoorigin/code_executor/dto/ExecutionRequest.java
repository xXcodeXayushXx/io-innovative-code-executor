package com.ioinnovate.infoorigin.code_executor.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class ExecutionRequest {
    @Schema(
            description = "Absolute path to the Python script",
            example = "C:/user/scripts/test.py"
    )
    private String filePath;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
