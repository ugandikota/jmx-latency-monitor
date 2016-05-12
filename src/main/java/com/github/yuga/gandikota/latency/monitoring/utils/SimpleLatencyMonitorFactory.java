package com.github.yuga.gandikota.latency.monitoring.utils;

import java.util.concurrent.TimeUnit;

/**
 * Simple implementation of LatencyMonitorFactory interface.
 * 
 * @author Yuga Gandikota
 * @see LatencyMonitorFactory
 */
public class SimpleLatencyMonitorFactory implements LatencyMonitorFactory {

	public LatencyMonitor createLatencyMonitor(int aSampleSize, TimeUnit aUnit) {
		return new SimpleLatencyMonitor(aSampleSize, aUnit);
	}
}
