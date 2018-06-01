package com.k9.batch.tasklet;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
public class TaskletStep implements Tasklet {

	private static final Logger logger = LoggerFactory.getLogger(TaskletStep.class);

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		try {
			File file = new File("MyFiles/test.txt");
			if (file.exists()) {
				file.delete();
				logger.info("### TaskletStep:" + file.getName() + " is deleted!");
			} else {
				logger.info("Delete operation is failed.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}