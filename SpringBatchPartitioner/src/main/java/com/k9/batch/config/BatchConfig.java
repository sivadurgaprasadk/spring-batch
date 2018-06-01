package com.k9.batch.config;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.RowMapper;

import com.k9.batch.model.Person;
import com.k9.batch.partitioner.SpringBatchPartitioner;
import com.k9.batch.processor.PersonItemProcessor;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	public DataSource dataSource;

	@Bean
	@StepScope
	public JdbcPagingItemReader<Person> reader(@Value("#{stepExecutionContext[fromId]}") String from,
			@Value("#{stepExecutionContext[toId]}") String to) {
		JdbcPagingItemReader<Person> reader = new JdbcPagingItemReader<>();
		reader.setDataSource(dataSource);
		reader.setRowMapper(new RowMapper<Person>() {
			@Override
			public Person mapRow(ResultSet rs, int rowNum) throws SQLException {
				return new Person(rs.getInt("person_id"), rs.getString("first_name"), rs.getString("last_name"),
						rs.getString("email"), rs.getInt("age"));
			}
		});
		MySqlPagingQueryProvider queryProvider = new MySqlPagingQueryProvider();
		queryProvider.setSelectClause("person_id, first_name, last_name, email, age");
		queryProvider.setFromClause("from person");
		queryProvider.setWhereClause("where person_id >= :from  and person_id <= :to");

		Map<String, Order> sortKeys = new HashMap<>(1);
		sortKeys.put("person_id", Order.ASCENDING);
		queryProvider.setSortKeys(sortKeys);
		reader.setQueryProvider(queryProvider);
		Map<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("from", from);
		parameterValues.put("to", to);
		reader.setParameterValues(parameterValues);
		reader.setPageSize(1000);

		return reader;
	}

	@Bean
	@StepScope
	public PersonItemProcessor processor(@Value("#{stepExecutionContext[name]}") String threadName) {
		PersonItemProcessor processor = new PersonItemProcessor();
		processor.setThreadName(threadName);
		return processor;
	}

	@Bean
	@StepScope
	public FlatFileItemWriter<Person> writer(@Value("#{stepExecutionContext[fromId]}") String from,
			@Value("#{stepExecutionContext[toId]}") String to) {
		FlatFileItemWriter<Person> writer = new FlatFileItemWriter<Person>();
		writer.setResource(new FileSystemResource("output/persons_" + from + "-" + to + ".csv"));
		writer.setAppendAllowed(false);
		DelimitedLineAggregator<Person> lineAggregator = new DelimitedLineAggregator<Person>();
		lineAggregator.setDelimiter(",");

		BeanWrapperFieldExtractor<Person> fieldExtractor = new BeanWrapperFieldExtractor<Person>();
		fieldExtractor.setNames(new String[] { "firstName", "lastName", "email", "age" });
		lineAggregator.setFieldExtractor(fieldExtractor);

		writer.setLineAggregator(lineAggregator);
		return writer;
	}

	@Bean
	public Partitioner myPartitioner() {
		return new SpringBatchPartitioner();
	}

	@Bean
	public TaskExecutor asynchExecutor() {
		SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
		return executor;
	}

	@Bean
	public Job partitionerJob() {
		return jobBuilderFactory.get("partitionerJob").start(masterStep()).build();
	}

	@Bean
	public PartitionHandler partitionHandler() {
		TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
		handler.setGridSize(10);
		handler.setTaskExecutor(asynchExecutor());
		handler.setStep(slaveStep());
		try {
			handler.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return handler;
	}

	@Bean
	public Step masterStep() {
		return stepBuilderFactory.get("masterStep").partitioner(slaveStep().getName(), myPartitioner())
				.partitionHandler(partitionHandler()).build();
	}

	@Bean
	public Step slaveStep() {
		return stepBuilderFactory.get("slaveStep").<Person, Person>chunk(1).reader(reader(null, null))
				.processor(processor(null)).writer(writer(null, null)).build();
	}

}
