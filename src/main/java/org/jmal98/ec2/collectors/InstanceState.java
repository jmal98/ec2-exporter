package org.jmal98.ec2.collectors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.Reservation;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

public class InstanceState extends Collector {

	private final Logger logger = LogManager.getLogger(getClass());

	@Override
	public List<MetricFamilySamples> collect() {
		List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
		
		Map<InstanceStateName, Integer> instancesByState = new HashMap<InstanceStateName,Integer>();
		for (InstanceStateName name : InstanceStateName.values())
			instancesByState.put(name, 0);
		
		AmazonEC2 ec2 = AmazonEc2ClientCache.getClient();
		try {
			
			String nextToken = null;	
			while ((nextToken = list(ec2, instancesByState, nextToken)) != null) {
				if (nextToken != null)
					logger.warn("Iterating to obtain more information");
			}
			
			GaugeMetricFamily labeledGauge = new GaugeMetricFamily(
					"ec2_instances",
					"Instance details by state",
					Arrays.asList("state")
				);
			Iterator<InstanceStateName> iterator = instancesByState.keySet().iterator();
			while (iterator.hasNext()) {
				InstanceStateName state = iterator.next();
				Integer total = instancesByState.get(state);
				
				labeledGauge.addMetric(Arrays.asList(state.toString()),
						Double.valueOf(total));
			}
			mfs.add(labeledGauge);
		} finally {}
		
		return mfs;
	}
	
	private static String list(AmazonEC2 ec2, Map<InstanceStateName, Integer> instancesByState, String nextToken) {
		int maxResults = 250;  // a quarter of max to reduce memory usage
		try {
			DescribeInstancesResult dir = null;
			if (nextToken != null) {
				dir = ec2
						.describeInstances(
								new DescribeInstancesRequest()
								.withMaxResults(maxResults)
								.withNextToken(nextToken)
								);
			} else {
				dir = ec2
						.describeInstances(
								new DescribeInstancesRequest()
								.withMaxResults(maxResults)
								);
			}
			
			if (dir != null) {
				
				for (Reservation reservation : dir.getReservations()) {
					for (Instance instance : reservation.getInstances()) {
						Integer cur = instancesByState.get(InstanceStateName.fromValue(instance.getState().getName()));
						instancesByState.put(InstanceStateName.fromValue(instance.getState().getName()), new Integer(cur + 1));
					}
				}

				return dir.getNextToken();
			}
		} finally {}
		return null;
	}


}
