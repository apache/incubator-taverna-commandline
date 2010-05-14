package net.sf.taverna.t2.commandline;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.invocation.WorkflowDataToken;

public class CommandLineResultListener implements ResultListener {
	
	private Map<String, WorkflowDataToken> outputMap = new HashMap<String, WorkflowDataToken>();	
	private Map<String,WorkflowDataToken> finalTokens = new HashMap<String, WorkflowDataToken>();	
	private final SaveResultsHandler saveResultsHandler;
	private final int numberOfOutputs;
	private final boolean saveIndividualResults;
	private final boolean saveOutputDocument;

	public CommandLineResultListener(int numberOfOutputs,SaveResultsHandler saveResultsHandler,boolean saveIndividualResults,boolean saveOutputDocument) {		
		this.numberOfOutputs = numberOfOutputs;
		this.saveResultsHandler = saveResultsHandler;
		this.saveIndividualResults = saveIndividualResults;
		this.saveOutputDocument = saveOutputDocument;						
	}

	public void resultTokenProduced(WorkflowDataToken token, String portName) {		
		if (saveIndividualResults) {
			saveResultsHandler.tokenReceived(token, portName);
		}
		
		if (token.isFinal()) {
			finalTokens.put(portName, token);		
			if (isComplete() && saveOutputDocument) {
				saveResultsHandler.saveOutputDocument(finalTokens);
			}
		}
	}

	public Map<String, WorkflowDataToken> getOutputMap() {
		return outputMap;
	}

	public boolean isComplete() {		
		return finalTokens.size() == numberOfOutputs;
	}

}
