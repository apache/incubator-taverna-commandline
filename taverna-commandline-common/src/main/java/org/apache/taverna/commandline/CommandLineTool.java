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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.taverna.commandline.data.DatabaseConfigurationHandler;
import org.apache.taverna.commandline.data.InputsHandler;
import org.apache.taverna.commandline.data.SaveResultsHandler;
import org.apache.taverna.commandline.exceptions.DatabaseConfigurationException;
import org.apache.taverna.commandline.exceptions.InputMismatchException;
import org.apache.taverna.commandline.exceptions.InvalidOptionException;
import org.apache.taverna.commandline.exceptions.OpenDataflowException;
import org.apache.taverna.commandline.exceptions.ReadInputException;
import org.apache.taverna.commandline.options.CommandLineOptions;
import org.apache.taverna.security.credentialmanager.CMException;
import org.apache.taverna.security.credentialmanager.CredentialManager;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.RollingFileAppender;
import org.apache.taverna.databundle.DataBundles;
import org.apache.taverna.robundle.Bundle;
import org.apache.taverna.scufl2.api.common.NamedSet;
import org.apache.taverna.scufl2.api.container.WorkflowBundle;
import org.apache.taverna.scufl2.api.core.Workflow;
import org.apache.taverna.scufl2.api.io.ReaderException;
import org.apache.taverna.scufl2.api.io.WorkflowBundleIO;
import org.apache.taverna.scufl2.api.port.InputWorkflowPort;
import org.apache.taverna.scufl2.api.port.OutputWorkflowPort;
import org.apache.taverna.scufl2.validation.ValidationException;
import org.apache.taverna.scufl2.validation.correctness.CorrectnessValidator;
import org.apache.taverna.scufl2.validation.correctness.ReportCorrectnessValidationListener;
import org.apache.taverna.scufl2.validation.structural.ReportStructuralValidationListener;
import org.apache.taverna.scufl2.validation.structural.StructuralValidator;

import org.apache.taverna.configuration.database.DatabaseConfiguration;
import org.apache.taverna.configuration.database.DatabaseManager;
import org.apache.taverna.platform.execution.api.ExecutionEnvironment;
import org.apache.taverna.platform.execution.api.InvalidExecutionIdException;
import org.apache.taverna.platform.execution.api.InvalidWorkflowException;
import org.apache.taverna.platform.report.State;
import org.apache.taverna.platform.report.WorkflowReport;
import org.apache.taverna.platform.run.api.InvalidRunIdException;
import org.apache.taverna.platform.run.api.RunProfile;
import org.apache.taverna.platform.run.api.RunProfileException;
import org.apache.taverna.platform.run.api.RunService;
import org.apache.taverna.platform.run.api.RunStateException;

/**
 * A utility class that wraps the process of executing a workflow, allowing workflows to be easily
 * executed independently of the GUI.
 *
 * @author Stuart Owen
 * @author Alex Nenadic
 */
public class CommandLineTool {
	private static boolean BOOTSTRAP_LOGGING = false;
	private static Logger logger = Logger.getLogger(CommandLineTool.class);

	private RunService runService;
	private CredentialManager credentialManager;
	private CommandLineOptions commandLineOptions;
	private WorkflowBundle workflowBundle;
	private WorkflowBundleIO workflowBundleIO;
	private DatabaseConfiguration databaseConfiguration;
	private DatabaseManager databaseManager;

	public void run() {
		try {
			if (BOOTSTRAP_LOGGING)
				initialiseLogging();
			int result = setupAndExecute();
			System.exit(result);
		} catch (InvalidOptionException | IOException | ReadInputException
				| InvalidRunIdException | RunStateException
				| InvalidExecutionIdException | OpenDataflowException
				| RunProfileException e) {
			error(e.getMessage());
		} catch (CMException e) {
			error("There was an error initializing Taverna's SSLSocketFactory from Credential Manager. "
					+ e.getMessage());
		} catch (ReaderException e) {
			error("There was an error reading the workflow: " + e.getMessage());
		} catch (ValidationException e) {
			error("There was an error validating the workflow: " + e.getMessage());
		} catch (InvalidWorkflowException e) {
			error("There was an error validating the workflow: " + e.getMessage());
		} catch (DatabaseConfigurationException e) {
			error("There was an error configuring the database: " + e.getMessage());
		}
		System.exit(1);
	}

