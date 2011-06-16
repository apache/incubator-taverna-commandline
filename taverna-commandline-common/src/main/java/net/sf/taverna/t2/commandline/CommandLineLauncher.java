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

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.raven.launcher.Launchable;
import net.sf.taverna.t2.commandline.data.DatabaseConfigurationHandler;
import net.sf.taverna.t2.commandline.data.InputsHandler;
import net.sf.taverna.t2.commandline.data.SaveResultsHandler;
import net.sf.taverna.t2.commandline.exceptions.DatabaseConfigurationException;
import net.sf.taverna.t2.commandline.exceptions.InvalidOptionException;
import net.sf.taverna.t2.commandline.exceptions.OpenDataflowException;
import net.sf.taverna.t2.commandline.exceptions.ReadInputException;
import net.sf.taverna.t2.commandline.options.CommandLineOptions;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade.State;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactory;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactoryRegistry;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.InvalidDataflowException;
import net.sf.taverna.t2.workflowmodel.serialization.DeserializationException;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerRegistry;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.RollingFileAppender;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.context.ApplicationContext;

/**
 * A utility class that wraps the process of executing a workflow, allowing
 * workflows to be easily executed independently of the GUI.
 *
 * @author Stuart Owen
 */

public class CommandLineLauncher implements Launchable {

	private static Logger logger = Logger.getLogger(CommandLineLauncher.class);
	
