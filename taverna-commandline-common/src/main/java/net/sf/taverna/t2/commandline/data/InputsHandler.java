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
package net.sf.taverna.t2.commandline.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.taverna.t2.commandline.exceptions.InputMismatchException;
import net.sf.taverna.t2.commandline.exceptions.InvalidOptionException;
import net.sf.taverna.t2.commandline.exceptions.ReadInputException;
import net.sf.taverna.t2.commandline.options.CommandLineOptions;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.DataflowInputPort;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;

/**
 * 
 * Handles the reading, or processing, or input values according to arguments provided to the commandline.
 * The may be either as direct values, from a file, or from a Baclava document.
 * 
 * Also handles registering the input values with the Reference Service, ready to initiate
 * the workflow run.
 * 
 * @author Stuart Owen
 *
 */
public class InputsHandler {
	
	private static Logger logger = Logger.getLogger(InputsHandler.class); 

	
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
					
					T2Reference entityId=null;
					
					if (options.hasDelimiterFor(inputName)) {
						String delimiter=options.inputDelimiter(inputName);
						Object value = IOUtils.toString(inputURL.openStream()).split(delimiter);
						
						value=checkForDepthMismatch(1, port.getDepth(), inputName, value);						
						entityId=context.getReferenceService().register(value, port.getDepth(), true, context);						
					}
					else
					{
						Object value = IOUtils.toByteArray(inputURL.openStream());
						value=checkForDepthMismatch(0, port.getDepth(), inputName, value);
						entityId=context.getReferenceService().register(value, port.getDepth(), true, context);
					}
															
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
										
					T2Reference entityId=null;
					if (options.hasDelimiterFor(inputName)) {
						String delimiter=options.inputDelimiter(inputName);
						Object value=checkForDepthMismatch(1, port.getDepth(), inputName, inputValue.split(delimiter));
						entityId=context.getReferenceService().register(value, port.getDepth(), true, context);						
					}
					else
					{
						Object value=checkForDepthMismatch(0, port.getDepth(), inputName, inputValue);
						entityId=context.getReferenceService().register(value, port.getDepth(), true, context);
					}
										
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
	
	private Object checkForDepthMismatch(int inputDepth,int portDepth,String inputName,Object inputValue) throws InvalidOptionException {
		if (inputDepth!=portDepth) {
			if (inputDepth<portDepth) {
				logger.warn("Wrapping input for '" + inputName + "' from a depth of "+inputDepth+" to the required depth of "+portDepth);
				while (inputDepth<portDepth) {
					List<Object> l=new ArrayList<Object>();
					l.add(inputValue);
					inputValue=l;
					inputDepth++;
				}
			}
			else {
				String msg="There is an irreconcilable mismatch between depth of the list for the input port '"+inputName+"' and the data presented. The input port requires a "+depthToString(portDepth)+" and the data presented is a "+depthToString(inputDepth);				
				throw new InvalidOptionException(msg);
			}
		}
		
		return inputValue;
	}
	
	private String depthToString(int depth) {
		switch (depth) {
		case 0:
			return "single item";			
		case 1:
			return "list";			
		case 2:
			return "list of lists";			
		default:
			return "list of depth "+depth;
		}
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
