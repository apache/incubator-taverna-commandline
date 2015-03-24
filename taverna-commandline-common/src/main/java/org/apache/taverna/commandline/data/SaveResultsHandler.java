/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.commandline.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.taverna.databundle.DataBundles;

/**
 * Handles all recording of results as they are received by the {@link CommandLineResultListener} or
 * when the workflow enactment has completed.
 * This includes saving as a Baclava Document, or storing individual results.
 *
 * @author Stuart Owen
 * @see BaclavaHandler
 * @see CommandLineResultListener
 */
public class SaveResultsHandler {

	private final File outputDirectory;

	public SaveResultsHandler(File rootOutputDir) {
		this.outputDirectory = rootOutputDir;
	}

	/**
	 * Given the Data on an output port, saves the data on a disk in the
	 * output directory.
	 *
	 * @param portName
	 * @param data
	 * @throws IOException
	 */
	public void saveResultsForPort(String portName, Path data) throws IOException {
		if (DataBundles.isList(data)) {
			Path outputPath = outputDirectory.toPath().resolve(portName);
			Files.createDirectories(outputPath);
			saveList(DataBundles.getList(data), outputPath);
		} else if (DataBundles.isError(data)) {
			Files.copy(data, outputDirectory.toPath().resolve(portName + ".error"));
		} else {
			Files.copy(data, outputDirectory.toPath().resolve(portName));
		}
	}

	private void saveList(List<Path> list, Path destination) throws IOException {
		int index = 1;
		for (Path data : list) {
			if (data != null) {
				if (DataBundles.isList(data)) {
					Path outputPath = destination.resolve(String.valueOf(index));
					Files.createDirectories(outputPath);
					saveList(DataBundles.getList(data), outputPath);
				} else if (DataBundles.isError(data)) {
					Files.copy(data, destination.resolve(String.valueOf(index) + ".error"));
				} else {
					Files.copy(data, destination.resolve(String.valueOf(index)));
				}
			}
			index++;
		}
	}

}
