package com.yuga.latency.monitoring.utils;

import java.util.concurrent.TimeUnit;

/**
 * Class to collect samples in nano seconds and keep track of average. 
 * 
 * @author Yuga Gandikota
 */
public class SimpleLatencyMonitor implements LatencyMonitor {
	
	/*Circular buffer to keep samples.*/
	CircularBuffer<Long> samples;
	
	/*time units that this monitor is setup to use.*/
	TimeUnit timeUnit;
	
	/**
	 * Constructor that instantiates an instance with sample size and time units to use. 
	 * 
	 * @param aSampleSize
	 * @param aUnit
	 */
	public SimpleLatencyMonitor(int aSampleSize, TimeUnit aUnit) {
		samples = createNewCircularBuffer(aSampleSize);
		timeUnit = aUnit;
	}

	/**
	 * Utility method that you can override to be able to override CircularBuffer implementation used.
	 * @param aSampleSize
	 * @return
	 */
	protected CircularBuffer<Long> createNewCircularBuffer(int aSampleSize) {
		return new SimpleCircularBuffer<Long>(Long.class, aSampleSize);
	}

	/**
	 * Adds sample to the buffer. duration is expected to be in nanoseconds
	 * @param duration duration is nanoseconds.
	 */
	public void addSample(long duration) {
		samples.add(duration);
	}
	
	/**
	 * Calculates average in nanoseconds, based on current snapshot of the buffer
	 * and converts it to this instance's time unit
	 * 
	 * @return average from current snapshot of samples buffer, converted to this instance's TimeUnit.
	 */
	public long getAverage() {
        Long[] snapshot = samples.getSnapshot();

        if (snapshot.length == 0) {
            return 0L;
        }

        long totalDuration = 0L;

        for (Long duration : snapshot) {
        	if (duration != null) {
        		totalDuration += duration;
        	}
        }

        return timeUnit.convert(totalDuration / snapshot.length,
                                      TimeUnit.NANOSECONDS);		
	}
}
