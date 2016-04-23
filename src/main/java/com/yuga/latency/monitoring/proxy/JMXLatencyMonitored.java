package com.yuga.latency.monitoring.proxy;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.yuga.latency.monitoring.proxy.naming.DefaultAttributeNamingStrategy;

/**
 * Annotation to be used to mark an implementation to be proxy'd and exposed as an MBean  
 *
 * <pre>
 * @JMXMonitored(beanName="TestInterface1:name=TestImpl1", //name used for MBean
 * 				types={TestInterface1.class},     		   //array of interfaces to build proxy for.		
 * 				addAllMonitorsAtStartup=true,              //if true, adds latency monitors for all the methods at startup.
 * 				sampleSize=100, 						   //number of samples to use to compute running avg., default 100
 * 				units=TimeUnit.MILLISECONDS,			   //time units to be used, default TimeUnit.MILLISECONDS	
 * 				namingStrategyClass=MyCustomAttributeNamingStrategy.class) //default DefaultAttributeNamingStrategy.class 	 	
 * public class TestImpl1 implements TestInterface1{
 * ...
 * }
 * </pre>
 * 
 * @author Yuga Gandikota
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface JMXLatencyMonitored {
	
	/* name used while registering MBean with the MBean server*/
	String beanName();
	
	/* array of Interfaces that proxy has to be created for*/
	Class<?>[] types();
	
	/**
	 * Determines whether attributes corresponding to all the methods are created
	 * at startup or not. Default is true. If set to false, attributes are created
	 * as an when interface methods are invoked. If a certain method is not invoked
	 * at the time JMX client connects, then the attribute corresponding to that method
	 * will not show up in the client interface. If that method is invoked after the client 
	 * is connected, it does not show up in the client automatically. You will have to 
	 * re-connect to refresh the attribute list.
	 */
	boolean addAllMonitorsAtStartup() default true;
	
	/* Sample size to use for calculating running average. */
	int sampleSize() default 100;
	
	/* time units to be used */
	TimeUnit units() default TimeUnit.MILLISECONDS;
	Class<?> namingStrategyClass() default DefaultAttributeNamingStrategy.class;
}
