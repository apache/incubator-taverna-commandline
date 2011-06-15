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
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.commandline.CommandLineResultListener;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.lang.baclava.BaclavaDocumentHandler;
import net.sf.taverna.t2.provenance.api.ProvenanceAccess;
import net.sf.taverna.t2.provenance.client.ProvenanceExporter;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;

import org.apache.log4j.Logger;

/**
 * Handles all recording of results as they are received by the {@link CommandLineResultListener}
 * or when the workflow enactment has completed.
 * This includes saving as a Baclava Document, or storing individual results.
 * 
 * @author Stuart Owen
 * @see BaclavaHandler
 * @see CommandLineResultListener
 *
 */
public class SaveResultsHandler {

	private final Map<String, Integer> portsAndDepth;
	private HashMap<String, Integer> depthSeen;
	private final File rootDirectory;
	private static Logger logger = Logger
			.getLogger(CommandLineResultListener.class);
	private final File outputDocumentFile;
	private final File janus;
	private final File opm;
	private ProvenanceExporter provExport;	

	public SaveResultsHandler(Map<String, Integer> portsAndDepth,
			File rootDirectory, File outputDocumentFile, File janus, File opm) {

		this.portsAndDepth = portsAndDepth;
		this.rootDirectory = rootDirectory;
		this.outputDocumentFile = outputDocumentFile;
		this.janus = janus;
		this.opm = opm;

		depthSeen = new HashMap<String, Integer>();
		for (String portName : portsAndDepth.keySet()) {
			depthSeen.put(portName, -1);
		}
	}

	public void tokenReceived(WorkflowDataToken token, String portName) {
		if (rootDirectory != null) { //only save individual results if a directory is specified
			if (portsAndDepth.containsKey(portName)) {
				int[] index = token.getIndex();
				if (depthSeen.get(portName) == -1)
					depthSeen.put(portName, index.length);
				if (index.length >= depthSeen.get(portName)) {
					storeToken(token, portName);
				}
			} else {
				logger
						.error("Result recieved for unexpected Port: "
								+ portName);
			}
		}
	}
	
	public void saveOutputDocument(Map<String,WorkflowDataToken> allResults) throws Exception {
		if (outputDocumentFile!=null) {			
			BaclavaDocumentHandler handler = new BaclavaDocumentHandler();
			InvocationContext context = null;
			Map<String,T2Reference> references = new HashMap<String, T2Reference>();			
			//fetch the references from the tokens, and pick up the context on the way
			for (String portname : allResults.keySet()) {
				WorkflowDataToken token = allResults.get(portname);
				if (context==null) {
					context=token.getContext();
				}
				references.put(portname, token.getData());
			} 
			handler.setChosenReferences(references);
			
			handler.setInvocationContext(context);
			ReferenceService referenceService=null;
			if (context!=null) referenceService = context.getReferenceService();
			handler.setReferenceService(referenceService);
			
			handler.saveData(outputDocumentFile);
		}
	}

	protected void storeToken(WorkflowDataToken token, String portName) {

		if (token.getData().getReferenceType() == T2ReferenceType.IdentifiedList) {
			saveList(token, portName);
		} else {
			File dataDirectory = rootDirectory;
			File dataFile = null;

			if (token.getIndex().length > 0) {
				dataDirectory = new File(rootDirectory, portName);
				for (int i = 0; i < token.getIndex().length - 1; i++) {
					dataDirectory = new File(dataDirectory, String
							.valueOf(token.getIndex()[i] + 1));
				}
				dataFile = new File(dataDirectory, String.valueOf(token
						.getIndex()[token.getIndex().length - 1] + 1));
			} else {
				dataFile = new File(dataDirectory, portName);
			}
			
			if (!dataDirectory.exists()) {
				dataDirectory.mkdirs();
			}

			if (dataFile.exists()) {
				System.err.println("There is already data saved to: "
						+ dataFile.getAbsolutePath());
				System.exit(-1);
			}
			
			saveIndividualDataFile(token.getData(), dataFile, token
					.getContext());
		}
	}

	private void saveList(WorkflowDataToken token, String portName) {
		File dataDirectory = null;
		int[] index = token.getIndex();

		if (index.length > 0) {
			dataDirectory = new File(rootDirectory, portName);
			for (int i = 0; i < index.length - 1; i++) {
				dataDirectory = new File(dataDirectory, String.valueOf(token
						.getIndex()[i] + 1));
			}
			dataDirectory = new File(dataDirectory, String.valueOf(token
					.getIndex()[index.length - 1]+ 1));
		} else {
			dataDirectory = new File(rootDirectory, portName);
		}
		
		T2Reference reference = token.getData();
		IdentifiedList<T2Reference> list = token.getContext()
				.getReferenceService().getListService().getList(reference);
		saveListItems(token.getContext(), dataDirectory,  list);
	}

