/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.taverna.commandline;

import java.io.File;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

/**
 * Test suite for running workflows specified by a method annotated by &#064;Workflows.
 */
public class WorkflowTestSuite extends Suite {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface Workflows {}

	private ArrayList<Runner> runners = new ArrayList<Runner>();

	public WorkflowTestSuite(Class<?> klass) throws Throwable {
		super(klass, Collections.<Runner>emptyList());
		for (File workflow : getWorkflows(getTestClass())) {
			runners.add(new WorkflowTestRunner(getTestClass().getJavaClass(), workflow));
		}
	}

	@Override
	protected List<Runner> getChildren() {
		return runners;
	}

	@SuppressWarnings("unchecked")
	private List<File> getWorkflows(TestClass klass) throws Throwable {
		return (List<File>) getWorkflowsMethod(klass).invokeExplosively(null);
	}

	public FrameworkMethod getWorkflowsMethod(TestClass testClass) throws Exception {
		List<FrameworkMethod> methods = testClass.getAnnotatedMethods(Workflows.class);
		for (FrameworkMethod method : methods) {
			int modifiers = method.getMethod().getModifiers();
			if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
				return method;
			}
		}
		throw new Exception("No public static Workflows annotated method on class " + testClass.getName());
	}

}
