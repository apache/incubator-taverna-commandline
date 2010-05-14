package net.sf.taverna.t2.commandline;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.T2Reference;

import org.apache.commons.io.IOUtils;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;

public class InputsHandler {

	protected Map<String, WorkflowDataToken> registerInputs(CommandLineOptions options,
			InvocationContext context) throws Exception {
		Map<String,WorkflowDataToken> inputs = new HashMap<String, WorkflowDataToken>();
		URL url = new URL("file:");
		
		if (options.hasOption("input") && options.hasOption("inputdoc")) {
			throw new InvalidOptionException("You can't provide both -input and -inputdoc arguments");
		}
		
		if (options.hasOption("input")) {
			String[] inputParams = options.getInputs();
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
					throw new InvalidOptionException("Missing input filename for input "+ inputName);					
				} catch (IOException e) {
					throw new InvalidOptionException("Could not read input " + inputName + ": " + e.getMessage());				
				}
			}
		}
		
		if (options.getInputDocument()!=null) {
			String inputDocPath = options.getInputDocument();
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
}