	private void initialiseLogging() {
		LogManager.resetConfiguration();

		if (System.getProperty("log4j.configuration") == null) {
			try {
				PropertyConfigurator.configure(CommandLineTool.class.getClassLoader()
						.getResource("cl-log4j.properties").toURI().toURL());
			} catch (MalformedURLException e) {
				logger.error("There was a serious error reading the default logging configuration",
						e);
			} catch (URISyntaxException e) {
				logger.error("There was a serious error reading the default logging configuration",
						e);
			}

		} else {
			PropertyConfigurator.configure(System.getProperty("log4j.configuration"));
		}

		if (commandLineOptions.hasLogFile()) {
			RollingFileAppender appender;
			try {

				PatternLayout layout = new PatternLayout("%-5p %d{ISO8601} (%c:%L) - %m%n");
				appender = new RollingFileAppender(layout, commandLineOptions.getLogFile());
				appender.setMaxFileSize("1MB");
				appender.setEncoding("UTF-8");
				appender.setMaxBackupIndex(4);
				// Let root logger decide level
				appender.setThreshold(Level.ALL);
				LogManager.getRootLogger().addAppender(appender);
			} catch (IOException e) {
				System.err.println("Could not log to " + commandLineOptions.getLogFile());
			}
		}
	}

	public int setupAndExecute() throws InputMismatchException, InvalidOptionException,
			CMException, OpenDataflowException, ReaderException, IOException, ValidationException,
			ReadInputException, InvalidWorkflowException, RunProfileException,
			InvalidRunIdException, RunStateException, InvalidExecutionIdException, DatabaseConfigurationException {

		if (!commandLineOptions.askedForHelp()) {
			 setupDatabase(commandLineOptions);

			if (commandLineOptions.getWorkflow() != null) {
				
				/* Set context class loader to us, 
				 * so that things such as JSON-LD caching of
				 * robundle works.
				 */
				
				Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
				
				
				/*
				 * Initialise Credential Manager and SSL stuff quite early as
				 * parsing and validating the workflow may require it
				 */
				String credentialManagerDirPath = commandLineOptions.getCredentialManagerDir();

				/*
				 * If credentialManagerDirPath is null, the Credential Manager
				 * will be initialized from the default location in
				 * <TAVERNA_HOME>/security somewhere inside user's home
				 * directory. This should not be used when running command line
				 * tool on a server and the Credential Manager dir path should
				 * always be passed in as we do not want to store the security
				 * files in user's home directory on the server (we do not even
				 * know which user the command line tool will be running as).
				 */
				if (credentialManagerDirPath != null) {
					credentialManager.setConfigurationDirectoryPath(Paths.get(
							credentialManagerDirPath));
				}

				// Initialise the SSL stuff - set the SSLSocketFactory
				// to use Taverna's Keystore and Truststore.
				credentialManager.initializeSSL();

				URL workflowURL = readWorkflowURL(commandLineOptions.getWorkflow());

				workflowBundle = workflowBundleIO.readBundle(workflowURL, null);

				logger.debug("Read the wf bundle");

				validateWorkflowBundle(workflowBundle);
				logger.debug("Validated the wf bundle");


				Set<ExecutionEnvironment> executionEnvironments = runService
						.getExecutionEnvironments();

				ExecutionEnvironment executionEnvironment = null;

				/*
				 * Find the right execution environment, e.g. local execution
				 * with the correct reference service based on command line
				 * options
				 */
				while (executionEnvironments.iterator().hasNext()) {
					// TODO Choose the right one
					// take the fist one for now
					executionEnvironment = executionEnvironments.iterator().next();
					break;
				}

				logger.debug("Got the execution environment");

				InputsHandler inputsHandler = new InputsHandler();
				Map<String, InputWorkflowPort> portMap = new HashMap<String, InputWorkflowPort>();

				Workflow workflow = workflowBundle.getMainWorkflow();

				for (InputWorkflowPort port : workflow.getInputPorts()) {
					portMap.put(port.getName(), port);
				}
				inputsHandler.checkProvidedInputs(portMap, commandLineOptions);
				logger.debug("Checked inputs");

				Bundle inputs = inputsHandler.registerInputs(portMap, commandLineOptions, null);
				logger.debug("Registered inputs");

				RunProfile runProfile = new RunProfile(executionEnvironment, workflowBundle, inputs);

				String runId = runService.createRun(runProfile);

				runService.start(runId);
				logger.debug("Started wf run");

				WorkflowReport report = runService.getWorkflowReport(runId);

				while (!workflowFinished(report)) {
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						System.err.println("Interrupted while waiting for workflow to finish");
						return 1;
					}
				}

				NamedSet<OutputWorkflowPort> workflowOutputPorts = workflow.getOutputPorts();
				if (!workflowOutputPorts.isEmpty()) {
					File outputDir = null;

					if (commandLineOptions.saveResultsToDirectory()) {
						outputDir = determineOutputDir(commandLineOptions, workflowBundle.getName());
						outputDir.mkdirs();
					}

					Path outputs = DataBundles.getOutputs(runService.getDataBundle(runId));

					if (outputDir != null) {
						SaveResultsHandler saveResultsHandler = new SaveResultsHandler(outputDir);

						for (OutputWorkflowPort outputWorkflowPort : workflowOutputPorts) {
							String workflowOutputPortName = outputWorkflowPort.getName();
							Path output = DataBundles.getPort(outputs, workflowOutputPortName);
							if (!DataBundles.isMissing(output)) {
								saveResultsHandler.saveResultsForPort(workflowOutputPortName, output);
							}
						}
					}
				}
				if (commandLineOptions.saveResultsToBundle() != null) {
					Path bundlePath = Paths.get(commandLineOptions.saveResultsToBundle());
					DataBundles.closeAndSaveBundle(runService.getDataBundle(runId), bundlePath);
					System.out.println("Workflow Run Bundle saved to: " + bundlePath.toAbsolutePath());
				} else {
					System.out.println("Workflow Run Bundle: " + runService.getDataBundle(runId).getSource());
					// For debugging we'll leave it in /tmp for now
					runService.getDataBundle(runId).setDeleteOnClose(false);
					DataBundles.closeBundle(runService.getDataBundle(runId));
				}

				if (report.getState().equals(State.FAILED)) {
					System.out.println("Workflow failed - see report below.");
					System.out.println(report);
				} else if (report.getState().equals(State.COMPLETED)) {
					System.out.println("Workflow completed.");
				}

			}
		} else {
			commandLineOptions.displayHelp();
		}

