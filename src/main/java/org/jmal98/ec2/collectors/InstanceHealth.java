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
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.SummaryStatus;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

public class InstanceHealth extends Collector {
	
	private final Logger logger = LogManager.getLogger(getClass());

	@Override
	public List<MetricFamilySamples> collect() {
		List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
		
		Map<SummaryStatus, Integer> instancesByStatus = new HashMap<SummaryStatus,Integer>();
		for (SummaryStatus name : SummaryStatus.values())
			instancesByStatus.put(name, 0);
		
		
		AmazonEC2 ec2 = AmazonEc2ClientCache.getClient();
		try {
			
			String nextToken = null;	
			while ((nextToken = list(ec2, instancesByStatus, nextToken)) != null) {
				if (nextToken != null)
					logger.warn("Iterating to obtain more information");
			}


			GaugeMetricFamily labeledGauge = new GaugeMetricFamily(
					"ec2_instance_status",
					"Instance details by status",
					Arrays.asList("status")
				);
			Iterator<SummaryStatus> iterator = instancesByStatus.keySet().iterator();
			while (iterator.hasNext()) {
				SummaryStatus state = iterator.next();
				Integer total = instancesByStatus.get(state);
				
				labeledGauge.addMetric(Arrays.asList(state.toString()),
						Double.valueOf(total));
			}
			mfs.add(labeledGauge);			
		} finally {}
		
		return mfs;
	}
	
	private static String list(AmazonEC2 ec2, Map<SummaryStatus, Integer> instancesByState, String nextToken) {
		int maxResults = 250;  // a quarter of max to reduce memory usage
		try {
			DescribeInstanceStatusResult disr = null;
			if (nextToken != null) {
				disr = ec2
						.describeInstanceStatus(
								new DescribeInstanceStatusRequest()
								.withMaxResults(maxResults)
								.withIncludeAllInstances(false)
								.withNextToken(nextToken)
								);
			} else {
				disr = ec2
						.describeInstanceStatus(
								new DescribeInstanceStatusRequest()
								.withIncludeAllInstances(false)
								.withMaxResults(maxResults)
								);
			}
			
			if (disr != null) {
				
				for (InstanceStatus is : disr.getInstanceStatuses()) {
					
					Integer cur = instancesByState.get(SummaryStatus.fromValue(is.getSystemStatus().getStatus()));
					instancesByState.put(SummaryStatus.fromValue(is.getSystemStatus().getStatus()), new Integer(cur + 1));	
				
					
					cur = instancesByState.get(SummaryStatus.fromValue(is.getInstanceStatus().getStatus()));
					instancesByState.put(SummaryStatus.fromValue(is.getInstanceStatus().getStatus()), new Integer(cur + 1));
					
				}

				return disr.getNextToken();
			}
		} finally {}
		return null;
	}

}
