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

	public CommandLineResultListener(int numberOfOutputs,SaveResultsHandler saveResultsHandler) {		
		this.numberOfOutputs = numberOfOutputs;
		this.saveResultsHandler = saveResultsHandler;						
	}

	public void resultTokenProduced(WorkflowDataToken token, String portName) {		
		saveResultsHandler.tokenReceived(token, portName);
		
		if (token.isFinal()) {
			finalTokens.put(portName, token);		
			if (isComplete()) {
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
