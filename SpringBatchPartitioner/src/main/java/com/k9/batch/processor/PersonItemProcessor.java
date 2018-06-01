package com.k9.batch.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.boot.autoconfigure.security.SecurityProperties.User;

import com.k9.batch.model.Person;

public class PersonItemProcessor implements ItemProcessor<Person, Person> {

	private static final Logger logger = LoggerFactory.getLogger(PersonItemProcessor.class);
	private String threadName;

	@Override
	public Person process(Person person) throws Exception {
		logger.error("thread :" + threadName + " processing : " + person.getPersonId() + " Person :"
				+ person.getFirstName());
		return person;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}

}