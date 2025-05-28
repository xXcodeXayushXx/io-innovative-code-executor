package com.ioinnovate.infoorigin.code_executor.controller;

import com.ioinnovate.infoorigin.code_executor.dto.CodeExecutionRequest;
import com.ioinnovate.infoorigin.code_executor.service.CodeExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CodeExecutionController {

    @Autowired
    private CodeExecutionService codeExecutionService;

    @PostMapping("/execute")
    public boolean executeCode(@RequestBody CodeExecutionRequest request) {
        return codeExecutionService.executeAndVerify(
                request.getCode(),
                request.getInput(),
                request.getExpectedOutput()
        );
    }
}
