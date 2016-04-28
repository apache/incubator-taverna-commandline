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
import java.util.List;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Test runner for running workflows.
 *
 */
public class WorkflowTestRunner extends BlockJUnit4ClassRunner {
	private File workflow;

	public WorkflowTestRunner(Class<?> type, File workflow) throws InitializationError {
		super(type);
		this.workflow = workflow;
	}

	@Override
	public Object createTest() throws Exception {
		return getTestClass().getOnlyConstructor().newInstance(workflow);
	}

	@Override
	protected String getName() {
		return String.format("[%s]", workflow.getName());
	}

	@Override
	protected String testName(final FrameworkMethod method) {
		return String.format("%s[%s]", method.getName(), workflow.getName());
	}

	@Override
	protected void validateConstructor(List<Throwable> errors) {
		validateOnlyOneConstructor(errors);
	}

	@Override
	protected Statement classBlock(RunNotifier notifier) {
		return childrenInvoker(notifier);
	}

}
