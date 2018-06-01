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
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.RowMapper;

import com.k9.batch.model.Person;
import com.k9.batch.processor.PersonItemProcessor;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Value(value = "input/persons_*.csv")
	private Resource[] resources;


	@Bean
	public FlatFileItemReader<Person> reader() {
		FlatFileItemReader<Person> reader = new FlatFileItemReader<Person>();
		reader.setLineMapper(new DefaultLineMapper<Person>() {
			{
				setLineTokenizer(new DelimitedLineTokenizer() {
					{
						setNames(new String[] { "firstName", "lastName", "email", "age" });
					}
				});
				setFieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {
					{
						setTargetType(Person.class);
					}
				});
			}
		});
		return reader;
	}

	@Bean
	public MultiResourceItemReader<Person> multiResourceItemReader() {
		MultiResourceItemReader<Person> multiResourceItemReader = new MultiResourceItemReader<Person>();
		multiResourceItemReader.setResources(resources);
		multiResourceItemReader.setDelegate(reader());
		return multiResourceItemReader;
	}

	@Bean
	public PersonItemProcessor processor() {
		return new PersonItemProcessor();
	}

	@Bean
	public FlatFileItemWriter<Person> writer() {
		FlatFileItemWriter<Person> writer = new FlatFileItemWriter<Person>();
		writer.setResource(new ClassPathResource("output/persons_output.csv"));
		// writer.setAppendAllowed(true);
		DelimitedLineAggregator<Person> lineAggregator = new DelimitedLineAggregator<Person>();
		lineAggregator.setDelimiter(",");

		BeanWrapperFieldExtractor<Person> fieldExtractor = new BeanWrapperFieldExtractor<Person>();
		fieldExtractor.setNames(new String[] { "firstName", "lastName", "email", "age" });
		lineAggregator.setFieldExtractor(fieldExtractor);

		writer.setLineAggregator(lineAggregator);
		return writer;
	}

	@Bean
	public Job myJob() {
		return jobBuilderFactory.get("myJob").incrementer(new RunIdIncrementer()).flow(step1()).end().build();
	}

	@Bean
	public Step step1() {
		return stepBuilderFactory.get("step1").<Person, Person>chunk(10).reader(multiResourceItemReader())
				.processor(processor()).writer(writer()).build();
	}
}