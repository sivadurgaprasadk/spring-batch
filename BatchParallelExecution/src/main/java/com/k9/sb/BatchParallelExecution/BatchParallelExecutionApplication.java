package com.k9.sb.BatchParallelExecution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.k9.sb.config.BatchConfig;

@SpringBootApplication
@Import(value = BatchConfig.class)
public class BatchParallelExecutionApplication {

	public static void main(String[] args) {
		SpringApplication.run(BatchParallelExecutionApplication.class, args);
	}
}
