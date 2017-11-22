package org.jmal98.ec2.collectors;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.prometheus.client.Collector;

public class Volumes extends Collector {
	
	private final Logger logger = LogManager.getLogger(getClass());

	@Override
	public List<MetricFamilySamples> collect() {
		List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
		
		try {
			
		} finally {
			logger.info("done");
		}
		
		return mfs;
	}

}
