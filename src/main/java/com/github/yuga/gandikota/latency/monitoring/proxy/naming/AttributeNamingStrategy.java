package com.github.yuga.gandikota.latency.monitoring.proxy.naming;

import java.lang.reflect.Method;


/**
 * Interface that you have to implement if you wish to change the naming format of the attributes 
 * in the JMX client (JConsole). You have access to various info to help you decide on naming convention.
 *  
 * ex:
 * 1) If the Annotated class implements multiple interfaces then you may want to add information about the 
 *    interface also to the attribute name.
 *    <code>InterfaceName::methodName(name.package.Param1, name.package.Param2)</code>
 *
 *     
 * 2) If the interface has several methods with the same name (but with different parameters), you may 
 *    want to add signature information to the attribute to avoid confusion.
 *    <code>methodName(name.package.Param1, name.package.Param2)</code>
 * 
 * 
 * @author Yuga Gandikota
 * @see DefaultAttributeNamingStrategy
 */
public interface AttributeNamingStrategy {
	/**
	 * @param source implementation class that the attribute is being built for.
	 * @param allTypes Array of interfaces that we are building proxy/monitoring for. You can use this
	 *        information to decide whether to put the interface name in the attribute name or not.
	 *        if there is only one interface being monitored then, adding interface name would be redundant. 
	 * @param type current interface that the attribute is being built for.
	 * @param method current method that the attribute is being built for.
	 * 
	 * @return name of the attribute to be shown in the JConsole for the the method and type combination. 
	 */
	public String getAttributeName(Object source, Class<?>[] allTypes, Class<?> type, Method method);
}
