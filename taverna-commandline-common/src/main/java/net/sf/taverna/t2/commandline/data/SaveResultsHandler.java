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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.commandline.CommandLineResultListener;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactory;

import org.apache.log4j.Logger;

import uk.org.taverna.configuration.database.DatabaseConfiguration;
import uk.org.taverna.databundle.DataBundles;

/**
 * Handles all recording of results as they are received by the {@link CommandLineResultListener}
 * or when the workflow enactment has completed.
 * This includes saving as a Baclava Document, or storing individual results.
 *
 * @author Stuart Owen
 *
 * @see BaclavaHandler
 * @see CommandLineResultListener
 *
 */
public class SaveResultsHandler {

	private static Logger logger = Logger.getLogger(CommandLineResultListener.class);

	private final File outputDirectory;
	private final File baclavaFile;
	private final File janusFile;
	private final File opmFile;
	private final DatabaseConfiguration dbConfig;
//	private ProvenanceExporter provExport;
	private List<ProvenanceConnectorFactory> provenanceConnectorFactories;

	public SaveResultsHandler(File rootOutputDir, File outputBaclavaDocumentFile, File opmFile,
			File janusFile, DatabaseConfiguration databaseConfiguration, List<ProvenanceConnectorFactory> provenanceConnectorFactories) {
		this.outputDirectory = rootOutputDir;
		this.baclavaFile = outputBaclavaDocumentFile;
		this.janusFile = janusFile;
		this.opmFile = opmFile;
		dbConfig = databaseConfiguration;
		this.provenanceConnectorFactories = provenanceConnectorFactories;
	}


	public void saveOutputBaclavaDocument(Map<String, Path> allResults) throws IOException {
		if (baclavaFile != null) {
			if (baclavaFile.getParentFile() != null){
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
	 * @param workflowOutputPortName
	 * @param data
	 * @throws IOException
	 */
	public void saveResultsForPort(String workflowOutputPortName, Path data) throws IOException {
		if (DataBundles.isList(data)) {
			DataBundles.copyRecursively(data, outputDirectory.toPath().resolve(workflowOutputPortName));
		} else {
	        Files.copy(data, outputDirectory.toPath().resolve(workflowOutputPortName));
		}
	}

	public void saveOpm(String workflowRunId) {
		if (opmFile.getParentFile() != null) {
			opmFile.getParentFile().mkdirs();
		}
		BufferedOutputStream outStream;
		try {
			outStream = new BufferedOutputStream(new FileOutputStream(opmFile));
		} catch (FileNotFoundException e1) {
			logger.error("Can't find directory for writing OPM to " + opmFile, e1);
			return;
		}
		try {
			//getProvenanceExporter().exportAsOPMRDF(workflowRunId, outStream);
		} catch (Exception e) {
			logger.error("Can't write OPM to " + opmFile, e);
		} finally {
			try {
				outStream.close();
			} catch (IOException e) {
			}
		}
	}

//	protected synchronized ProvenanceExporter getProvenanceExporter() {
//		if (provExport == null) {
//			String connectorType = dbConfig.getConnectorType();
//			ProvenanceAccess provAccess = new ProvenanceAccess(connectorType, provenanceConnectorFactories);
//			provExport = new ProvenanceExporter(provAccess);
//		}
//		return provExport;
//	}

	public void saveJanus(String workflowRunId) {
		if (janusFile.getParentFile() != null) {
			janusFile.getParentFile().mkdirs();
		}
		BufferedOutputStream outStream;
		try {
			outStream = new BufferedOutputStream(new FileOutputStream(janusFile));
		} catch (FileNotFoundException e1) {
			logger.error("Can't find directory for writing Janus to " + janusFile, e1);
			return;
		}
		try {
			//getProvenanceExporter().exportAsJanusRDF(workflowRunId, outStream);
		} catch (Exception e) {
			logger.error("Can't write Janus to " + janusFile, e);
		} finally {
			try {
				outStream.close();
			} catch (IOException e) {
			}
		}
	}

}
