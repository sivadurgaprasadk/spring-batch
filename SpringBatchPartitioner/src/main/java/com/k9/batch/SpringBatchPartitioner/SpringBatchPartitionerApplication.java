package com.k9.batch.SpringBatchPartitioner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.k9.batch.config.BatchConfig;

@SpringBootApplication
@Import(value = BatchConfig.class)
public class SpringBatchPartitionerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBatchPartitionerApplication.class, args);
	}
}
