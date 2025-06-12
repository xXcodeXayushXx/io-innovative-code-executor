package com.ioinnovate.infoorigin.code_executor.controller;

import com.ioinnovate.infoorigin.code_executor.dto.ExecutionRequest;
import com.ioinnovate.infoorigin.code_executor.dto.ExecutionResponse;
import com.ioinnovate.infoorigin.code_executor.service.PythonExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class PythonExecutionController {

    private final PythonExecutionService pythonExecutionService;

    public PythonExecutionController(PythonExecutionService pythonExecutionService) {
        this.pythonExecutionService = pythonExecutionService;
    }


    @Operation(
            summary = "Execute a Python script",
            description = "Runs the specified Python file and returns execution results",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Script executed successfully",
                            content = @Content(schema = @Schema(implementation = ExecutionResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid file path provided"
                    )
            }
    )
    @PostMapping(
            value = "/execute-python-script",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ExecutionResponse executePythonScript(@RequestBody ExecutionRequest request) {
        return pythonExecutionService.parallelExecutePythonScript(request.getFilePath());
    }

    @PostMapping("/execute-python-file")
    public ExecutionResponse executePythonFile(@RequestBody byte[] scriptBytes) {
        return pythonExecutionService.executePythonFile(scriptBytes);
    }
}
