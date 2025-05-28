package com.ioinnovate.infoorigin.code_executor.controller;

import com.ioinnovate.infoorigin.code_executor.dto.CodeExecutionRequest;
import com.ioinnovate.infoorigin.code_executor.dto.ExecutionResponse;
import com.ioinnovate.infoorigin.code_executor.dto.ExecutionResult;
import com.ioinnovate.infoorigin.code_executor.service.CodeExecutionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Swagger/OpenAPI imports
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@Tag(name = "Code Execution", description = "APIs for executing code and verifying outputs")
public class CodeExecutionController {

    private static final Logger logger = LoggerFactory.getLogger(CodeExecutionController.class);
    private final CodeExecutionService codeExecutionService;

    public CodeExecutionController(CodeExecutionService codeExecutionService) {
        this.codeExecutionService = codeExecutionService;
    }

    @PostMapping("/execute")
    @Operation(
            summary = "Execute code snippet",
            description = "Executes the submitted code snippet with optional input/output validation and timeout.",
            requestBody = @RequestBody(
                    description = "Code execution request payload",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CodeExecutionRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Code Execution Example",
                                            value = "{\n" +
                                                    "  \"code\": \"print(input())\",\n" +
                                                    "  \"inputs\": [\"Hello\"],\n" +
                                                    "  \"expectedOutputs\": [\"Hello\"],\n" +
                                                    "  \"timeoutSeconds\": 5\n" +
                                                    "}"
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Execution completed successfully",
                            content = @Content(schema = @Schema(implementation = ExecutionResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad request (e.g., empty code submitted)",
                            content = @Content(schema = @Schema(implementation = ExecutionResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error during code execution",
                            content = @Content(schema = @Schema(implementation = ExecutionResponse.class))
                    )
            }
    )
    public ResponseEntity<ExecutionResponse> executeCode(
            @org.springframework.web.bind.annotation.RequestBody CodeExecutionRequest request) {
        logger.info("Received execution request for code snippet");

        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ExecutionResponse(false, "Code cannot be empty", null, null, null));
        }

        try {
            ExecutionResult result = codeExecutionService.executeAndVerify(
                    request.getCode(),
                    request.getInputs(),
                    request.getExpectedOutputs(),
                    request.getTimeoutSeconds() != null ? request.getTimeoutSeconds() : 10
            );

            ExecutionResponse response = new ExecutionResponse(
                    result.isSuccess(),
                    result.getMessage(),
                    result.getActualOutputs(),
                    result.getExecutionTime(),
                    result.getErrorOutput()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error executing code: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(new ExecutionResponse(false, "Internal server error", null, null, e.getMessage()));
        }
    }
}
