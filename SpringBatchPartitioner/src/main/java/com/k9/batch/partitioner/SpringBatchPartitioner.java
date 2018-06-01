package com.k9.batch.partitioner;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

public class SpringBatchPartitioner implements Partitioner {

	private static final Logger logger = LoggerFactory.getLogger(SpringBatchPartitioner.class);

	@Override
	public Map<String, ExecutionContext> partition(int gridSize) {
		Map<String, ExecutionContext> result = new HashMap<String, ExecutionContext>();

		int range = 10;
		int fromId = 1;
		int toId = range;

		for (int i = 1; i <= gridSize; i++) {
			ExecutionContext value = new ExecutionContext();

			logger.info("Starting : Thread" + i);
			logger.info("fromId : " + fromId);
			logger.info("toId : " + toId);

			value.putInt("fromId", fromId);
			value.putInt("toId", toId);

			// give each thread a name, thread 1,2,3
			value.putString("name", "Thread" + i);

			result.put("partition" + i, value);

			fromId = toId + 1;
			toId += range;
		}
		return result;
	}

}
