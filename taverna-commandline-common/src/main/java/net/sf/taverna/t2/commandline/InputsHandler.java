package net.sf.taverna.t2.commandline;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.commandline.exceptions.InvalidOptionException;
import net.sf.taverna.t2.commandline.exceptions.ReadInputException;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.T2Reference;

import org.apache.commons.io.IOUtils;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class InputsHandler {

	protected Map<String, WorkflowDataToken> registerInputs(CommandLineOptions options,
			InvocationContext context) throws InvalidOptionException, ReadInputException  {
		Map<String,WorkflowDataToken> inputs = new HashMap<String, WorkflowDataToken>();
		URL url;
		try {
			url = new URL("file:");
		} catch (MalformedURLException e1) {
			//Should never happen, but just incase:
			throw new ReadInputException("The was an internal error setting up the URL to open the inputs. You should contact Taverna support.",e1);
		}
		
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
			URL inputDocURL;
			try {
				inputDocURL = new URL(url, inputDocPath);
			} catch (MalformedURLException e1) {
				throw new ReadInputException("The a problem reading the input document from : "+inputDocPath+", "+e1.getMessage(),e1);
			}
			SAXBuilder builder = new SAXBuilder();
			Document inputDoc;
			try {
				inputDoc = builder.build(inputDocURL.openStream());
			} catch (IOException e) {
				throw new ReadInputException("There was an error reading the input document file: "+e.getMessage(),e);
			} catch (JDOMException e) {
				throw new ReadInputException("There was a problem processing the input document XML: "+e.getMessage(),e);
			}
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
