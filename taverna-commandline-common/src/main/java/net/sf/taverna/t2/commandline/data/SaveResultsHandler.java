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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.commandline.CommandLineResultListener;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.provenance.ProvenanceConnectorFactory;
import net.sf.taverna.t2.reference.ErrorDocument;

import org.apache.log4j.Logger;

import uk.org.taverna.platform.data.Data;
import uk.org.taverna.platform.data.DataService;
import uk.org.taverna.platform.database.DatabaseConfiguration;

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

	private final Map<String, Integer> portsAndDepth;
	private HashMap<String, Integer> depthSeen;
	private final File rootOutputDirectory;
	private static Logger logger = Logger
			.getLogger(CommandLineResultListener.class);
	private final File outputBaclavaDocumentFile;
	private final File janusFile;
	private final File opmFile;
	private final DataService dataService;
	private final DatabaseConfiguration dbConfig;
	//private ProvenanceExporter provExport;
	private List<ProvenanceConnectorFactory> provenanceConnectorFactories;

//	public SaveResultsHandler(Map<String, Integer> portsAndDepth,
//			File rootDirectory, File outputDocumentFile, File janus, File opm) {
//
//		this.portsAndDepth = portsAndDepth;
//		this.rootOutputDirectory = rootDirectory;
//		this.outputBaclavaDocumentFile = outputDocumentFile;
//		this.janusFile = janus;
//		this.opmFile = opm;
//
//		depthSeen = new HashMap<String, Integer>();
//		for (String portName : portsAndDepth.keySet()) {
//			depthSeen.put(portName, -1);
//		}
//	}

	public SaveResultsHandler(DataService dataService, File rootOutputDir, File outputBaclavaDocumentFile, File opmFile,
			File janusFile, DatabaseConfiguration databaseConfiguration, List<ProvenanceConnectorFactory> provenanceConnectorFactories) {
		this.dataService = dataService;
		this.rootOutputDirectory = rootOutputDir;
		this.outputBaclavaDocumentFile = outputBaclavaDocumentFile;
		this.janusFile = janusFile;
		this.opmFile = opmFile;
		dbConfig = databaseConfiguration;
		this.provenanceConnectorFactories = provenanceConnectorFactories;

		this.portsAndDepth = null;
	}

	public void tokenReceived(WorkflowDataToken token, String portName) {
		if (rootOutputDirectory != null) { //only save individual results if a directory is specified
			if (portsAndDepth.containsKey(portName)) {
				int[] index = token.getIndex();
				if (depthSeen.get(portName) == -1)
					depthSeen.put(portName, index.length);
				if (index.length >= depthSeen.get(portName)) {
					//storeToken(token, portName);
				}
			} else {
				logger
						.error("Result recieved for unexpected Port: "
								+ portName);
			}
		}
	}

//	public void saveOutputBaclavaDocument(Map<String,WorkflowDataToken> allResults) throws Exception {
//		if (outputBaclavaDocumentFile!=null) {
//			BaclavaDocumentHandler handler = new BaclavaDocumentHandler();
//			InvocationContext context = null;
//			Map<String,T2Reference> references = new HashMap<String, T2Reference>();
//			//fetch the references from the tokens, and pick up the context on the way
//			for (String portname : allResults.keySet()) {
//				WorkflowDataToken token = allResults.get(portname);
//				if (context==null) {
//					context=token.getContext();
//				}
//				references.put(portname, token.getData());
//			}
//			saveOutputBaclavaDocument(references);
//		}
//	}

//	public void saveOutputBaclavaDocument(Map<String,T2Reference> allResults) throws IOException {
//
//		if (outputBaclavaDocumentFile != null) {
//
//			if (outputBaclavaDocumentFile.getParentFile().exists()){
//				outputBaclavaDocumentFile.getParentFile().mkdirs();
//			}
//
//			BaclavaDocumentHandler handler = new BaclavaDocumentHandler();
//			InvocationContext context = null;
//			handler.setChosenReferences(allResults);
//
//			handler.setInvocationContext(context);
//			handler.setReferenceService(referenceService);
//
//			handler.saveData(outputBaclavaDocumentFile);
//		}
//	}

