package org.jmal98.ec2.collectors;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;

public class AmazonEc2ClientCache {

	private final static Logger logger = LogManager.getLogger(AmazonEc2ClientCache.class);

	static Map<String, AmazonEC2> cache = new HashMap<String, AmazonEC2>();

	public static AmazonEC2 getClient() {
		if (!cache.containsKey("default")) {
			logger.info("Constructing new default client.");
			cache.put("default", AmazonEC2ClientBuilder.defaultClient());
		}
		return cache.get("default");
	}

}
