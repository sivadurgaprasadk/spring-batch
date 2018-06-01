package com.k9.batch.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.xstream.XStreamMarshaller;

import com.k9.batch.model.Person;
import com.k9.batch.processor.PersonItenProcessor;

@Configuration
@EnableBatchProcessing
public class BatchConfig {
	
	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	private DataSource dataSource;
	
	@Bean
	public JdbcCursorItemReader<Person> reader(){
		JdbcCursorItemReader<Person> cursorItemReader = new JdbcCursorItemReader<>();
		cursorItemReader.setDataSource(dataSource);
		cursorItemReader.setSql("SELECT person_id,first_name,last_name,email,age FROM person");
		cursorItemReader.setRowMapper(new PersonRowMapper());
		return cursorItemReader;
	}
	
	@Bean
	public PersonItenProcessor processor(){
		return new PersonItenProcessor();
	}
	
	@Bean
	public StaxEventItemWriter<Person> writer(){
		StaxEventItemWriter<Person> writer = new StaxEventItemWriter<Person>();
		writer.setResource(new ClassPathResource("persons.xml"));
		
		Map<String,String> aliasesMap =new HashMap<String,String>();
		aliasesMap.put("Person", "com.k9.batch.model.Person");
		XStreamMarshaller marshaller = new XStreamMarshaller();
		marshaller.setAliases(aliasesMap);
		writer.setMarshaller(marshaller);
		writer.setRootTagName("Persons");
		writer.setOverwriteOutput(true);
		return writer;
	}
	
	@Bean
	public Step step(){
		return stepBuilderFactory.get("step").<Person,Person>chunk(10).reader(reader()).processor(processor()).writer(writer()).build();
	}

	@Bean
	public Job exportPerosnJob(){
		return jobBuilderFactory.get("PeronJob").incrementer(new RunIdIncrementer()).flow(step()).end().build();
	}
}
