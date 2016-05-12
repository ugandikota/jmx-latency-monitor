package com.github.yuga.gandikota.latency.monitoring.proxy.naming;

import java.lang.reflect.Method;

public class SimpleAttributeNamingStrategy implements AttributeNamingStrategy {

	public String getAttributeName(Object source, Class<?>[] allTypes, Class<?> type, Method method) {
		return method.getName();
	}
}
