package net.sf.taverna.t2.commandline;

import net.sf.taverna.t2.commandline.exceptions.InvalidOptionException;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestCommandLineOptionsHandler {

	@Test
	public void testWorkflowName() throws Exception {
		CommandLineOptions handler = new CommandLineOptions(new String[]{"myworkflow.t2flow"});
		assertEquals("myworkflow.t2flow",handler.getWorkflow());
	}
	
	@Test(expected=InvalidOptionException.class)
	public void noWorkflowName() throws Exception {
		CommandLineOptions handler = new CommandLineOptions(new String[]{});
		handler.getWorkflow();
	}
	
	@Test
	public void noWorkflowNameButHelp() throws Exception {
		//should not throw an error
		new CommandLineOptions(new String[]{"-help"});
	}
	
	@Test(expected=InvalidOptionException.class)
	public void provenanceButNoDatabase() throws Exception {
		new CommandLineOptions(new String[]{"-provenance","myworkflow.t2flow"});
	}
	
	@Test(expected=InvalidOptionException.class)
	public void provenanceButNoDatabase2() throws Exception {
		new CommandLineOptions(new String[]{"-provenance","-inmemory","myworkflow.t2flow"});
	}
	
	@Test
	public void provenanceDatabase() throws Exception {
		//should be no errors
		new CommandLineOptions(new String[]{"-provenance","-embedded","myworkflow.t2flow"});
		new CommandLineOptions(new String[]{"-provenance","-clientserver","myworkflow.t2flow"});
		new CommandLineOptions(new String[]{"-provenance","-dbproperties","dbproperties.properties","myworkflow.t2flow"});
	}
	
	@Test
	public void testInMemory() throws Exception {
		CommandLineOptions handler = new CommandLineOptions(new String[]{"-inmemory","myworkflow.t2flow"});
		assertTrue(handler.hasOption("inmemory"));
	}
	
	@Test
	public void testEmbedded() throws Exception {
		CommandLineOptions handler = new CommandLineOptions(new String[]{"-embedded","myworkflow.t2flow"});
		assertTrue(handler.hasOption("embedded"));
	}
	
	@Test
	public void testClientServer() throws Exception {
		CommandLineOptions handler = new CommandLineOptions(new String[]{"-clientserver","myworkflow.t2flow"});
		assertTrue(handler.hasOption("clientserver"));
	}
	
	@Test(expected=InvalidOptionException.class)
	public void testInvalidEmbeddedAndClientServer() throws Exception {
		new CommandLineOptions(new String[]{"-clientserver","-embedded","myworkflow.t2flow"});
	}
	
	@Test(expected=InvalidOptionException.class)
	public void testInvalidEmbeddedAndMemory() throws Exception {
		new CommandLineOptions(new String[]{"-embedded","-inmemory","myworkflow.t2flow"});
	}
	
	@Test(expected=InvalidOptionException.class)
	public void testInvalidClientServerAndInMemory() throws Exception {
		new CommandLineOptions(new String[]{"-clientserver","-inmemory","myworkflow.t2flow"});
	}
	
}
