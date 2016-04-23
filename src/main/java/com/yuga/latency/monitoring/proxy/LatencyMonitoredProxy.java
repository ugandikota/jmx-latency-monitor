package com.yuga.latency.monitoring.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.Attribute;
import javax.management.AttributeChangeNotification;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.ImmutableDescriptor;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ReflectionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.MBeanExportException;

import com.yuga.latency.monitoring.proxy.naming.AttributeNamingStrategy;
import com.yuga.latency.monitoring.utils.LatencyMonitor;

/**
 * Creates proxy that maintains latency information and exposes itself as a MBean.
 * 
 * 
 * @author Yuga Gandikota
 */
public class LatencyMonitoredProxy 
		extends NotificationBroadcasterSupport 
		implements 
			InvocationHandler, 
			DynamicMBean {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(LatencyMonitoredProxy.class);

	/* Source implementation that we have to build proxy for. */
	protected Object source;
	
	/* Proxy implementation created */
	private Object proxy;
	
	/* HashMap of latency monitors */
	private ConcurrentHashMap<String, LatencyMonitor> monitors = new ConcurrentHashMap<String, LatencyMonitor>();
	
	/* Sequence used while sending MBean change notifications  */
	private int notificationSequence = 1;
	
	/* Naming strategy being used for making attribute names*/
	private AttributeNamingStrategy namingStrategy;

	/* Annotation setting used at source*/
	private JMXLatencyMonitored annotation;

	private String timeUnitSufix = "";
	

	/**
	 * 
	 * Constructs LatencyMonitoredProxy instance. After successful instantiation, you can
	 * get the proxy implementation by calling <code>getProxy()</code> method. 
	 * 
	 * @param aBean source bean that we are going to create proxy for.
	 * @param aAnnotation annotation configuration as declared on the source.
	 * @throws SecurityException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws MBeanExportException
	 * @throws MalformedObjectNameException
	 */
	protected LatencyMonitoredProxy(Object aBean, JMXLatencyMonitored aAnnotation) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException 
	 {
		
		annotation = aAnnotation;
		source = aBean;
		
		Class<?>[] aBeanTypes = annotation.types();
		
		Constructor<?> cons = annotation.namingStrategyClass().getConstructor();
		namingStrategy = (AttributeNamingStrategy) cons.newInstance();		
		
		Object result = java.lang.reflect.Proxy.newProxyInstance(
				aBean.getClass().getClassLoader(), 
				aBeanTypes,
				this);
		
		if (annotation.addAllMonitorsAtStartup() ) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Adding monitors at startup. bean:{}", annotation.beanName());
			}
			
			for (Class<?> iface : aBeanTypes) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Adding monitors for all methods. bean:{}, class:{}", annotation.beanName(), iface.getName());
				}
				//look at all the methods as part of the interface
				Method[] allMethods = iface.getDeclaredMethods();
				for(Method method : allMethods) {
					String key  = createKey(source, annotation.types(), iface, method);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Adding monitor for method:"+method.toString());
					}
					monitors.putIfAbsent(key,  new LatencyMonitor(annotation.sampleSize(), annotation.units()));
				}
			}
		}
			
		proxy = result;
		
		timeUnitSufix = " " +annotation.units().toString();
	}
	
	/* Method to create key given the details. In turn uses the naming strategy implementation to create key */
	private String createKey(Object aSource, Class<?>[] aAllTypes, Class<?> aType, Method aMethod) {
		return namingStrategy.getAttributeName(aSource, aAllTypes, aType, aMethod);
	}

	/* Gets current monitor allocted for the given key, if there is nothing allocated yet, it will allocate new monitor */
	private LatencyMonitor getMonitor(String aKey) {
		LatencyMonitor monitor = monitors.get(aKey);
		if (monitor == null) {
			monitor = new LatencyMonitor(annotation.sampleSize(), annotation.units());
			LatencyMonitor existing = monitors.putIfAbsent(aKey, monitor);
			if ((existing != monitor) && (existing != null)) {
				monitor = existing;
				if ( ! annotation.addAllMonitorsAtStartup()) {
					sendNotification(aKey, monitor);
				}
			}
		}
		return monitor;
	}

	/* Sends change notification to notify clients of changes. */
	private void sendNotification(String aKey, LatencyMonitor aMonitor) {
		notificationSequence++;
		Notification n = new Notification("jmx.mbean.info.changed", source.getClass().getName(), notificationSequence );
		n.setUserData(getMBeanInfo());
		sendNotification(n);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(annotation.beanName()+":: Sending notification "+n.getMessage()+", seq:"+notificationSequence);
		}
	}

	/**
	 * Returns latency value given attribute name. 
	 * @param aAttributeName
	 * @param suppressNotFoundException if false, throws AttributeNotFoundException if the attribute does not exist.
	 * @return latency value of the given attribute(method)
	 * @throws AttributeNotFoundException if the attribute does not exists and suppressNotFoundException is set to false.
	 */
	protected String getLatencyValue(String aAttributeName, boolean suppressNotFoundException) 
	throws AttributeNotFoundException {
		LatencyMonitor monitor = monitors.get(aAttributeName);
		String result = "";
		if (monitor != null) {
			result = monitor.getAverage() + timeUnitSufix;
		}
		else {
			if (!suppressNotFoundException) {
				throw new AttributeNotFoundException(aAttributeName+" is not a valid attribute");
			}
		}
		return result;		
	}

	/**
	 * Returns source implementation that we are building proxy for.
	 * @return source implementation.
	 */
	protected Object getSource() {
		return source;
	}

	/**
	 * Returns proxy implementation of the source implementation.
	 * 
	 * @return proxy implementation of the source.
	 */
	protected Object getProxy() {
		return proxy;
	}

	/**
	 * Part of the DynamicMBean interface. Returns value of the given attribute name.
	 * 
	 * @param aAttributeName name of the attribute to value of.
	 * @return value of the attribute.
	 * @throws AttributeNotFoundException if the attributeName is not found. 
	 */
	public Object getAttribute(String aAttributeName) throws AttributeNotFoundException,
			MBeanException, ReflectionException {
		return getLatencyValue(aAttributeName, /*suppressNotFoundException*/ false);
	}

	/**
	 * Part of the DynamicMBean interface. Returns value of the given attribute name.
	 * 
	 * @param aAttributeNames array of attribute names to return values of.
	 * @return AttributeList with values of the given attribute names.
	 */
	public AttributeList getAttributes(String[] aAttributeNames) {
		AttributeList attrList = new AttributeList();
		for(String attributeName : aAttributeNames) {
			String value = "";
			try {
				value = getLatencyValue(attributeName, /*suppresNotFoundExeption*/true);
			} catch (AttributeNotFoundException e) {
			}
			Attribute attr = new Attribute(attributeName, value);
			attrList.add(attr);
		}
		return attrList;
	}

	/**
	 * Part of the DynamicMBean interface. Returns necessary details of this MBean in the form of MBeanInfo. 
	 * .
	 * @return MBeanInfo with details about this MBean. 
	 */
	public MBeanInfo getMBeanInfo() {
		MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[monitors.size()];
		int i = 0;
		for(String key : monitors.keySet()) {
			Object o = monitors.get(key);
			if (o != null) {
				MBeanAttributeInfo attr = new MBeanAttributeInfo(key, String.class.getName(), 
																key, true, false, false);
				attrs[i] = attr;
				i++;
			}
		}
		return new MBeanInfo(annotation.beanName(), annotation.beanName(), 
									attrs, 
									new MBeanConstructorInfo[]{},
									new MBeanOperationInfo[]{},
									new MBeanNotificationInfo[]{
											new MBeanNotificationInfo(
												new String[] { AttributeChangeNotification.ATTRIBUTE_CHANGE },
												AttributeChangeNotification.class.getName(),
												"This notification is emitted when any attribute changes"
											),
											new MBeanNotificationInfo(
												new String[] { "jmx.mbean.info.changed" },
												Notification.class.getName(),
												"This notification is emitted to signal a JMX client to refresh the mbean info"
											)},
									new ImmutableDescriptor("immutableInfo=false", "infoTimeout=10000"));
	}

	/* We don't have to support any actions thru JMX.*/
	public Object invoke(String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException {
        throw new ReflectionException(new NoSuchMethodException(
                actionName + " is not supported"));
	}

	/* We don't have to support this action thru JMX. Our values are read only.*/
	public void setAttribute(Attribute aAttributeName) throws AttributeNotFoundException,
			InvalidAttributeValueException, MBeanException, ReflectionException {
        throw new ReflectionException(new NoSuchMethodException(
        		aAttributeName + " is read only."));
    }

	/* We don't have to support this action thru JMX. Our values are read only.*/
	public AttributeList setAttributes(AttributeList aAttributeList) {
        return new AttributeList();
    }

	/**
	 * Method invoked every time any of the methods in the proxy are invoked by the client.
	 */
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {

		Object result;
		long start = -1L;
		boolean success = false;
		try {
			start = System.nanoTime();
			result = method.invoke(source, args);
			success = true;
		} catch (Exception e) {
			throw new RuntimeException("unexpected invocation exception: " + e.getMessage());
		} finally {
			if (success) {
				long end = System.nanoTime();
				String key = createKey(source, annotation.types(), method.getDeclaringClass(), method);
				LatencyMonitor monitor = getMonitor(key);
				long duration = end - start;
				monitor.addSample(duration);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Adding new time sample. bean:{}, methodKey:{}, time:{}", annotation.beanName(), key, duration);
				}
			}
		}
		return result;
	}
}
