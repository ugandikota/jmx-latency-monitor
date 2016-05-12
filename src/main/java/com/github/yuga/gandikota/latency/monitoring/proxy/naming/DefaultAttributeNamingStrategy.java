package com.github.yuga.gandikota.latency.monitoring.proxy.naming;

import java.lang.reflect.Method;

/**
 * Default naming strategy implementation used. Naming convention used is one of the following:
 * 1) If only one interface is being proxy'd..
 *    <code>methodName(name.package.Param1, name.package.Param2)</code>
 *    
 * 2) If multiple interfaces are being proxy'd..
 *    <code>InterfaceName::methodName(name.package.Param1, name.package.Param2)</code>
 * @see AttributeNamingStrategy
 *   
 * @author Yuga Gandikota
 */

public class DefaultAttributeNamingStrategy implements AttributeNamingStrategy {

	public String getAttributeName(Object source, Class<?>[] allTypes, Class<?> type, Method method) {
		StringBuffer sb = new StringBuffer();
		
		//add interface name only if there are multiple interfaces being proxies.
		if (allTypes != null && allTypes.length > 1) {
			sb.append(type.getSimpleName()+"::");
		}
		
		sb.append(method.getName());

		//if the interface has multiple methods with the same name, you may have to
		//use parameter info in the attribute name to avoid collisions.
		//if you know for sure that the name is unique, you can just use
		//method.getName() instead of the following block.
		sb.append("(");
		boolean first = true;
		for(Class<?> c : method.getParameterTypes()) {
			if (first) {
				first = false;
			}
			else {
				sb.append(",");
			}
			sb.append(c.getName());
		}
		sb.append(")");
		return sb.toString();		
	}
}
