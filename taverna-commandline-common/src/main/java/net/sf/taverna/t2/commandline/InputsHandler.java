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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.commandline.exceptions.InputMismatchException;
import net.sf.taverna.t2.commandline.exceptions.InvalidOptionException;
import net.sf.taverna.t2.commandline.exceptions.ReadInputException;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;

import org.apache.commons.io.IOUtils;
import org.embl.ebi.escience.baclava.DataThing;

public class InputsHandler {

	
	public void checkProvidedInputs(Map<String, DataflowInputPort> portMap, CommandLineOptions options) throws InputMismatchException  {
		//we dont check for the document 
		if (options.getInputDocument()==null) {
			Set<String> providedInputNames = new HashSet<String>();
			for (int i=0;i<options.getInputFiles().length;i+=2) {
				providedInputNames.add(options.getInputFiles()[i]);								
			}
			
			for (int i=0;i<options.getInputValues().length;i+=2) {
				providedInputNames.add(options.getInputValues()[i]);								
			}
			
			if (portMap.size()*2 != (options.getInputFiles().length + options.getInputValues().length)) {
				throw new InputMismatchException("The number of inputs provided does not match the number of input ports.",portMap.keySet(),providedInputNames);
			}
			
			for (String portName : portMap.keySet()) {
				if (!providedInputNames.contains(portName)) {
					throw new InputMismatchException("The provided inputs does not contain an input for the port '"+portName+"'",portMap.keySet(),providedInputNames);
				}
			}
		}
	}


	public Map<String, WorkflowDataToken> registerInputs(Map<String, DataflowInputPort> portMap, CommandLineOptions options,
			InvocationContext context) throws InvalidOptionException, ReadInputException  {
		Map<String,WorkflowDataToken> inputs = new HashMap<String, WorkflowDataToken>();
		URL url;
		try {
			url = new URL("file:");
		} catch (MalformedURLException e1) {
			//Should never happen, but just incase:
			throw new ReadInputException("The was an internal error setting up the URL to open the inputs. You should contact Taverna support.",e1);
		}
		
		
		if (options.hasInputFiles()) {
			String[] inputParams = options.getInputFiles();
			for (int i = 0; i < inputParams.length; i = i + 2) {
				String inputName = inputParams[i];
				try {					
					URL inputURL = new URL(url, inputParams[i + 1]);
					DataflowInputPort port = portMap.get(inputName);
					
					if (port==null) {
						throw new InvalidOptionException("Cannot find an input port named '"+inputName+"'");
					}
					
					T2Reference entityId=context.getReferenceService().register(IOUtils.toByteArray(inputURL.openStream()), port.getDepth(), true, context);

					WorkflowDataToken token = new WorkflowDataToken("",new int[]{}, entityId, context);
					inputs.put(inputName, token);
					
				} catch (IndexOutOfBoundsException e) {
					throw new InvalidOptionException("Missing input filename for input "+ inputName);					
				} catch (IOException e) {
					throw new InvalidOptionException("Could not read input " + inputName + ": " + e.getMessage());				
				}
			}
		}
		
		if (options.hasInputValues()) {
			String[] inputParams = options.getInputValues();
			for (int i = 0; i < inputParams.length; i = i + 2) {
				String inputName = inputParams[i];
				try {					
					String inputValue = inputParams[i + 1];
					DataflowInputPort port = portMap.get(inputName);
					
					if (port==null) {
						throw new InvalidOptionException("Cannot find an input port named '"+inputName+"'");
					}
					
					T2Reference entityId=context.getReferenceService().register(inputValue, port.getDepth(), true, context);

					WorkflowDataToken token = new WorkflowDataToken("",new int[]{}, entityId, context);
					inputs.put(inputName, token);
					
				} catch (IndexOutOfBoundsException e) {
					throw new InvalidOptionException("Missing input value for input "+ inputName);					
				} 
			}
			
		}
		
		if (options.getInputDocument()!=null) {
			String inputDocPath = options.getInputDocument();
			Map<String, DataThing> things = new BaclavaDocumentHandler().readInputDocument(inputDocPath);
			for (String inputName : things.keySet()) {
				DataThing thing = things.get(inputName);
				Object object = thing.getDataObject();
				T2Reference entityId=context.getReferenceService().register(object,getObjectDepth(object), true, context);
				WorkflowDataToken token = new WorkflowDataToken("",new int[]{}, entityId, context);
				inputs.put(inputName, token);
			}
		}
		
		return inputs;
	}


	@SuppressWarnings("unchecked")
	private int getObjectDepth(Object o) {
		int result = 0;
		if (o instanceof Iterable) {
			result++;
			Iterator i = ((Iterable) o).iterator();
			
			if (i.hasNext()) {
				Object child = i.next();
				result = result + getObjectDepth(child);
			}
		}
		return result;
	}
}
