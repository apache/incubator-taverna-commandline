/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
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
package net.sf.taverna.t2.commandline;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import net.sf.taverna.t2.commandline.data.DatabaseConfigurationHandler;
import net.sf.taverna.t2.commandline.data.InputsHandler;
import net.sf.taverna.t2.commandline.data.SaveResultsHandler;
import net.sf.taverna.t2.commandline.exceptions.DatabaseConfigurationException;
import net.sf.taverna.t2.commandline.exceptions.InputMismatchException;
import net.sf.taverna.t2.commandline.exceptions.InvalidOptionException;
import net.sf.taverna.t2.commandline.exceptions.OpenDataflowException;
import net.sf.taverna.t2.commandline.exceptions.ReadInputException;
import net.sf.taverna.t2.commandline.options.CommandLineOptions;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.RollingFileAppender;
import org.purl.wf4ever.robundle.Bundle;

import uk.org.taverna.configuration.database.DatabaseConfiguration;
import uk.org.taverna.configuration.database.DatabaseManager;
import uk.org.taverna.databundle.DataBundles;
import uk.org.taverna.platform.execution.api.ExecutionEnvironment;
import uk.org.taverna.platform.execution.api.InvalidExecutionIdException;
import uk.org.taverna.platform.execution.api.InvalidWorkflowException;
import uk.org.taverna.platform.report.State;
import uk.org.taverna.platform.report.WorkflowReport;
import uk.org.taverna.platform.run.api.InvalidRunIdException;
import uk.org.taverna.platform.run.api.RunProfile;
import uk.org.taverna.platform.run.api.RunProfileException;
import uk.org.taverna.platform.run.api.RunService;
import uk.org.taverna.platform.run.api.RunStateException;
import uk.org.taverna.scufl2.api.common.NamedSet;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Workflow;
import uk.org.taverna.scufl2.api.io.ReaderException;
import uk.org.taverna.scufl2.api.io.WorkflowBundleIO;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;
import uk.org.taverna.scufl2.api.port.OutputWorkflowPort;
import uk.org.taverna.scufl2.validation.ValidationException;
import uk.org.taverna.scufl2.validation.correctness.CorrectnessValidator;
import uk.org.taverna.scufl2.validation.correctness.ReportCorrectnessValidationListener;
import uk.org.taverna.scufl2.validation.structural.ReportStructuralValidationListener;
import uk.org.taverna.scufl2.validation.structural.StructuralValidator;

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
					credentialManager.setConfigurationDirectoryPath(new File(
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
						// Ignore
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
