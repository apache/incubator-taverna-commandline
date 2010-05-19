package net.sf.taverna.t2.commandline;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.raven.launcher.Launchable;
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
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.springframework.context.ApplicationContext;

/**
 * A utility class that wraps the process of executing a workflow, allowing
 * workflows to be easily executed independently of the GUI.
 * 
 * @author Stuart Owen
 */

public class CommandLineLauncher implements Launchable {
	
	private static Namespace namespace = Namespace.getNamespace("b","http://org.embl.ebi.escience/baclava/0.1alpha");
	private static Logger logger = Logger.getLogger(CommandLineLauncher.class);
	
	/**
	 * Main method, purely for development and debugging purposes. Full execution of workflows will not work through this method.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) {
		new CommandLineLauncher().launch(args);
	}
	
	public int launch(String [] args) {
		try {
			return setupAndLaunch(args);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EditException e) {
			error("There was an error opening the workflow: "+e.getMessage());
		} catch (DeserializationException e) {
			error("There was an error opening the workflow: "+e.getMessage());
		} catch (InvalidDataflowException e) {
			error("There was an error opening the workflow: "+e.getMessage());
		}catch (JDOMException e) {
			error("There was an error opening the workflow: "+e.getMessage());
		}  catch (TokenOrderException e) {
			error("There was an error starting the workflow execution: "+e.getMessage());
		} catch (InvalidOptionException e) {
			error(e.getMessage());
		}
		return 0;
	}

	public int setupAndLaunch(String[] args) throws InvalidOptionException, IOException, EditException, DeserializationException, JDOMException, InvalidDataflowException, TokenOrderException {		
		
		CommandLineOptions options = new CommandLineOptions(args);
		if (!options.askedForHelp()) {
			DatabaseConfigurationHandler dbHandler = new DatabaseConfigurationHandler(options);	
			dbHandler.configureDatabase();
			
			URL workflowURL = readWorkflowURL(options.getWorkflow());

			InputStream stream = workflowURL.openStream();

			Dataflow dataflow = openDataflowFromStream(stream);
			DataflowValidationReport report = validateDataflow(dataflow);		
			
			InvocationContext context = createInvocationContext();
			
			WorkflowInstanceFacade facade = compileFacade(dataflow, context);
			Map<String,WorkflowDataToken> inputs = new InputsHandler().registerInputs(options, context);				
			
			CommandLineResultListener resultListener = addResultListener(facade, context,dataflow,options);

			facade.fire();
			for (String inputName : inputs.keySet()) {					
					WorkflowDataToken token = inputs.get(inputName);
					facade.pushData(token, inputName);
			}										
			
			while(!resultListener.isComplete()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.warn("Thread Interuption Exception whilst waiting for dataflow completion",e);
				}			
			}
		}
		
		

		return 0;
	}	

	private InvocationContext createInvocationContext() {
		ReferenceService referenceService = createReferenceServiceBean();
		ProvenanceConnector connector = null;
		DataManagementConfiguration dbConfig = DataManagementConfiguration.getInstance();
		if (dbConfig.isProvenanceEnabled()) {
			String connectorType = dbConfig.getConnectorType();

			for (ProvenanceConnectorFactory factory : ProvenanceConnectorFactoryRegistry
					.getInstance().getInstances()) {
				if (connectorType.equalsIgnoreCase(factory
						.getConnectorType())) {
					connector = factory
							.getProvenanceConnector();
				}
				
			}
			if (connector!=null) {
				connector.init();
			}
			else {
				error("Unable to initialise the provenance - the ProvenanceConnector cannot be found.");
			}
		}
		InvocationContext context = new CommandLineInvocationContext(
				referenceService, connector);
		return context;
	}

	private File determineOutputDir(CommandLineOptions options, String dataflowName) {
		File result = null;
		if (options.saveResultsToDirectory()) {
			result = new File(options.getOutputDirectory());
			if (result.exists()) {
				error("The specified output directory '" + options.getOutputDirectory() +"' already exists");
			}
		}
		else if (options.getOutputDocument()!=null) {
			result = new File(dataflowName+"_output");
			int x=1;
			while (result.exists()) {
				result=new File(dataflowName+"_output_"+x);
				x++;
			}
		}		
		return result;
	}	

	protected void error(String msg) {		
		System.err.println(msg);
		System.exit(-1);		
	}

	

	private URL readWorkflowURL(String workflowOption) throws MalformedURLException {
		URL url = new URL("file:");		
		URL workflowURL = new URL(url, workflowOption);
		return workflowURL;
	}		

	private CommandLineResultListener addResultListener(WorkflowInstanceFacade facade,
			InvocationContext context,Dataflow dataflow,CommandLineOptions options) {
		File outputDir = null;
		File baclavaDoc = null;
		
		if (options.saveResultsToDirectory()) {
			outputDir = determineOutputDir(options,dataflow.getLocalName());
		}
		if (options.getOutputDocument()!=null) {
			baclavaDoc = new File(options.getOutputDocument());
		}
		
		Map<String,Integer> outputPortNamesAndDepth = new HashMap<String, Integer>();
		for (DataflowOutputPort port : dataflow.getOutputPorts()) {
			outputPortNamesAndDepth.put(port.getName(), port.getDepth());
		}
		SaveResultsHandler resultsHandler = new SaveResultsHandler(outputPortNamesAndDepth, outputDir, baclavaDoc);
		CommandLineResultListener listener = new CommandLineResultListener(outputPortNamesAndDepth.size(),resultsHandler,outputDir!=null,baclavaDoc!=null);
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
			InvocationContext context) throws InvalidDataflowException  {
		Edits edits = EditsRegistry.getEdits();
		return edits.createWorkflowInstanceFacade(dataflow, context, "");
	}

	protected Dataflow openDataflowFromStream(InputStream stream) throws JDOMException, IOException, DeserializationException, EditException
			 {
		XMLDeserializer deserializer = XMLDeserializerRegistry.getInstance()
				.getDeserializer();
		SAXBuilder builder = new SAXBuilder();
		Element el = builder.build(stream).detachRootElement();
		return deserializer.deserializeDataflow(el);
	}

	protected DataflowValidationReport validateDataflow(Dataflow dataflow) {
		return dataflow.checkValidity();
	}

}
