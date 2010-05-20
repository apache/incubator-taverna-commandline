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
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.raven.launcher.Launchable;
import net.sf.taverna.t2.commandline.exceptions.DatabaseConfigurationException;
import net.sf.taverna.t2.commandline.exceptions.InvalidOptionException;
import net.sf.taverna.t2.commandline.exceptions.OpenDataflowException;
import net.sf.taverna.t2.commandline.exceptions.ReadInputException;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.TokenOrderException;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactory;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactoryRegistry;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;
import net.sf.taverna.t2.reference.ReferenceService;
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

import org.apache.log4j.Logger;
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
			return setupAndExecute(args);
		} catch (EditException e) {
			error("There was an error opening the workflow: " + e.getMessage());
		} catch (DeserializationException e) {
			error("There was an error opening the workflow: " + e.getMessage());
		} catch (InvalidDataflowException e) {			
			error("There was an error validating the workflow: " + e.getMessage());
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
		return 0;
	}

	public int setupAndExecute(String[] args) throws InvalidOptionException,
			EditException, DeserializationException, InvalidDataflowException,
			TokenOrderException, ReadInputException, OpenDataflowException,
			DatabaseConfigurationException {

		CommandLineOptions options = new CommandLineOptions(args);
		if (!options.askedForHelp()) {
			setupDatabase(options);

			if (options.getWorkflow() != null) {
				URL workflowURL = readWorkflowURL(options.getWorkflow());

				Dataflow dataflow = openDataflow(workflowURL);
				validateDataflow(dataflow);
				
				InvocationContext context = createInvocationContext();

				WorkflowInstanceFacade facade = compileFacade(dataflow, context);				
				InputsHandler inputsHandler = new InputsHandler();
				Map<String,DataflowInputPort> portMap = new HashMap<String, DataflowInputPort>();
				
				for (DataflowInputPort port : dataflow.getInputPorts()) {
					portMap.put(port.getName(), port);
				}
				inputsHandler.checkProvidedInputs(portMap,options);
				Map<String, WorkflowDataToken> inputs = inputsHandler.registerInputs(portMap,options, context);

				CommandLineResultListener resultListener = addResultListener(
						facade, context, dataflow, options);

				executeWorkflow(facade, inputs, resultListener);
			}
		}
		else {
			options.displayHelp();
		}
		
		//wait until user hits CTRL-C before exiting
		if (options.getStartDatabaseOnly()) {
			//FIXME: need to do this more gracefully.
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

	protected void validateDataflow(Dataflow dataflow) throws InvalidDataflowException {
		//FIXME: this needs expanding upon to give more details info back to the user
		//FIXME: added a getMessage to InvalidDataflowException may be good place to do this.
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

		while (!resultListener.isComplete()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger
						.warn(
								"Thread Interuption Exception whilst waiting for dataflow completion",
								e);
			}
		}
	}

	private void setupDatabase(CommandLineOptions options) throws DatabaseConfigurationException {
		DatabaseConfigurationHandler dbHandler = new DatabaseConfigurationHandler(
				options);		
		dbHandler.configureDatabase();
		if (!options.isInMemory()) {
			try {
				dbHandler.testDatabaseConnection();
			} catch (NamingException e) {
				throw new DatabaseConfigurationException("There was an error trying to setup the database datasource: "+e.getMessage(),e);
			} catch (SQLException e) {
				if (options.isClientServer()) {
					throw new DatabaseConfigurationException("There was an error whilst making a test database connection. If running with -clientserver you should check that a server is running (check -startdb or -dbproperties)",e);
				}
				if (options.isEmbedded()) {
					throw new DatabaseConfigurationException("There was an error whilst making a test database connection. If running with -embedded you should make sure that another process isn't using the database, or a server running through -startdb",e);
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

	private CommandLineResultListener addResultListener(
			WorkflowInstanceFacade facade, InvocationContext context,
			Dataflow dataflow, CommandLineOptions options) {
		File outputDir = null;
		File baclavaDoc = null;

		if (options.saveResultsToDirectory()) {
			outputDir = determineOutputDir(options, dataflow.getLocalName());
		}
		if (options.getOutputDocument() != null) {
			baclavaDoc = new File(options.getOutputDocument());
		}

		Map<String, Integer> outputPortNamesAndDepth = new HashMap<String, Integer>();
		for (DataflowOutputPort port : dataflow.getOutputPorts()) {
			outputPortNamesAndDepth.put(port.getName(), port.getDepth());
		}
		SaveResultsHandler resultsHandler = new SaveResultsHandler(
				outputPortNamesAndDepth, outputDir, baclavaDoc);
		CommandLineResultListener listener = new CommandLineResultListener(
				outputPortNamesAndDepth.size(), resultsHandler,
				outputDir != null, baclavaDoc != null);
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
