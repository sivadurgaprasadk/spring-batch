package com.k9.batch.skipper;

import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.step.skip.SkipLimitExceededException;
import org.springframework.batch.core.step.skip.SkipPolicy;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.file.FlatFileParseException;

public class FileReaderVerificationSkipper implements SkipPolicy {
	private static final Logger logger = LoggerFactory.getLogger(FileReaderVerificationSkipper.class);

	@Override
	public boolean shouldSkip(Throwable t, int skipCount) throws SkipLimitExceededException {
		logger.warn("request received");
		System.out.println("request received");
		if (t instanceof ItemStreamException) {
			logger.error("item not available :" + t.getMessage() + " skip count :" + skipCount);
			return true;
		} else if (t instanceof FileNotFoundException) {
			logger.error("File not availble");
			return false;
		} else if (t instanceof FlatFileParseException && skipCount <= 5) {
			logger.error("Exception :" + t.getMessage() + " skip count :" + skipCount);
			return true;
		}
		return false;
	}

}
