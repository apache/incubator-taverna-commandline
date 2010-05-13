package net.sf.taverna.t2.commandline;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestCommandLineOptionsHandler {

	@Test
	public void testWorkflowName() throws Exception {
		CommandLineOptionsHandler handler = new CommandLineOptionsHandler(new String[]{"myworkflow.t2flow"});
		assertEquals("myworkflow.t2flow",handler.getWorkflow());
	}
	
	@Test(expected=InvalidOptionException.class)
	public void noWorkflowName() throws Exception {
		CommandLineOptionsHandler handler = new CommandLineOptionsHandler(new String[]{});
		handler.getWorkflow();
	}
	
	@Test
	public void testInMemory() throws Exception {
		CommandLineOptionsHandler handler = new CommandLineOptionsHandler(new String[]{"-inmemory","myworkflow.t2flow"});
		assertTrue(handler.hasOption("inmemory"));
	}
	
	@Test
	public void testEmbedded() throws Exception {
		CommandLineOptionsHandler handler = new CommandLineOptionsHandler(new String[]{"-embedded","myworkflow.t2flow"});
		assertTrue(handler.hasOption("embedded"));
	}
	
	@Test
	public void testClientServer() throws Exception {
		CommandLineOptionsHandler handler = new CommandLineOptionsHandler(new String[]{"-clientserver","myworkflow.t2flow"});
		assertTrue(handler.hasOption("clientserver"));
	}
	
	@Test(expected=InvalidOptionException.class)
	public void testInvalidEmbeddedAndClientServer() throws Exception {
		new CommandLineOptionsHandler(new String[]{"-clientserver","-embedded","myworkflow.t2flow"});
	}
	
	@Test(expected=InvalidOptionException.class)
	public void testInvalidEmbeddedAndMemory() throws Exception {
		new CommandLineOptionsHandler(new String[]{"-embedded","-inmemory","myworkflow.t2flow"});
	}
	
	@Test(expected=InvalidOptionException.class)
	public void testInvalidClientServerAndInMemory() throws Exception {
		new CommandLineOptionsHandler(new String[]{"-clientserver","-inmemory","myworkflow.t2flow"});
	}
	
}
