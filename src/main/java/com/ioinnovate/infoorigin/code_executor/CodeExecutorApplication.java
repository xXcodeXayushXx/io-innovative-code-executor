package com.ioinnovate.infoorigin.code_executor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class CodeExecutorApplication {
	public static void main(String[] args) {
		SpringApplication.run(CodeExecutorApplication.class, args);
	}
}