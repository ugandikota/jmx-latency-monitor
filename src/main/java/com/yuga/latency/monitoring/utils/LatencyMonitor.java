package com.yuga.latency.monitoring.utils;

/**
 * Class to collect samples in nano seconds and keep track of average. 
 * 
 * @author Yuga Gandikota
 */
public interface LatencyMonitor {
	
	/**
	 * Adds sample to the buffer. duration is expected to be in nanoseconds
	 * @param duration duration is nanoseconds.
	 */
	public void addSample(long duration);
	
	/**
	 * Calculates average in nanoseconds, based on current snapshot of the buffer
	 * and converts it to this instance's time unit
	 * 
	 * @return average from current snapshot of samples buffer, converted to this instance's TimeUnit.
	 */
	public long getAverage();
}
