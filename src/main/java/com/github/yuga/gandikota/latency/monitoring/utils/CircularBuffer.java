package com.github.yuga.gandikota.latency.monitoring.utils;

/**
 * Simple interface for circular buffer.
 * 
 * @author Yuga Gandikota
 */
public interface CircularBuffer<T> {
	/* Adds a new element to the buffer*/
	public void add(T sample);
	
	/* Returns a snapshot copy of the buffer*/
	public T[] getSnapshot();

}
