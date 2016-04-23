package com.yuga.latency.monitoring.utils;

import java.lang.reflect.Array;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple implementation of circular buffer. Synchronized around the buffer to make it thread safe.
 * 
 * @author Yuga Gandikota
 */
public class SimpleCircularBuffer<T> {
	
	/* backend structure for the circular buffer*/ 
	private T[] buffer;
	
	/* type of buffer element. used while initializing buffer*/
	private Class<T> type;
	
	/* index where the next element goes*/ 
	private int index = 0;
	
	/**
	 * Constructs a simple ring buffer with the given size.
	 * 
	 * @param bufferSize size of the circular buffer.
	 */
	SimpleCircularBuffer(Class<T> aType, int bufferSize) {
		buffer = (T[]) Array.newInstance(aType, bufferSize);
		type = aType;
	}
	
	/* Adds a new element to the buffer*/
	public void add(T sample) {
		synchronized(buffer) {
			buffer[(int)(index % buffer.length)] = sample;
			index++;
		}
	}
	
	/* Returns a snapshot copy of the buffer*/
	public T[] getSnapshot() {
		T[] snapshot = (T[]) Array.newInstance(type, buffer.length);
		
		synchronized(buffer) {
			System.arraycopy(buffer, 0, snapshot, 0, buffer.length);
		}
		
		return snapshot;
	}
	
	public int size() {
		return buffer.length;
	}
	
	
	public static void main(String[] args) {
		SimpleCircularBuffer<Long> buffer = new SimpleCircularBuffer<Long>(Long.class, 10);
		buffer.add(123456L);
		Long[] snapshot = buffer.getSnapshot();
		long total = 0;
		for(Long l : snapshot) {
			if (l != null) {
				total += l;
			}
		}
		System.out.println("total:"+total);
	}
}
