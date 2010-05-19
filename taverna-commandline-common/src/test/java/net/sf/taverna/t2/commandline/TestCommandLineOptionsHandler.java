package net.sf.taverna.t2.commandline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.commandline.exceptions.InvalidOptionException;

import org.junit.Test;

public class TestCommandLineOptionsHandler {

	@Test
	public void testWorkflowName() throws Exception {
		CommandLineOptions handler = new CommandLineOptions(new String[]{"myworkflow.t2flow"});
		assertEquals("myworkflow.t2flow",handler.getWorkflow());
	}
	
	@Test(expected=InvalidOptionException.class)
	public void noWorkflowName() throws Exception {
		new CommandLineOptions(new String[]{});		
	}
	
	@Test
	public void getWorkflow() throws Exception {
		CommandLineOptions options = new CommandLineOptions(new String[]{"-help"});
		assertNull(options.getWorkflow());
		options = new CommandLineOptions(new String[]{"myworkflow.t2flow"});
		assertEquals("myworkflow.t2flow", options.getWorkflow());
	}
	
	@Test(expected=InvalidOptionException.class)
	public void cannotProvideInputsAndInputDoc() throws Exception {
		new CommandLineOptions(new String[]{"-input","fred","fred.txt","-inputdoc","myworkflow.t2flow"});
	}
	
	@Test
	public void getInputs() throws Exception {
		CommandLineOptions options = new CommandLineOptions(new String[]{"-input","fred","fred.txt","myworkflow.t2flow"});
		assertEquals(2, options.getInputs().length);
		assertEquals("fred",options.getInputs()[0]);
		assertEquals("fred.txt",options.getInputs()[1]);
		
		options = new CommandLineOptions(new String[]{"-input","fred","fred.txt","-input","fred2","fred2.txt","myworkflow.t2flow"});
		assertEquals(4, options.getInputs().length);
		assertEquals("fred",options.getInputs()[0]);
		assertEquals("fred.txt",options.getInputs()[1]);
		assertEquals("fred2",options.getInputs()[2]);
		assertEquals("fred2.txt",options.getInputs()[3]);
		
		options = new CommandLineOptions(new String[]{"myworkflow.t2flow"});
		assertNotNull(options.getInputs());
		assertEquals(0, options.getInputs().length);		
				
	}
	
	@Test
	public void hasInputs() throws Exception {
		CommandLineOptions options = new CommandLineOptions(new String[]{"-input","fred","fred.txt","myworkflow.t2flow"});
		assertTrue(options.hasInputs());
		
		options = new CommandLineOptions(new String[]{"myworkflow.t2flow"});
		assertFalse(options.hasInputs());
	}
	
	@Test
	public void noWorkflowNameButHelp() throws Exception {
		//should not throw an error
		new CommandLineOptions(new String[]{"-help"});
	}
	
	@Test
	public void noWorkflowNameButStartDB() throws Exception {
		//should not throw an error				
		CommandLineOptions options = new CommandLineOptions(new String[]{"-startdb"});
		assertTrue(options.getStartDatabase());
		assertTrue(options.getStartDatabaseOnly());
	}

	@Test
	public void workflowNameAndStartDB() throws Exception {
		//should not throw an error
		CommandLineOptions options = new CommandLineOptions(new String[]{"-startdb","myworkflow.t2flow"});
		assertTrue(options.getStartDatabase());
		assertFalse(options.getStartDatabaseOnly());
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
	
	@Test
	public void isInMemory() throws Exception {
		CommandLineOptions options = new CommandLineOptions(new String [] {"-inmemory","myworkflow.t2flow"});
		
		assertTrue(options.isInMemory());
		assertFalse(options.isClientServer());
		assertFalse(options.isEmbedded());
	}
	
	@Test
	public void isClientServer() throws Exception {
		CommandLineOptions options = new CommandLineOptions(new String [] {"-clientserver","myworkflow.t2flow"});
		
		assertTrue(options.isClientServer());
		assertFalse(options.isInMemory());		
		assertFalse(options.isEmbedded());
	}
	
	@Test
	public void isEmbedded() throws Exception {
		CommandLineOptions options = new CommandLineOptions(new String [] {"-embedded","myworkflow.t2flow"});
		
		assertTrue(options.isEmbedded());
		assertFalse(options.isInMemory());
		assertFalse(options.isClientServer());		
	}
	
}