	/**
	 * Main method, purely for development and debugging purposes. Full
	 * execution of workflows will not work through this method.
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) {
		new CommandLineLauncher().launch(args);
	}

	public int launch(String[] args) {

		try {
			CommandLineOptions options = new CommandLineOptions(args);
			initialiseLogging(options);
			int result = setupAndExecute(args,options);
			System.exit(result);
			return result;
		} catch (EditException e) {
			error("There was an error opening the workflow: " + e.getMessage());
		} catch (DeserializationException e) {
			error("There was an error opening the workflow: " + e.getMessage());
		} catch (InvalidDataflowException e) {
			error("There was an error validating the workflow: "
					+ e.getMessage());
		} catch (TokenOrderException e) {
			error("There was an error starting the workflow execution: "
					+ e.getMessage());
		} catch (InvalidOptionException e) {
			error(e.getMessage());
		} catch (ReadInputException e) {
			error(e.getMessage());
		} catch (OpenDataflowException e) {
			error(e.getMessage());
		} catch (DatabaseConfigurationException e) {
			error(e.getMessage());
		}
		catch (CMException e) {
			error("There was an error instantiating Credential Manager: " + e.getMessage());
		}
		// Should be unreachable
		System.exit(-1);
		return -1;
	}

	private void initialiseLogging(CommandLineOptions options) {
		LogManager.resetConfiguration();

		if (System.getProperty("log4j.configuration") == null) {
			try {
				PropertyConfigurator.configure(CommandLineLauncher.class
						.getClassLoader().getResource("cl-log4j.properties")
						.toURI().toURL());
			} catch (MalformedURLException e) {
				logger
						.error(
								"There was a serious error reading the default logging configuration",
								e);
			} catch (URISyntaxException e) {
				logger
						.error(
								"There was a serious error reading the default logging configuration",
								e);
			}

		} else {
			PropertyConfigurator.configure(System
					.getProperty("log4j.configuration"));
		}

		if (options.hasLogFile()) {
			RollingFileAppender appender;
			try {

				PatternLayout layout = new PatternLayout(
						"%-5p %d{ISO8601} (%c:%L) - %m%n");
				appender = new RollingFileAppender(layout, options.getLogFile());
				appender.setMaxFileSize("1MB");
				appender.setEncoding("UTF-8");
				appender.setMaxBackupIndex(4);
				// Let root logger decide level
				appender.setThreshold(Level.ALL);
				LogManager.getRootLogger().addAppender(appender);
			} catch (IOException e) {
				System.err.println("Could not log to " + options.getLogFile());
			}
		}
	}

	public int setupAndExecute(String[] args,CommandLineOptions options) throws InvalidOptionException,
			EditException, DeserializationException, InvalidDataflowException,
			TokenOrderException, ReadInputException, OpenDataflowException,
			DatabaseConfigurationException, CMException {


		if (!options.askedForHelp()) {
			setupDatabase(options);

			if (options.getWorkflow() != null) {
							
				// Initialise Credential Manager and SSL stuff quite early as parsing and
				// validating the workflow might require its services
				String credentialManagerDirPath = options.getCredentialManagerDir();
				String credentialManagerPassword = null;									
				if (options.hasOption(CommandLineOptions.CREDENTIAL_MANAGER_PASSWORD_OPTION)){ // if this parameter was used when launching the command line tool
					// Try to read the password from stdin (terminal or pipe)
					credentialManagerPassword = getCredentialManagerPasswordFromStdin();
				}
				else{ 	
					// Try to read the password from a special file located in 
					// Credential Manager directory (if the dir was not null)
					credentialManagerPassword = getCredentialManagerPasswordFromFile(options.getCredentialManagerDir());
				}
				if (credentialManagerPassword != null){
					// Initialise Credential Manager (Taverna's Keystore and Truststore) and 
					// SSL stuff - set the SSLSocketFactory to use Taverna's Keystore and Truststore.
					
					// If credentialManagerDirPath is null - initialize from the default location in <TAVERNA_HOME>/security somewhere 
					// inside user's home directory. This should not be used when running command 
					// line tool on a server and the Credential Manager dir path should always be 
					// passed in as we do not want to store the security files in user's home directory 
					// on the server (we do not even know which user the command line tool will be running as).

					//if (credentialManagerDirPath != null){
						CredentialManager.initialiseSSL(credentialManagerDirPath, credentialManagerPassword); // this can now handle situations when credentialManagerDirPath is null
					//}
					//else{
						// Initialize from the default location in <TAVERNA_HOME>/security somewhere 
						// inside user's home directory. This should not be used when running command 
						// line tool on a server and the Credential Manager dir path should always be 
						// passed in as we do not want to store the security files in user's home directory 
						// on the server (we do not even know which user the command line tool will be running as).
					//	CredentialManager.initialiseSSL(credentialManagerPassword);
					//}
				}
				else{
					logger.warn("No master password provided for Credential Manager.");
				}

				URL workflowURL = readWorkflowURL(options.getWorkflow());

				Dataflow dataflow = openDataflow(workflowURL);
				validateDataflow(dataflow);

				InvocationContext context = createInvocationContext();

				WorkflowInstanceFacade facade = compileFacade(dataflow, context);
				InputsHandler inputsHandler = new InputsHandler();
				Map<String, DataflowInputPort> portMap = new HashMap<String, DataflowInputPort>();

				for (DataflowInputPort port : dataflow.getInputPorts()) {
					portMap.put(port.getName(), port);
				}
				inputsHandler.checkProvidedInputs(portMap, options);
				Map<String, WorkflowDataToken> inputs = inputsHandler
						.registerInputs(portMap, options, context);

				CommandLineResultListener resultListener = addResultListener(
						facade, context, dataflow, options);

				executeWorkflow(facade, inputs, resultListener);
			}
		} else {
			options.displayHelp();
		}

		// wait until user hits CTRL-C before exiting
		if (options.getStartDatabaseOnly()) {
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

	protected void validateDataflow(Dataflow dataflow)
			throws InvalidDataflowException {
		// FIXME: this needs expanding upon to give more details info back to
		// the user
		// FIXME: added a getMessage to InvalidDataflowException may be good
		// place to do this.
		DataflowValidationReport report = dataflow.checkValidity();
		if (!report.isValid()) {
			throw new InvalidDataflowException(dataflow, report);
		}
	}

	protected void executeWorkflow(WorkflowInstanceFacade facade,
			Map<String, WorkflowDataToken> inputs,
			CommandLineResultListener resultListener)
			throws TokenOrderException {
		facade.fire();
		for (String inputName : inputs.keySet()) {
			WorkflowDataToken token = inputs.get(inputName);
			facade.pushData(token, inputName);
		}
		while (facade.getState().compareTo(State.completed) < 0) {
			// Test facade state, resultListener.isComplete() does not check wait for any
			// dangling processors not connected to an output port.

			//		while (!resultListener.isComplete()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger
						.warn(
								"Thread Interuption Exception whilst waiting for dataflow completion",
								e);
			}
		}
		resultListener.saveProvenance();
		resultListener.saveOutputDocument();
	}

	private void setupDatabase(CommandLineOptions options)
			throws DatabaseConfigurationException {
		DatabaseConfigurationHandler dbHandler = new DatabaseConfigurationHandler(
				options);
		dbHandler.configureDatabase();
		if (!options.isInMemory()) {
			try {
				dbHandler.testDatabaseConnection();
			} catch (NamingException e) {
				throw new DatabaseConfigurationException(
						"There was an error trying to setup the database datasource: "
								+ e.getMessage(), e);
			} catch (SQLException e) {
				if (options.isClientServer()) {
					throw new DatabaseConfigurationException(
							"There was an error whilst making a test database connection. If running with -clientserver you should check that a server is running (check -startdb or -dbproperties)",
							e);
				}
				if (options.isEmbedded()) {
					throw new DatabaseConfigurationException(
							"There was an error whilst making a test database connection. If running with -embedded you should make sure that another process isn't using the database, or a server running through -startdb",
							e);
				}
			}
		}

	}

	private InvocationContext createInvocationContext() {
		ReferenceService referenceService = createReferenceServiceBean();
		ProvenanceConnector connector = null;
		DataManagementConfiguration dbConfig = DataManagementConfiguration
				.getInstance();
		if (dbConfig.isProvenanceEnabled()) {
			String connectorType = dbConfig.getConnectorType();

			for (ProvenanceConnectorFactory factory : ProvenanceConnectorFactoryRegistry
					.getInstance().getInstances()) {
				if (connectorType.equalsIgnoreCase(factory.getConnectorType())) {
					connector = factory.getProvenanceConnector();
				}

			}
			if (connector != null) {
				connector.init();
			} else {
				error("Unable to initialise the provenance - the ProvenanceConnector cannot be found.");
			}
		}
		InvocationContext context = new CommandLineInvocationContext(
				referenceService, connector);
		if (connector != null){
			connector.setInvocationContext(context);
		}
		return context;
	}

	private File determineOutputDir(CommandLineOptions options,
			String dataflowName) {
		File result = null;
		if (options.getOutputDirectory() != null) {
			result = new File(options.getOutputDirectory());
			if (result.exists()) {
				error("The specified output directory '"
						+ options.getOutputDirectory() + "' already exists");
			}
		} else if (options.getOutputDocument() == null) {
			result = new File(dataflowName + "_output");
			int x = 1;
			while (result.exists()) {
				result = new File(dataflowName + "_output_" + x);
				x++;
			}
		}
		if (result != null) {
			System.out.println("Outputs will be saved to the directory: "
					+ result.getAbsolutePath());
		}
		return result;
	}

	protected void error(String msg) {
		System.err.println(msg);
		System.exit(-1);
	}

	private URL readWorkflowURL(String workflowOption)
			throws OpenDataflowException {
		URL url;
		try {
			url = new URL("file:");
			return new URL(url, workflowOption);
		} catch (MalformedURLException e) {
			throw new OpenDataflowException(
					"The was an error processing the URL to the workflow: "
							+ e.getMessage(), e);
		}
	}
	
	/**
	 * 
	 * @param cmDir
	 * @return Password for Credential Manager.
	 * @throws CMException
	 */
	private String getCredentialManagerPasswordFromFile(String cmDir) throws CMException{

		if (cmDir == null){
			return null;
		}
		File passwordFile = new File(cmDir, "password.txt");
		String password = null;
		BufferedReader buffReader = null;
		try {
			buffReader = new BufferedReader(new FileReader(passwordFile));
			password = buffReader.readLine();
		} catch (IOException ioe) {
			// For some reason the error of the exception thrown 
			// does not get printed from the Launcher so print it here as
			// well as it gives more clue as to what is going wrong.
			logger.error("There was an error reading the Credential Manager password from "
					+ passwordFile.toString() + ": " + ioe.getMessage(), ioe); 
			throw new CMException(
					"There was an error reading the Credential Manager password from "
							+ passwordFile.toString() + ": " + ioe.getMessage(), ioe);
		} finally {
			try {
				buffReader.close();
			} catch (Exception ioe1) {
				// Ignore
			}
		}
		return password;
	}

