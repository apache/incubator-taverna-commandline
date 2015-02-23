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
import java.util.List;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Test runner for running workflows.
 *
 * @author David Withers
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
