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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import uk.org.taverna.databundle.DataBundles;

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
	private final File baclavaFile;

	public SaveResultsHandler(File rootOutputDir, File outputBaclavaDocumentFile) {
		this.outputDirectory = rootOutputDir;
		this.baclavaFile = outputBaclavaDocumentFile;
	}

	public void saveOutputBaclavaDocument(Map<String, Path> allResults) throws IOException {
		if (baclavaFile != null) {
			if (baclavaFile.getParentFile() != null) {
				baclavaFile.getParentFile().mkdirs();
			}
			BaclavaDocumentHandler handler = new BaclavaDocumentHandler();
			handler.setChosenReferences(allResults);
			handler.saveData(baclavaFile);
		}
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
