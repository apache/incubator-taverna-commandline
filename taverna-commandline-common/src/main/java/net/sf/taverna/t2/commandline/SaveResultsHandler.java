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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;

import org.apache.log4j.Logger;

public class SaveResultsHandler {

	private final Map<String, Integer> portsAndDepth;
	private HashMap<String, Integer> depthSeen;
	private final File rootDirectory;
	private static Logger logger = Logger
			.getLogger(CommandLineResultListener.class);
	private final File outputDocumentFile;	

	public SaveResultsHandler(Map<String, Integer> portsAndDepth,
			File rootDirectory, File outputDocumentFile) {

		this.portsAndDepth = portsAndDepth;
		this.rootDirectory = rootDirectory;
		this.outputDocumentFile = outputDocumentFile;

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
			new BaclavaDocumentHandler().storeDocument(allResults, outputDocumentFile);
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
							.valueOf(token.getIndex()[i]));
				}
				dataFile = new File(dataDirectory, String.valueOf(token
						.getIndex()[token.getIndex().length - 1]));
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

		if (token.getIndex().length > 0) {
			dataDirectory = new File(rootDirectory, portName);
			for (int i = 0; i < index.length - 1; i++) {
				dataDirectory = new File(dataDirectory, String.valueOf(token
						.getIndex()[i]));
			}
			dataDirectory = new File(dataDirectory, String.valueOf(token
					.getIndex()[index.length - 1]));
		} else {
			dataDirectory = new File(rootDirectory, portName);
		}
		if (!dataDirectory.exists()) {
			dataDirectory.mkdirs();
		}

		T2Reference reference = token.getData();
		IdentifiedList<T2Reference> list = token.getContext()
				.getReferenceService().getListService().getList(reference);
		int c = 0;
		for (T2Reference id : list) {
			File dataFile = new File(dataDirectory, String.valueOf(c));
			saveIndividualDataFile(id, dataFile, token.getContext());
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
		if (reference.containsErrors()) {
			ErrorDocument errorDoc = context.getReferenceService()
			.getErrorDocumentService().getError(reference);
			data = ErrorDocumentHandler.buildErrorDocumentString(errorDoc, context);			
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
				data = externalReference.openStream(context);
			}			
		}

		FileOutputStream fos;
		try {
			fos = new FileOutputStream(dataFile);
			if (data instanceof InputStream) {			
				InputStream inStream = (InputStream)data;
				int c;
				while ( ( c = inStream.read() ) != -1  ) {
					fos.write( (char) c);
				}				
				fos.flush();
				fos.close();
			}
			if (data instanceof byte[]) {
				fos.write((byte[]) data);
				fos.flush();
				fos.close();
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
		}
	}
	
	
}
