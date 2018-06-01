package com.k9.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
public class MultiResourceItemReaderSpringBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(MultiResourceItemReaderSpringBatchApplication.class, args);
	}
}
