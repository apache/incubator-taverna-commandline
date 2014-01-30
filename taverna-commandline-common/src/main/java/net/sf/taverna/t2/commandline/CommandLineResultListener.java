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
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.taverna.t2.commandline.data.SaveResultsHandler;
import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.facade.WorkflowInstanceFacade;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.T2Reference;

import org.apache.log4j.Logger;
import org.purl.wf4ever.provtaverna.export.Saver;

/**
 * A ResultListener that is using for collecting and storing results when running
 * workflows from the commandline.
 * 
 * @author Stuart Owen 
 */
public class CommandLineResultListener implements ResultListener {
	
	private static final Logger logger = Logger.getLogger(CommandLineResultListener.class);
	
	private Map<String, WorkflowDataToken> outputMap = new HashMap<String, WorkflowDataToken>();	
	private Map<String,WorkflowDataToken> finalTokens = new HashMap<String, WorkflowDataToken>();	
	private final SaveResultsHandler saveResultsHandler;
	//private final int numberOfOutputs;
	private final boolean saveIndividualResults;
	private final boolean saveOutputDocument;

	//private boolean saveOpm;

	//private boolean saveJanus;
	
	private boolean exportProvBundle;
	private File provBundleFile;

	private WorkflowInstanceFacade facade;
	
	//private final String workflowRunId;

	public CommandLineResultListener(int numberOfOutputs,
			SaveResultsHandler saveResultsHandler,
			boolean saveIndividualResults, boolean saveOutputDocument,
			boolean saveOpm, boolean saveJanus, boolean exportProvBundle, File provBundleFile, 
			WorkflowInstanceFacade facade) {
		//this.numberOfOutputs = numberOfOutputs;
		this.saveResultsHandler = saveResultsHandler;
		this.saveIndividualResults = saveIndividualResults;
		this.saveOutputDocument = saveOutputDocument;	
		//this.saveOpm = saveOpm;
		//this.saveJanus = saveJanus;
		this.exportProvBundle = exportProvBundle;
		this.provBundleFile = provBundleFile;
		this.facade = facade;
		//this.workflowRunId = facade.getWorkflowRunId();
	}

	public Map<String, WorkflowDataToken> getOutputMap() {
		return outputMap;
	}	

	public void resultTokenProduced(WorkflowDataToken token, String portName) {		
		if (saveIndividualResults) {
			saveResultsHandler.tokenReceived(token, portName);
		}
		
		if (token.isFinal()) {
			finalTokens.put(portName, token);			
		}
	}
	
	public void saveOutputDocument() {
		if (saveOutputDocument) {
			try {
				saveResultsHandler.saveOutputDocument(finalTokens);
			} catch (Exception e) {
				logger.error("An error occurred saving the final results to -outputdoc",e);
			}
		}
	}

	public void saveProvenance() {
		// if (saveOpm) {
		// saveResultsHandler.saveOpm(workflowRunId);
		// }
		// if (saveJanus) {
		// saveResultsHandler.saveJanus(workflowRunId);
		// }
		if (exportProvBundle)
			if (provBundleFile != null) {
				saveProvenanceBundle();
			} else {
				System.err
						.println("Provenance bundle export file has not been specified.");
				System.exit(-1);
			}
	}

	public void saveProvenanceBundle() {
		Map<String, T2Reference> chosenReferences = new HashMap<String, T2Reference>();
		for (Entry<String, WorkflowDataToken> entry : getOutputMap()
				.entrySet()) {
			chosenReferences.put(entry.getKey(), entry.getValue()
					.getData());
		}
		Saver saver = new Saver(facade.getContext()
				.getReferenceService(), facade.getContext(),
				facade.getWorkflowRunId(), chosenReferences);
		//saver.setFileToId(fileToId);
		Path bundle = provBundleFile.toPath().toAbsolutePath();
		try {
			saver.saveData(bundle);
		} catch (IOException e1) {
			System.err.println("Can't store output to "
					+ provBundleFile + ": " + e1);
			System.exit(-1);
		}
	}

}
