package net.sf.taverna.t2.commandline;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.invocation.WorkflowDataToken;

public class CommandLineResultListener implements ResultListener {
	
	private Map<String, WorkflowDataToken> outputMap = new HashMap<String, WorkflowDataToken>();
	private final Map<String, Integer> outputPortNamesAndDepth;
	private SaveResultsHandler saveResultsHandler;
	private List<String> finishedPorts = new ArrayList<String>();

	public CommandLineResultListener(Map<String, Integer> outputPortNamesAndDepth, File outputDirectory) {
		this.outputPortNamesAndDepth = outputPortNamesAndDepth;		
		this.saveResultsHandler = new SaveResultsHandler(outputPortNamesAndDepth, outputDirectory);
	}

	public void resultTokenProduced(WorkflowDataToken token, String portName) {
		saveResultsHandler.tokenReceived(token, portName);				
		if (token.isFinal()) finishedPorts.add(portName);
	}

	public Map<String, WorkflowDataToken> getOutputMap() {
		return outputMap;
	}

	public boolean isComplete() {		
		return finishedPorts.size() == outputPortNamesAndDepth.size();
	}

}