//	protected void storeToken(WorkflowDataToken token, String portName) {
//
//		if (token.getData().getReferenceType() == T2ReferenceType.IdentifiedList) {
//			saveList(token, portName);
//		} else {
//			File dataDirectory = rootOutputDirectory;
//			File dataFile = null;
//
//			if (token.getIndex().length > 0) {
//				dataDirectory = new File(rootOutputDirectory, portName);
//				for (int i = 0; i < token.getIndex().length - 1; i++) {
//					dataDirectory = new File(dataDirectory, String
//							.valueOf(token.getIndex()[i] + 1));
//				}
//				dataFile = new File(dataDirectory, String.valueOf(token
//						.getIndex()[token.getIndex().length - 1] + 1));
//			} else {
//				dataFile = new File(dataDirectory, portName);
//			}
//
//			if (!dataDirectory.exists()) {
//				dataDirectory.mkdirs();
//			}
//
//			if (dataFile.exists()) {
//				System.err.println("There is already data saved to: "
//						+ dataFile.getAbsolutePath());
//				System.exit(-1);
//			}
//
//			saveIndividualDataFile(token.getData(), dataFile, token
//					.getContext());
//		}
//	}

	/**
	 * Given the T2Reference to the data on an output port, saves the data on a disk in the
	 * output directory.
	 * @param workflowOutputPortName
	 * @param data
	 */
//	public void saveResultsForPort(String workflowOutputPortName, T2Reference t2Reference) {
//
//		if (t2Reference.getReferenceType() == T2ReferenceType.IdentifiedList) {
//			saveList(t2Reference, workflowOutputPortName);
//		} else {
//			File dataDirectory = rootOutputDirectory;
//			File dataFile = null;
//
//			dataFile = new File(dataDirectory, workflowOutputPortName);
//
//			if (!dataDirectory.exists()) {
//				dataDirectory.mkdirs();
//			}
//
//			if (dataFile.exists()) {
//				System.err.println("There is already data saved to: "
//						+ dataFile.getAbsolutePath());
//				//System.exit(-1);
//			}
//
//			saveIndividualDataFile(t2Reference, dataFile);
//		}
//	}

	public void saveResultsForPort(String workflowOutputPortName, Data data) {

		if (data.getDepth() > 0) {
			saveList(data, workflowOutputPortName);
		} else {
			File dataDirectory = rootOutputDirectory;
			File dataFile = null;

			dataFile = new File(dataDirectory, workflowOutputPortName);

			if (!dataDirectory.exists()) {
				dataDirectory.mkdirs();
			}

			if (dataFile.exists()) {
				System.err.println("There is already data saved to: "
						+ dataFile.getAbsolutePath());
				//System.exit(-1);
			}

			saveIndividualDataFile(data, dataFile);
		}
	}

//	private void saveList(T2Reference t2Reference, String portName) {
//
//		File dataDirectory = new File(rootOutputDirectory, portName);
//		IdentifiedList<T2Reference> list = referenceService.getListService().getList(t2Reference);
//		saveListItems(list, dataDirectory);
//	}

	private void saveList(Data data, String portName) {
		File dataDirectory = new File(rootOutputDirectory, portName);
		List<Data> list = data.getElements();
		saveListItems(list, dataDirectory);
	}

//	private void saveListItems(IdentifiedList<T2Reference> list, File dataDirectory) {
//		int c = 0;
//		if (!dataDirectory.exists()) {
//			dataDirectory.mkdirs();
//		}
//		for (T2Reference id : list) {
//			File dataFile = new File(dataDirectory, String.valueOf(c+1));
//			if (id.getReferenceType() ==  T2ReferenceType.IdentifiedList) {
//				IdentifiedList<T2Reference> innerList = referenceService.getListService().getList(id);
//				saveListItems(innerList, dataFile);
//			}
//			else {
//				saveIndividualDataFile(id, dataFile);
//			}
//			c++;
//		}
//	}

	private void saveListItems(List<Data> list, File dataDirectory) {
		int c = 0;
		if (!dataDirectory.exists()) {
			dataDirectory.mkdirs();
		}
		for (Data data : list) {
			File dataFile = new File(dataDirectory, String.valueOf(c+1));
			if (data.getDepth() > 0) {
				List<Data> innerList = data.getElements();
				saveListItems(innerList, dataFile);
			}
			else {
				saveIndividualDataFile(data, dataFile);
			}
			c++;
		}
	}

