# jmx-monitor

This project aims at providing a non-invasive simple to use annotation based framework that builds a run-time proxy that keeps track of latency information of the interface methods of the candidate class.

When you add following annotation to a class, this framework will do the following:
* builds a proxy instance that maintains latency information for each method in the interface (TestInterface as shown in the example) that this proxy is being added for.
* calculates and keeps track of latency info in real-time.
* injects proxy bean instead of the bean of type TestImpl, in all the places where the original bean is being injected into
* registers a MBean with MBeanServer and exposes entries that give latency information 
```
@JMXMonitored(beanName="TestInterface1:name=TestImpl1", //name used for MBean as it shows in the JMX console
			types={TestInterface1.class},     		   //array of interfaces to build proxy for.		
			addAllMonitorsAtStartup=true,              //if true, adds latency monitors for all the methods at startup.
			sampleSize=100, 						   //number of samples to use to compute running avg., default 100
			units=TimeUnit.MILLISECONDS,			   //time units to be used, default TimeUnit.MILLISECONDS	
			namingStrategyClass=MyCustomAttributeNamingStrategy.class) //default DefaultAttributeNamingStrategy.class 	 	
public class TestImpl1 implements TestInterface1{

}
 ```  

Refer to comments in the file about how to use this.

[`/src/main/java/com/github/yuga/gandikota/latency/monitoring/proxy/JMXLatencyMonitored.java`](https://github.com/yuga-gandikota/jmx-latency-monitor/blob/master/src/main/java/com/github/yuga/gandikota/latency/monitoring/proxy/JMXLatencyMonitored.java)