	private String getCredentialManagerPasswordFromStdin() throws CMException{
		
		String password = null;
        
		Console console = System.console();		

		if (console == null) { // password is being piped in, not entered in the terminal by user
			BufferedReader buffReader = null;
    		try {
    			buffReader = new BufferedReader(new InputStreamReader(System.in));
    			password = buffReader.readLine();
    		} 
    		catch (IOException ex) {
    			// For some reason the error of the exception thrown 
    			// does not get printed from the Launcher so print it here as
    			// well as it gives more clue as to what is going wrong.
    			logger.error("An error occured while trying to read Credential Manager's password the user piped in: "
						+ ex.getMessage(), ex); 
    			throw new CMException(
						"An error occured while trying to read Credential Manager's password the user piped in: "
								+ ex.getMessage(), ex);
				} 
    		finally {
    			try {
    				buffReader.close();
    			} catch (Exception ioe1) {
    				// Ignore
    			}
    		}	  
		}
		else{ // read the password from the terminal as entered by the user
			try {
				// Block until user enters password
				char passwordArray[] = console
						.readPassword("Password for Credential Manager: ");
				if (passwordArray != null) { // user did not abort input
					password = new String(passwordArray);
				} // else password will be null

			} catch (Exception ex) {
    			// For some reason the error of the exception thrown 
    			// does not get printed from the Launcher so print it here as
    			// well as it gives more clue as to what is going wrong.
				logger.error("An error occured while trying to read Credential Manager's password from the terminal: "
								+ ex.getMessage(), ex);
				throw new CMException(
						"An error occured while trying to read Credential Manager's password from the terminal: "
								+ ex.getMessage(), ex);
			}
		}
        return password;
	}
	
