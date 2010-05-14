package net.sf.taverna.t2.commandline;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sf.taverna.platform.spring.RavenAwareClassPathXmlApplicationContext;
import net.sf.taverna.raven.launcher.Launchable;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.provenance.reporter.ProvenanceReporter;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowOutputPort;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;
import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializer;
import net.sf.taverna.t2.workflowmodel.serialization.xml.XMLDeserializerRegistry;

import net.sf.taverna.t2.provenance.ProvenanceConnectorFactory;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactoryRegistry;
import net.sf.taverna.t2.provenance.connector.ProvenanceConnector;

import org.apache.commons.io.IOUtils;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.springframework.context.ApplicationContext;

/**
 * A utility class that wraps the process of executing a workflow, allowing
 * workflows to be easily executed independently of the GUI.
 * 
 * @author Stuart Owen
 */

public class CommandLineLauncher implements Launchable {
	
	private static Namespace namespace = Namespace.getNamespace("b","http://org.embl.ebi.escience/baclava/0.1alpha");
	
	/**
	 * Main method, purely for development and debugging purposes. Full execution of workflows will not work through this method.
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		new CommandLineLauncher().launch(args);
	}

	public int launch(String[] args) throws Exception {		
		
		CommandLineOptions options = new CommandLineOptions(args);
		
		DatabaseConfigurationHandler dbHandler = new DatabaseConfigurationHandler(options);	
		dbHandler.configureDatabase();
		
		URL workflowURL = readWorkflowURL(options.getWorkflow());

		InputStream stream = workflowURL.openStream();

		Dataflow dataflow = openDataflowFromStream(stream);
		DataflowValidationReport report = validateDataflow(dataflow);		
		
		InvocationContext context = createInvocationContext();
		
		WorkflowInstanceFacade facade = compileFacade(dataflow, context);
		Map<String,WorkflowDataToken> inputs = registerInputs(options,context);					
		
		CommandLineResultListener resultListener = addResultListener(facade, context,dataflow,options);

		facade.fire();
		for (String inputName : inputs.keySet()) {
				System.out.println("Pushing input: "+inputName);
				WorkflowDataToken token = inputs.get(inputName);
				facade.pushData(token, inputName);
		}										
		
		while(!resultListener.isComplete()) {
			Thread.sleep(100);			
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
		if (options.hasOption("output")) {
			result = new File(options.getOptionValue("output"));
			if (result.exists()) {
				error("The specified output directory '"+options.getOptionValue("output")+"' already exists");
			}
		}
		else if (!options.hasOption("outputdoc")) {
			result = new File(dataflowName+"_output");
			int x=1;
			while (result.exists()) {
				result=new File(dataflowName+"_output_"+x);
				x++;
			}
		}		
		return result;
	}

	private void saveOutputDoc(String filename,Map<String, WorkflowDataToken> outputMap,
			InvocationContext context) throws IOException {
		Map<String, Object> objectMap = new HashMap<String, Object>();
		for (String outputName : outputMap.keySet()) {			
			WorkflowDataToken token = outputMap.get(outputName);
			Object value = context.getReferenceService().renderIdentifier(token.getData(),
					Object.class, context);
			objectMap.put(outputName, value);
   		}
		Map<String, DataThing> dataThings = bakeDataThingMap(objectMap);
		
		// Build the string containing the XML document from the result map
		Document doc = getDataDocument(dataThings);
	    XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
	    File file = new File(filename);
	    String xmlString = xo.outputString(doc);
	    PrintWriter out = new PrintWriter(new FileWriter(file));
	    out.print(xmlString);
	    out.flush();
	    out.close();
	}
	
	/**
	 * Returns a map of port names to DataThings from a map of port names to a 
	 * list of (lists of ...) result objects.
	 */
	protected Map<String, DataThing> bakeDataThingMap(Map<String, Object> resultMap) {
		
		Map<String, DataThing> dataThingMap = new HashMap<String, DataThing>();
		for (Iterator<String> i = resultMap.keySet().iterator(); i.hasNext();) {
			String portName = (String) i.next();
			dataThingMap.put(portName, DataThingFactory.bake(resultMap.get(portName)));
		}
		return dataThingMap;
	}
	
	/**
	 * Returns a org.jdom.Document from a map of port named to DataThingS containing
	 * the port's results.
	 */
	public static Document getDataDocument(Map<String, DataThing> dataThings) {
		Element rootElement = new Element("dataThingMap", namespace);
		Document theDocument = new Document(rootElement);
		for (Iterator<String> i = dataThings.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			DataThing value = (DataThing) dataThings.get(key);
			Element dataThingElement = new Element("dataThing", namespace);
			dataThingElement.setAttribute("key", key);
			dataThingElement.addContent(value.getElement());
			rootElement.addContent(dataThingElement);
		}
		return theDocument;
	}

	protected void error(String msg) {		
		System.err.println(msg);
		System.exit(-1);		
	}

	protected Map<String, WorkflowDataToken> registerInputs(CommandLineOptions options,
			InvocationContext context) throws Exception {
		Map<String,WorkflowDataToken> inputs = new HashMap<String, WorkflowDataToken>();
		URL url = new URL("file:");
		
		if (options.hasOption("input") && options.hasOption("inputdoc")) {
			error("You can't provide both -input and -inputdoc arguments");
		}
		
		if (options.hasOption("input")) {
			String[] inputParams = options.getOptionValues("input");
			for (int i = 0; i < inputParams.length; i = i + 2) {
				String inputName = inputParams[i];
				try {
					
					URL inputURL = new URL(url, inputParams[i + 1]);
					
					Object inputValue=IOUtils.toString(inputURL.openStream());
					System.out.println("Input for "+inputName+" is '"+inputValue.toString()+"'");
					T2Reference entityId=context.getReferenceService().register(inputValue, 0, true, context);
					WorkflowDataToken token = new WorkflowDataToken("",new int[]{}, entityId, context);
					inputs.put(inputName, token);
					
				} catch (IndexOutOfBoundsException e) {
					error("Missing input filename for input "+ inputName);					
				} catch (IOException e) {
					error("Could not read input " + inputName + ": " + e.getMessage());				
				}
			}
		}
		
		if (options.hasOption("inputdoc")) {
			String inputDocPath = options.getOptionValue("inputdoc");
			URL inputDocURL = new URL(url, inputDocPath);
			SAXBuilder builder = new SAXBuilder();
			Document inputDoc = builder.build(inputDocURL.openStream());
			Map<String,DataThing> things = DataThingXMLFactory.parseDataDocument(inputDoc);
			for (String inputName : things.keySet()) {
				DataThing thing = things.get(inputName);
				T2Reference entityId=context.getReferenceService().register(thing.getDataObject(), 0, true, context);
				WorkflowDataToken token = new WorkflowDataToken("",new int[]{}, entityId, context);
				inputs.put(inputName, token);
			}
		}
		
		return inputs;
	}

	private URL readWorkflowURL(String workflowOption) throws Exception {
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
		if (options.outputDocument()!=null) {
			baclavaDoc = new File(options.outputDocument());
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
			InvocationContext context) throws Exception {
		Edits edits = EditsRegistry.getEdits();
		return edits.createWorkflowInstanceFacade(dataflow, context, "");
	}

	protected Dataflow openDataflowFromStream(InputStream stream)
			throws Exception {
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