//	protected void saveIndividualDataFile(T2Reference reference, File dataFile) {
//
//		if (dataFile.exists()) {
//			System.err.println("There is already data saved to: "
//					+ dataFile.getAbsolutePath());
//			//System.exit(-1);
//		}
//
//		Object data = null;
//		InputStream stream = null;
//		try {
//			if (reference.containsErrors()) {
//				ErrorDocument errorDoc = referenceService
//						.getErrorDocumentService().getError(reference);
//				data = ErrorDocumentHandler.buildErrorDocumentString(errorDoc,
//						null);
//				dataFile = new File(dataFile.getAbsolutePath() + ".error");
//			} else {
//				// FIXME: this really should be done using a stream rather
//				// than an instance of the object in memory
//				Identified identified = referenceService.resolveIdentifier(
//						reference, null, null);
//				ReferenceSet referenceSet = (ReferenceSet) identified;
//
//				if (referenceSet.getExternalReferences().isEmpty()) {
//					data = referenceService.renderIdentifier(reference,
//							Object.class, null);
//				} else {
//					ExternalReferenceSPI externalReference = referenceSet
//							.getExternalReferences().iterator().next();
//					stream = externalReference.openStream(null);
//					data = stream;
//				}
//			}
//
//			FileOutputStream fos = null;
//			try {
//				fos = new FileOutputStream(dataFile);
//				if (data instanceof InputStream) {
//					byte[] bytes = new byte[500000];
//					int readBytes = 0 ;
//					while ((readBytes = ((InputStream) data).read(bytes)) != -1) {
//						fos.write(bytes, 0, readBytes);
//					}
//					stream.close();
//					fos.flush();
//				} else if (data instanceof byte[]) {
//					fos.write((byte[]) data);
//					fos.flush();
//				} else {
//					PrintWriter out = new PrintWriter(new OutputStreamWriter(
//							fos));
//					out.print(data.toString());
//					out.flush();
//					out.close();
//				}
//			} catch (FileNotFoundException e) {
//				logger.error(
//						"Unable to find the file: '"
//								+ dataFile.getAbsolutePath()
//								+ "' for writing results", e);
//			} catch (IOException e) {
//				logger.error(
//						"IO Error writing resuts to: '"
//								+ dataFile.getAbsolutePath(), e);
//			} finally {
//				if (fos != null) {
//					try {
//						fos.close();
//					} catch (IOException e) {
//						logger.error("Cannot close file output stream", e);
//					}
//				}
//			}
//		} finally {
//			if (stream != null) {
//				try {
//					stream.close();
//				} catch (IOException e) {
//					logger.error("Cannot close stream from reference", e);
//				}
//			}
//		}
//	}

	protected void saveIndividualDataFile(Data reference, File dataFile) {

		if (dataFile.exists()) {
			System.err.println("There is already data saved to: "
					+ dataFile.getAbsolutePath());
			//System.exit(-1);
		}

		Object value = null;
		if (reference.isError()) {
			value = ErrorDocumentHandler.buildErrorDocumentString((ErrorDocument) reference.getValue(),
					null);
			dataFile = new File(dataFile.getAbsolutePath() + ".error");
		} else {
			value = reference.getValue();
		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(dataFile);
			if (value instanceof byte[]) {
				fos.write((byte[]) value);
				fos.flush();
			} else {
				PrintWriter out = new PrintWriter(new OutputStreamWriter(
						fos));
				out.print(value.toString());
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			logger.error(
					"Unable to find the file: '"
							+ dataFile.getAbsolutePath()
							+ "' for writing results", e);
		} catch (IOException e) {
			logger.error(
					"IO Error writing resuts to: '"
							+ dataFile.getAbsolutePath(), e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					logger.error("Cannot close file output stream", e);
				}
			}
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
