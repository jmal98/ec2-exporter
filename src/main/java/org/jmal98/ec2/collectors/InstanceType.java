package org.jmal98.ec2.collectors;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.*;
import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class InstanceType extends Collector {

	private final Logger logger = LogManager.getLogger(getClass());

	@Override
	public List<MetricFamilySamples> collect() {
		List<MetricFamilySamples> mfs = new ArrayList<>();

		Map<String, Integer> instancesByType = new HashMap<>();

		AmazonEC2 ec2 = AmazonEc2ClientCache.getClient();

        String nextToken = null;
        while ((nextToken = list(ec2, instancesByType, nextToken)) != null) {
            logger.warn("Iterating to obtain more information");
        }

        GaugeMetricFamily labeledGauge = new GaugeMetricFamily(
                "ec2_instances_by_type_running",
                "Instance details by type, filtered by running instances",
                Collections.singletonList("type")
            );
        for (String name : instancesByType.keySet()) {
            Integer total = instancesByType.get(name);

            labeledGauge.addMetric(Collections.singletonList(name),
                    Double.valueOf(total));
        }
        mfs.add(labeledGauge);

        return mfs;
	}

	private static String list(AmazonEC2 ec2, Map<String, Integer> instancesByType, String nextToken) {
		int maxResults = 250;  // a quarter of max to reduce memory usage
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
                    if(!instance.getState().getName().equals("running")) {
                        continue;
                    }
                    Integer cur = instancesByType.getOrDefault(instance.getInstanceType(), 0);
                    instancesByType.put(instance.getInstanceType(), cur + 1);
                }
            }

            return dir.getNextToken();
        }
        return null;
	}


}