	private CommandLineResultListener addResultListener(
			WorkflowInstanceFacade facade, InvocationContext context,
			Dataflow dataflow, CommandLineOptions options) {
		File outputDir = null;
		File baclavaDoc = null;
		File janus = null;
		File janusDir = null;
		File opm = null;
		
		if (options.saveResultsToDirectory()) {
			outputDir = determineOutputDir(options, dataflow.getLocalName());
			janusDir = outputDir;
		}
		if (options.getOutputDocument() != null) {
			baclavaDoc = new File(options.getOutputDocument());
		}
		if (options.isJanus()) {
			if (options.getJanus() == null) {
				if (janusDir == null) {
					janusDir = determineOutputDir(options, dataflow.getLocalName());
				}
				janus = new File(janusDir, "provenance-janus.rdf");
			} else {
				janus = new File(options.getJanus());
			}
		}
		if (options.isOPM()) {
			if (options.getOPM() == null) {
				if (janusDir == null) {
					janusDir = determineOutputDir(options, dataflow.getLocalName());
				}
				opm = new File(janusDir, "provenance-opm.rdf");
			} else {
				opm = new File(options.getOPM());
			}
		}
		
		Map<String, Integer> outputPortNamesAndDepth = new HashMap<String, Integer>();
		for (DataflowOutputPort port : dataflow.getOutputPorts()) {
			outputPortNamesAndDepth.put(port.getName(), port.getDepth());
		}
		SaveResultsHandler resultsHandler = new SaveResultsHandler(
				outputPortNamesAndDepth, outputDir, baclavaDoc, janus, opm);
		CommandLineResultListener listener = new CommandLineResultListener(
				outputPortNamesAndDepth.size(), resultsHandler,
				outputDir != null, baclavaDoc != null, opm != null, janus != null, facade.getWorkflowRunId());
		facade.addResultListener(listener);
		return listener;

	}

	protected ReferenceService createReferenceServiceBean() {
		ApplicationContext appContext = new RavenAwareClassPathXmlApplicationContext(
				DataManagementConfiguration.getInstance().getDatabaseContext());
		return (ReferenceService) appContext
				.getBean("t2reference.service.referenceService");
	}

	protected WorkflowInstanceFacade compileFacade(Dataflow dataflow,
			InvocationContext context) throws InvalidDataflowException {
		Edits edits = EditsRegistry.getEdits();
		return edits.createWorkflowInstanceFacade(dataflow, context, "");
	}

	protected Dataflow openDataflow(URL workflowURL)
			throws DeserializationException, EditException,
			OpenDataflowException {
		XMLDeserializer deserializer = XMLDeserializerRegistry.getInstance()
				.getDeserializer();
		SAXBuilder builder = new SAXBuilder();
		Element el;
		try {
			InputStream stream = workflowURL.openStream();
			el = builder.build(stream).detachRootElement();
		} catch (JDOMException e) {
			throw new OpenDataflowException(
					"There was a problem processing the workflow XML: "
							+ e.getMessage(), e);
		} catch (IOException e) {
			throw new OpenDataflowException(
					"There was a problem reading the workflow file: "
							+ e.getMessage(), e);
		}
		return deserializer.deserializeDataflow(el);
	}

}