		// wait until user hits CTRL-C before exiting
		if (commandLineOptions.getStartDatabaseOnly()) {
			// FIXME: need to do this more gracefully.
			while (true) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					return 0;
				}
			}
		}

		return 0;
	}

	private boolean workflowFinished(WorkflowReport report) {
		State state = report.getState();
		if (state == State.CANCELLED || state == State.COMPLETED || state == State.FAILED) {
			return true;
		}
		return false;
	}

	protected void validateWorkflowBundle(WorkflowBundle workflowBundle) throws ValidationException {
		CorrectnessValidator cv = new CorrectnessValidator();
		ReportCorrectnessValidationListener rcvl = new ReportCorrectnessValidationListener();
		cv.checkCorrectness(workflowBundle, true, rcvl);
		if (rcvl.detectedProblems()) {
			throw rcvl.getException();
		}

		StructuralValidator sv = new StructuralValidator();
		ReportStructuralValidationListener rsvl = new ReportStructuralValidationListener();
		sv.checkStructure(workflowBundle, rsvl);
		if (rsvl.detectedProblems()) {
			throw rcvl.getException();
		}
	}

	private void setupDatabase(CommandLineOptions options)
			throws DatabaseConfigurationException {
		DatabaseConfigurationHandler dbHandler = new DatabaseConfigurationHandler(
				options, databaseConfiguration, databaseManager);
		dbHandler.configureDatabase();
		try {
			if (!options.isInMemory())
				dbHandler.testDatabaseConnection();
		} catch (NamingException e) {
			throw new DatabaseConfigurationException(
					"There was an error trying to setup the database datasource: "
							+ e.getMessage(), e);
		} catch (SQLException e) {
			if (options.isClientServer())
				throw new DatabaseConfigurationException(
						"There was an error whilst making a test database connection. If running with -clientserver you should check that a server is running (check -startdb or -dbproperties)",
						e);
			if (options.isEmbedded())
				throw new DatabaseConfigurationException(
						"There was an error whilst making a test database connection. If running with -embedded you should make sure that another process isn't using the database, or a server running through -startdb",
						e);
		}
	}

	private File determineOutputDir(CommandLineOptions options, String dataflowName) {
		File result = new File(dataflowName + "_output");
		int x = 1;
		while (result.exists()) {
			result = new File(dataflowName + "_output_" + x);
			x++;
		}
		System.out.println("Outputs will be saved to the directory: "
				+ result.getAbsolutePath());
		return result;
	}

	protected void error(String msg) {
		System.err.println(msg);
	}

	private URL readWorkflowURL(String workflowOption) throws OpenDataflowException {
		try {
			return new URL(new URL("file:"), workflowOption);
		} catch (MalformedURLException e) {
			throw new OpenDataflowException("The was an error processing the URL to the workflow: "
					+ e.getMessage(), e);
		}
	}

	public void setCommandLineOptions(CommandLineOptions commandLineOptions){
		this.commandLineOptions = commandLineOptions;
	}

	public void setRunService(RunService runService) {
		this.runService = runService;
	}

	public void setCredentialManager(CredentialManager credentialManager) {
		this.credentialManager = credentialManager;
	}

	public void setWorkflowBundleIO(WorkflowBundleIO workflowBundleIO) {
		this.workflowBundleIO = workflowBundleIO;
	}

	/**
	 * Sets the databaseConfiguration.
	 *
	 * @param databaseConfiguration the new value of databaseConfiguration
	 */
	public void setDatabaseConfiguration(DatabaseConfiguration databaseConfiguration) {
		this.databaseConfiguration = databaseConfiguration;
	}

	/**
	 * Sets the databaseManager.
	 *
	 * @param databaseManager the new value of databaseManager
	 */
	public void setDatabaseManager(DatabaseManager databaseManager) {
		this.databaseManager = databaseManager;
	}

}
