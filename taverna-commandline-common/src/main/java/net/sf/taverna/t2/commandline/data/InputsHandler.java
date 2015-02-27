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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
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

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.taverna.databundle.DataBundles;
import org.apache.taverna.robundle.Bundle;
import org.apache.taverna.scufl2.api.port.InputWorkflowPort;

/**
 * Handles the reading, or processing, or input values according to arguments provided to the
 * commandline.
 * The may be either as direct values, from a file, or from a Baclava document.
 * Also handles registering the input values with the Data Service, ready to initiate
 * the workflow run.
 *
 * @author Stuart Owen
 */
public class InputsHandler {

	private static Logger logger = Logger.getLogger(InputsHandler.class);

	public void checkProvidedInputs(Map<String, InputWorkflowPort> portMap,
			CommandLineOptions options) throws InputMismatchException {
		// we dont check for the document
                Set<String> providedInputNames = new HashSet<String>();
                for (int i = 0; i < options.getInputFiles().length; i += 2) {
                        // If it already contains a value for the input port, e.g
                        // two inputs are provided for the same port
                        if (providedInputNames.contains(options.getInputFiles()[i])) {
                                throw new InputMismatchException(
                                        "Two input values were provided for the same input port "
                                        + options.getInputFiles()[i] + ".", null, null);
                        }
                        providedInputNames.add(options.getInputFiles()[i]);
                }

                for (int i = 0; i < options.getInputValues().length; i += 2) {
                        // If it already contains a value for the input port, e.g
                        // two inputs are provided for the same port
                        if (providedInputNames.contains(options.getInputValues()[i])) {
                                throw new InputMismatchException(
                                        "Two input values were provided for the same input port "
                                        + options.getInputValues()[i] + ".", null, null);
                        }
                        providedInputNames.add(options.getInputValues()[i]);
                }

                if (portMap.size() * 2 != (options.getInputFiles().length + options.getInputValues().length)) {
                        throw new InputMismatchException(
                                    "The number of inputs provided does not match the number of input ports.",
						portMap.keySet(), providedInputNames);
                }

                for (String portName : portMap.keySet()) {
                        if (!providedInputNames.contains(portName)) {
                                throw new InputMismatchException(
                                        "The provided inputs does not contain an input for the port '"
                                        + portName + "'", portMap.keySet(), providedInputNames);
			}
		}
	}

	public Bundle registerInputs(Map<String, InputWorkflowPort> portMap,
			CommandLineOptions options, InvocationContext context) throws InvalidOptionException,
			ReadInputException, IOException {
		Bundle inputDataBundle;
		inputDataBundle = DataBundles.createBundle();
		inputDataBundle.setDeleteOnClose(false);
		System.out.println("Bundle: " + inputDataBundle.getSource());
		
		Path inputs = DataBundles.getInputs(inputDataBundle);

		URL url;
		try {
			url = new URL("file:");
		} catch (MalformedURLException e1) {
			// Should never happen, but just in case:
			throw new ReadInputException(
					"The was an internal error setting up the URL to open the inputs. You should contact Taverna support.",
					e1);
		}

		if (options.hasInputFiles()) {
			regesterInputsFromFiles(portMap, options, inputs, url);
		}

		if (options.hasInputValues()) {
			registerInputsFromValues(portMap, options, inputs);

		}

		return inputDataBundle;
	}

	private void registerInputsFromValues(Map<String, InputWorkflowPort> portMap,
			CommandLineOptions options, Path inputs) throws InvalidOptionException {
		String[] inputParams = options.getInputValues();
		for (int i = 0; i < inputParams.length; i = i + 2) {
			String inputName = inputParams[i];
			try {
				String inputValue = inputParams[i + 1];
				InputWorkflowPort port = portMap.get(inputName);

				if (port == null) {
					throw new InvalidOptionException("Cannot find an input port named '"
							+ inputName + "'");
				}

				Path portPath = DataBundles.getPort(inputs, inputName);
				if (options.hasDelimiterFor(inputName)) {
					String delimiter = options.inputDelimiter(inputName);
					Object value = checkForDepthMismatch(1, port.getDepth(), inputName,
							inputValue.split(delimiter));
					setValue(portPath, value);
				} else {
					Object value = checkForDepthMismatch(0, port.getDepth(), inputName, inputValue);
					setValue(portPath, value);
				}

			} catch (IndexOutOfBoundsException e) {
				throw new InvalidOptionException("Missing input value for input " + inputName);
			} catch (IOException e) {
				throw new InvalidOptionException("Error creating value for input " + inputName);
			}
		}
	}

	private void regesterInputsFromFiles(Map<String, InputWorkflowPort> portMap,
			CommandLineOptions options, Path inputs, URL url) throws InvalidOptionException {
		String[] inputParams = options.getInputFiles();
		for (int i = 0; i < inputParams.length; i = i + 2) {
			String inputName = inputParams[i];
			try {
				URL inputURL = new URL(url, inputParams[i + 1]);
				InputWorkflowPort port = portMap.get(inputName);

				if (port == null) {
					throw new InvalidOptionException("Cannot find an input port named '"
							+ inputName + "'");
				}

				Path portPath = DataBundles.getPort(inputs, inputName);
				if (options.hasDelimiterFor(inputName)) {
					String delimiter = options.inputDelimiter(inputName);
					Object value = IOUtils.toString(inputURL.openStream()).split(delimiter);
					value = checkForDepthMismatch(1, port.getDepth(), inputName, value);
					setValue(portPath, value);
				} else {
					Object value = IOUtils.toByteArray(inputURL.openStream());
					value = checkForDepthMismatch(0, port.getDepth(), inputName, value);
					setValue(portPath, value);
				}
			} catch (IndexOutOfBoundsException e) {
				throw new InvalidOptionException("Missing input filename for input " + inputName);
			} catch (IOException e) {
				throw new InvalidOptionException("Could not read input " + inputName + ": "
						+ e.getMessage());
			}
		}
	}

	private void setValue(Path port, Object userInput) throws IOException {
		if (userInput instanceof File) {
			DataBundles.setReference(port, ((File) userInput).toURI());
		} else if (userInput instanceof URL) {
			try {
				DataBundles.setReference(port, ((URL) userInput).toURI());
			} catch (URISyntaxException e) {
				logger.warn(String.format("Error converting %1$s to URI", userInput), e);
			}
		} else if (userInput instanceof String) {
			DataBundles.setStringValue(port, (String) userInput);
		} else if (userInput instanceof byte[]) {
			Files.write(port, (byte[]) userInput, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		} else if (userInput instanceof List<?>) {
			DataBundles.createList(port);
			List<?> list = (List<?>) userInput;
			for (Object object : list) {
				setValue(DataBundles.newListItem(port), object);
			}
		} else {
			logger.warn("Unknown input type : " + userInput.getClass().getName());
		}
	}

	private Object checkForDepthMismatch(int inputDepth, int portDepth, String inputName,
			Object inputValue) throws InvalidOptionException {
		if (inputDepth != portDepth) {
			if (inputDepth < portDepth) {
				logger.warn("Wrapping input for '" + inputName + "' from a depth of " + inputDepth
						+ " to the required depth of " + portDepth);
				while (inputDepth < portDepth) {
					List<Object> l = new ArrayList<Object>();
					l.add(inputValue);
					inputValue = l;
					inputDepth++;
				}
			} else {
				String msg = "There is a mismatch between depth of the list for the input port '"
						+ inputName + "' and the data presented. The input port requires a "
						+ depthToString(portDepth) + " and the data presented is a "
						+ depthToString(inputDepth);
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
			return "list of depth " + depth;
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
