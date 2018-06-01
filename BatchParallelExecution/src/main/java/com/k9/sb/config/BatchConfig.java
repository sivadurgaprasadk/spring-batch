package com.k9.sb.config;

import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.TaskletStep;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

	private static final Logger logger = LoggerFactory.getLogger(BatchConfig.class);
	@Autowired
	private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private StepBuilderFactory stepBuilderFactor;

	public TaskletStep taskletStep(String step) {
		return stepBuilderFactor.get(step).tasklet((a, b) -> {
			IntStream.range(1, 100).forEach(token -> logger
					.info("step :" + step + " token :" + token + " thread :" + Thread.currentThread().getName()));
			return RepeatStatus.FINISHED;
		}).build();
	}

	@Bean

	public Job parallelStepsJob() {

		Flow masterFlow = new FlowBuilder<Flow>("MasterFlow").start(taskletStep("MasterStep")).build();

		Flow flowJob1 = new FlowBuilder<Flow>("flow1").start(taskletStep("SlaveStep1")).build();
		Flow flowJob2 = new FlowBuilder<Flow>("flow2").start(taskletStep("SlaveStep2")).build();
		Flow flowJob3 = new FlowBuilder<Flow>("flow3").start(taskletStep("SlaveStep3")).build();

		Flow slaveFlow = new FlowBuilder<Flow>("slaveFlow").split(new SimpleAsyncTaskExecutor())
				.add(flowJob1, flowJob2, flowJob3).build();

		Flow sampleFlow = new FlowBuilder<Flow>("sample").start(taskletStep("SampleStep")).build();

		return (jobBuilderFactory.get("parallelFlowJob").incrementer(new RunIdIncrementer()).start(masterFlow)
				.next(slaveFlow).next(sampleFlow).build()).build();

	}

}
