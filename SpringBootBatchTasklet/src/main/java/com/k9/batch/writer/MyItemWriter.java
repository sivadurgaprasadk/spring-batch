package com.k9.batch.writer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;

import com.k9.batch.tasklet.TaskletStep;

public class MyItemWriter implements ItemWriter<String> {

	private static final Logger logger = LoggerFactory.getLogger(MyItemWriter.class);

	@Override
	public void write(List<? extends String> paths) throws Exception {
		for (String filePath : paths) {
			logger.info("filePath = " + filePath);
			try (Stream<String> stream = Files.lines(Paths.get(filePath))) {
				stream.forEach(logger::info);
			} catch (IOException e) {
				throw (e);
			}
		}
	}
}