	private void saveListItems(InvocationContext context, File dataDirectory,IdentifiedList<T2Reference> list) {
		int c = 0;
		if (!dataDirectory.exists()) {
			dataDirectory.mkdirs();
		}
		for (T2Reference id : list) {			
			File dataFile = new File(dataDirectory, String.valueOf(c+1));
			if (id.getReferenceType() ==  T2ReferenceType.IdentifiedList) {
				IdentifiedList<T2Reference> innerList = context
				.getReferenceService().getListService().getList(id);				
				saveListItems(context, dataFile, innerList);
			}
			else {				
				saveIndividualDataFile(id, dataFile, context);				
			}
			c++;
		}
	}

	protected void saveIndividualDataFile(T2Reference reference, File dataFile,
			InvocationContext context) {

		if (dataFile.exists()) {
			System.err.println("There is already data saved to: "
					+ dataFile.getAbsolutePath());
			System.exit(-1);
		}

		Object data = null;
		InputStream stream = null;
		try {
		if (reference.containsErrors()) {
			ErrorDocument errorDoc = context.getReferenceService()
			.getErrorDocumentService().getError(reference);
			data = ErrorDocumentHandler.buildErrorDocumentString(errorDoc, context);
			dataFile = new File(dataFile.getAbsolutePath()+".error");
		} else {
			// FIXME: this really should be done using a stream rather
			// than an instance of the object in memory			
			
			Identified identified = context.getReferenceService().resolveIdentifier(reference, null, context);
			ReferenceSet referenceSet = (ReferenceSet) identified;
			
			if (referenceSet.getExternalReferences().isEmpty()) {
				data = context.getReferenceService().renderIdentifier(reference,
						Object.class, context);
			}
			else {
				ExternalReferenceSPI externalReference = referenceSet.getExternalReferences().iterator().next();				
				stream = externalReference.openStream(context);
				data = stream;
			}			
		}

		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(dataFile);
			if (data instanceof InputStream) {			

				int c;
				while ( ( c = stream.read() ) != -1  ) {
					fos.write( (char) c);
				}
				stream.close();
				fos.flush();
				//fos.close();
			}
			if (data instanceof byte[]) {
				fos.write((byte[]) data);
				fos.flush();
				//fos.close();
			} else {
				PrintWriter out = new PrintWriter(new OutputStreamWriter(fos));
				out.print(data.toString());
				out.flush();
				out.close();
			}
		} catch (FileNotFoundException e) {
			logger.error("Unable to find the file: '"
					+ dataFile.getAbsolutePath() + "' for writing results", e);
		} catch (IOException e) {
			logger.error("IO Error writing resuts to: '"
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
	} finally {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				logger.error("Cannot close stream from reference", e);
			}
		}
	}
	}

	public void saveOpm(String workflowRunId) {
		opm.getParentFile().mkdirs();
		BufferedOutputStream outStream;
		try {
			outStream = new BufferedOutputStream(new FileOutputStream(opm));
		} catch (FileNotFoundException e1) {
			logger.error("Can't find directory for writing OPM to " + opm, e1);
			return;
		}
		try {
			getProvenanceExporter().exportAsOPMRDF(workflowRunId, outStream);
		} catch (Exception e) {
			logger.error("Can't write OPM to " + opm, e);
		} finally {
			try {
				outStream.close();
			} catch (IOException e) {
			}
		}
	}

	protected synchronized ProvenanceExporter getProvenanceExporter() {
		if (provExport == null) {
			DataManagementConfiguration dbConfig = DataManagementConfiguration.getInstance();
			String connectorType = dbConfig.getConnectorType();
			ProvenanceAccess provAccess = new ProvenanceAccess(connectorType);
			provExport = new ProvenanceExporter(provAccess);
		}
		return provExport;
	}

	public void saveJanus(String workflowRunId) {
		janus.getParentFile().mkdirs();
		BufferedOutputStream outStream;
		try {
			outStream = new BufferedOutputStream(new FileOutputStream(janus));
		} catch (FileNotFoundException e1) {
			logger.error("Can't find directory for writing Janus to " + janus, e1);
			return;
		}
		try {
			getProvenanceExporter().exportAsJanusRDF(workflowRunId, outStream);
		} catch (Exception e) {
			logger.error("Can't write Janus to " + janus, e);
		} finally {
			try {
				outStream.close();
			} catch (IOException e) {
			}
		}
	}
	
	
}
