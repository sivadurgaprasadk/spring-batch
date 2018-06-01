package com.k9.batch.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class MyItemReader implements ItemReader<String> {
	
	private static final Logger logger = LoggerFactory.getLogger(MyItemReader.class);

	private String[] files = { "MyFiles/test.txt" };
	public int count = 0;

	@Override
	public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {

		if (count < files.length) {
			logger.debug("count :" + count);
			return files[count++];
		} else {
			count = 0;
		}
		return null;
	}
}