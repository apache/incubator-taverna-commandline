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
import net.sf.taverna.t2.results.BaclavaDocumentHandler;
import net.sf.taverna.t2.baclava.DataThing;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.jdom.JDOMException;

import uk.org.taverna.platform.data.Data;
import uk.org.taverna.platform.data.DataService;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;

/**
 *
 * Handles the reading, or processing, or input values according to arguments provided to the commandline.
 * The may be either as direct values, from a file, or from a Baclava document.
 *
 * Also handles registering the input values with the Data Service, ready to initiate
 * the workflow run.
 *
 * @author Stuart Owen
 *
 */
public class InputsHandler {

	private static Logger logger = Logger.getLogger(InputsHandler.class);
	private DataService dataService;


	public InputsHandler(DataService dataService) {
		this.dataService = dataService;
	}


	public void checkProvidedInputs(Map<String, InputWorkflowPort> portMap, CommandLineOptions options) throws InputMismatchException  {
		//we dont check for the document
		if (options.getInputDocument()==null) {
			Set<String> providedInputNames = new HashSet<String>();
			for (int i=0;i<options.getInputFiles().length;i+=2) {
				// If it already contains a value for the input port, e.g
				// two inputs are provided for the same port
				if (providedInputNames.contains(options.getInputFiles()[i])){
					throw new InputMismatchException("Two input values were provided for the same input port " +options.getInputFiles()[i] +".",null, null);
				}
				providedInputNames.add(options.getInputFiles()[i]);
			}

			for (int i=0;i<options.getInputValues().length;i+=2) {
				// If it already contains a value for the input port, e.g
				// two inputs are provided for the same port
				if (providedInputNames.contains(options.getInputValues()[i])){
					throw new InputMismatchException("Two input values were provided for the same input port " +options.getInputValues()[i] +".",null, null);
				}
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


	public Map<String, Data> registerInputs(Map<String, InputWorkflowPort> portMap, CommandLineOptions options,
			InvocationContext context) throws InvalidOptionException, ReadInputException  {
		Map<String, Data> inputs = new HashMap<String, Data>();
		URL url;
		try {
			url = new URL("file:");
		} catch (MalformedURLException e1) {
			//Should never happen, but just in case:
			throw new ReadInputException("The was an internal error setting up the URL to open the inputs. You should contact Taverna support.",e1);
		}

		if (options.hasInputFiles()) {
			regesterInputsFromFiles(portMap, options, inputs, url);
		}

		if (options.hasInputValues()) {
			registerInputsFromValues(portMap, options, inputs);

		}

		if (options.getInputDocument()!=null) {
			registerInputsFromBaclava(options, inputs, url);
		}

		return inputs;
	}


	private void registerInputsFromBaclava(CommandLineOptions options,
			Map<String, Data> inputs,
			URL url) throws ReadInputException {
		String inputDocPath = options.getInputDocument();

		URL inputDocURL;
		try {
			inputDocURL = new URL(url, inputDocPath);
		} catch (MalformedURLException e1) {
			throw new ReadInputException(
					"The a error reading the input document from : "
							+ inputDocPath + ", " + e1.getMessage(), e1);
		}
		Map<String, DataThing> things;
		try {
			things = new BaclavaDocumentHandler().readData(inputDocURL.openStream());
		} catch (IOException e) {
			throw new ReadInputException(
					"There was an error reading the input document file: "
							+ e.getMessage(), e);
		} catch (JDOMException e) {
			throw new ReadInputException(
					"There was a error processing the input document XML: "
							+ e.getMessage(), e);
		}
		for (String inputName : things.keySet()) {
			DataThing thing = things.get(inputName);
			Object object = thing.getDataObject();
			dataService.create(object);
//			T2Reference entityId = referenceService.register(object,getObjectDepth(object), true, null);
			inputs.put(inputName, dataService.create(object));
		}
	}


	private void registerInputsFromValues(
			Map<String, InputWorkflowPort> portMap, CommandLineOptions options,
			Map<String, Data> inputs)
			throws InvalidOptionException {
		String[] inputParams = options.getInputValues();
		for (int i = 0; i < inputParams.length; i = i + 2) {
			String inputName = inputParams[i];
			try {
				String inputValue = inputParams[i + 1];
				InputWorkflowPort port = portMap.get(inputName);

				if (port==null) {
					throw new InvalidOptionException("Cannot find an input port named '"+inputName+"'");
				}

				Data data = null;
				if (options.hasDelimiterFor(inputName)) {
					String delimiter=options.inputDelimiter(inputName);
					Object value=checkForDepthMismatch(1, port.getDepth(), inputName, inputValue.split(delimiter));
					data = dataService.create(value);
				}
				else
				{
					Object value=checkForDepthMismatch(0, port.getDepth(), inputName, inputValue);
					data = dataService.create(value);
				}
				inputs.put(inputName, data);

			} catch (IndexOutOfBoundsException e) {
				throw new InvalidOptionException("Missing input value for input "+ inputName);
			}
		}
	}


	private void regesterInputsFromFiles(
			Map<String, InputWorkflowPort> portMap, CommandLineOptions options,
			Map<String, Data> inputs,
			URL url) throws InvalidOptionException {
		String[] inputParams = options.getInputFiles();
		for (int i = 0; i < inputParams.length; i = i + 2) {
			String inputName = inputParams[i];
			try {
				URL inputURL = new URL(url, inputParams[i + 1]);
				InputWorkflowPort port = portMap.get(inputName);

				if (port==null) {
					throw new InvalidOptionException("Cannot find an input port named '"+inputName+"'");
				}

				Data data = null;
				if (options.hasDelimiterFor(inputName)) {
					String delimiter=options.inputDelimiter(inputName);
					Object value = IOUtils.toString(inputURL.openStream()).split(delimiter);

					value=checkForDepthMismatch(1, port.getDepth(), inputName, value);
					data = dataService.create(value);
				}
				else
				{
					Object value = IOUtils.toByteArray(inputURL.openStream());
					value=checkForDepthMismatch(0, port.getDepth(), inputName, value);
					data = dataService.create(value);
				}
				inputs.put(inputName, data);

			} catch (IndexOutOfBoundsException e) {
				throw new InvalidOptionException("Missing input filename for input "+ inputName);
			} catch (IOException e) {
				throw new InvalidOptionException("Could not read input " + inputName + ": " + e.getMessage());
			}
		}
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
				String msg="There is a mismatch between depth of the list for the input port '"+inputName+"' and the data presented. The input port requires a "+depthToString(portDepth)+" and the data presented is a "+depthToString(inputDepth);
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
