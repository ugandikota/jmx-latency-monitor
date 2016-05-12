package com.github.yuga.gandikota.latency.monitoring.proxy;

import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.stereotype.Component;

/**
 * Class that implements necessary call backs required for integration into to Spring framework to 
 * create proxy, and register with MBean server.
 * 
 * @author Yuga Gandikota
 */
@Component
public class LatencyMonitoredProxyFactory 
implements 
	ApplicationContextAware,
	BeanPostProcessor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LatencyMonitoredProxy.class);

	/* ApplicationContext that will be passed by Spring framework*/
	private ApplicationContext applicationContext;

	/**
	 * Callback method part of the BeanPostProcessor interface. Returns the same bean instance as is.
	 * 
	 * @param bean bean instance created by spring.
	 * @param beanName name of the bean.
	 * @return returns the same bean as is.
	 * @throws BeanException 
	 */
	public Object postProcessBeforeInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	/**
	 * Callback method part of the BeanPostProcessor interface. 
	 * Creates and returns the proxy interface.
	 * 
	 * @param bean bean instance created by spring.
	 * @param beanName name of the bean.
	 * @return returns the same bean as is.
	 * @throws BeanException 
	 */
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		Object result = bean;
		
		final Class<? extends Object> beanClass = bean.getClass();
		JMXLatencyMonitored annotation = beanClass.getAnnotation(JMXLatencyMonitored.class);
		if (annotation != null) {
			try {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("creating proxy for beanName:{}, JMXName:{}'"+beanName, annotation.beanName());
				}
				LatencyMonitoredProxy proxy = new LatencyMonitoredProxy(bean, annotation);
				result = proxy.getProxy();
				
				MBeanExporter exporter = (MBeanExporter) applicationContext.getBean(MBeanExporter.class);
				exporter.registerManagedResource(proxy, new ObjectName(annotation.beanName()));
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("registered MBean for beanName:'"+beanName);
				}
			}
			catch (Exception e) {
				throw new BeanInitializationException(e.getMessage(), e);
			}
		}
		
		return result;
	}

	/**
	 * Callback method part of the ApplicationContextAware interface. 
	 * Used to get (and save) ApplicationContext from spring.
	 * 
	 * @param applicationContext ApplicationContext instance passed by Spring framework.
	 * @throws BeansException 
	 */
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("ApplicationContext is set:"+applicationContext.toString());
		}
		this.applicationContext = applicationContext;
	}
}
