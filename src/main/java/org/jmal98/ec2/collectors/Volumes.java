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
import com.amazonaws.services.ec2.model.DescribeVolumesRequest;
import com.amazonaws.services.ec2.model.DescribeVolumesResult;
import com.amazonaws.services.ec2.model.Volume;
import com.amazonaws.services.ec2.model.VolumeType;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

public class Volumes extends Collector {
	
	private final Logger logger = LogManager.getLogger(getClass());

	@Override
	public List<MetricFamilySamples> collect() {
		List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();
		
		Map<VolumeType, Integer> volumesByTypeAndSize = new HashMap<VolumeType,Integer>();
		for (VolumeType vType : VolumeType.values())
			volumesByTypeAndSize.put(vType, 0);
		
		AmazonEC2 ec2 = AmazonEc2ClientCache.getClient();
		try {
			
			String nextToken = null;	
			while ((nextToken = list(ec2, volumesByTypeAndSize, nextToken)) != null) {
				if (nextToken != null)
					logger.warn("Iterating to obtain more information");
			}
			
			Iterator<VolumeType> iterator = volumesByTypeAndSize.keySet().iterator();
			while (iterator.hasNext()) {
				VolumeType vType = iterator.next();
				Integer totalSize = volumesByTypeAndSize.get(vType);
				
				GaugeMetricFamily labeledGauge = new GaugeMetricFamily(
						"ec2_ebs_volumes_allocated",
						"Total by Type in GBs",
						Arrays.asList("type")
					);
				
				labeledGauge.addMetric(Arrays.asList(vType.toString()),
						Double.valueOf(totalSize));
				
				mfs.add(labeledGauge);
			}
			
		} finally {}
		
		return mfs;
	}
	
	private static String list(AmazonEC2 ec2, Map<VolumeType, Integer> volumesByTypeAndSize, String nextToken) {
		int maxResults = 250;  // half of max to reduce memory usage
		try {
			DescribeVolumesResult dvr = null;
			if (nextToken != null) {
				dvr = ec2
						.describeVolumes(
								new DescribeVolumesRequest()
								.withMaxResults(maxResults)
								.withNextToken(nextToken)
								);
			} else {
				dvr = ec2
						.describeVolumes(
								new DescribeVolumesRequest()
								.withMaxResults(maxResults)
								);
			}
			
			if (dvr != null) {
				
				for (Volume v : dvr.getVolumes()) {
					Integer cursize = volumesByTypeAndSize.get(VolumeType.fromValue(v.getVolumeType()));
					volumesByTypeAndSize.put(VolumeType.fromValue(v.getVolumeType()), new Integer(cursize +  v.getSize()));
				}
				return dvr.getNextToken();
			}
		} finally {}
		return null;
	}

}
