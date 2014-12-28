/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package uk.org.taverna.commandline;

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
 *
 * @author David Withers
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
