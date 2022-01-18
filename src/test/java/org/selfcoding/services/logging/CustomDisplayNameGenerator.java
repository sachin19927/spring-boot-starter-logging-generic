package org.selfcoding.services.logging;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayNameGenerator;

public class CustomDisplayNameGenerator extends DisplayNameGenerator.Standard {

	@Override
	public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {

		return testMethod.getName().replaceAll("_", " ").replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase().
				replace("throws", "()")+".";
		
	}
}
