package com.github.yuga.gandikota.latency.monitoring.utils;

import java.util.concurrent.TimeUnit;

/**
 * Factory interface to create LatencyMonitor instances.
 * Be default, <code>SimpleLatencyMonitorFactory</code> is used. If you wish to override this
 * you will have to set system property with the name as defined by {@link LatencyMonitorFactory#LATENCY_MONITOR_FACTORY_PROPERTY_NAME}
 * 
 * @author Yuga Gandikota
 * @see SimpleLatencyMonitorFactory
 */
public interface LatencyMonitorFactory {
	
	static final String LATENCY_MONITOR_FACTORY_PROPERTY_NAME = "com.github.yuga.gandikota.latency.monitoring.proxy.LatencyMonitorFactory";
	
	public LatencyMonitor createLatencyMonitor(int aSampleSize, TimeUnit aUnit);
}
