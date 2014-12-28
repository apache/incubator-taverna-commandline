package uk.org.taverna.commandline;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import net.sf.taverna.t2.commandline.exceptions.InvalidOptionException;
import net.sf.taverna.t2.commandline.options.CommandLineOptions;

import org.junit.Test;

public class TestCommandLineOptionsHandler {

	@Test
	public void testWorkflowName() throws Exception {
		CommandLineOptions handler = new CommandLineOptionsImpl(
				new String[] { "myworkflow.t2flow" });
		assertEquals("myworkflow.t2flow", handler.getWorkflow());
	}

	@Test
	public void shouldShowHelp() throws Exception {
		CommandLineOptions options = new CommandLineOptionsImpl(
				new String[] { "-help" });
		assertTrue(options.askedForHelp());
		options = new CommandLineOptionsImpl(
				new String[] {});
		assertTrue(options.askedForHelp());
		options = new CommandLineOptionsImpl(new String[] { "myworkflow.t2flow" });
		assertFalse(options.askedForHelp());
	}

	@Test
	public void getWorkflow() throws Exception {
		CommandLineOptions options = new CommandLineOptionsImpl(
				new String[] { "-help" });
		assertNull(options.getWorkflow());
		options = new CommandLineOptionsImpl(new String[] { "myworkflow.t2flow" });
		assertEquals("myworkflow.t2flow", options.getWorkflow());
	}

	@Test(expected = InvalidOptionException.class)
	public void cannotProvideInputFileAndInputDoc() throws Exception {
		new CommandLineOptionsImpl(new String[] { "-inputfile", "fred", "fred.txt",
				"-inputdoc", "myworkflow.t2flow" });
	}

	@Test(expected = InvalidOptionException.class)
	public void cannotProvideInputValueAndInputDoc() throws Exception {
		new CommandLineOptionsImpl(new String[] { "-inputvalue", "fred", "fred.txt",
				"-inputdoc", "myworkflow.t2flow" });
	}

	@Test
	public void canProvideInputValueAndFileTogether() throws Exception {
		//should not be an error
		new CommandLineOptionsImpl(new String[] { "-inputvalue", "fred", "abc",
				"-inputfile","fred2","fred2.txt","myworkflow.t2flow" });
	}

	@Test
	public void getInputs() throws Exception {
		CommandLineOptions options = new CommandLineOptionsImpl(new String[] {
				"-inputfile", "fred", "fred.txt", "myworkflow.t2flow" });
		assertEquals(2, options.getInputFiles().length);
		assertEquals("fred", options.getInputFiles()[0]);
		assertEquals("fred.txt", options.getInputFiles()[1]);

		options = new CommandLineOptionsImpl(new String[] { "-inputfile", "fred",
				"fred.txt", "-inputfile", "fred2", "fred2.txt",
				"myworkflow.t2flow" });
		assertEquals(4, options.getInputFiles().length);
		assertEquals("fred", options.getInputFiles()[0]);
		assertEquals("fred.txt", options.getInputFiles()[1]);
		assertEquals("fred2", options.getInputFiles()[2]);
		assertEquals("fred2.txt", options.getInputFiles()[3]);

		options = new CommandLineOptionsImpl(new String[] { "myworkflow.t2flow" });
		assertNotNull(options.getInputFiles());
		assertEquals(0, options.getInputFiles().length);

	}

	@Test
	public void hasInputValue() throws Exception {
		CommandLineOptions options = new CommandLineOptionsImpl(new String[] {
				"-inputvalue", "fred", "abc", "myworkflow.t2flow" });
		assertTrue(options.hasInputValues());

		options = new CommandLineOptionsImpl(new String[] { "myworkflow.t2flow" });
		assertFalse(options.hasInputValues());
	}

	@Test
	public void getInputValues() throws Exception {
		CommandLineOptions options = new CommandLineOptionsImpl(new String[] {
				"-inputvalue", "fred", "abc", "myworkflow.t2flow" });
		assertEquals(2, options.getInputValues().length);

		options = new CommandLineOptionsImpl(new String[] { "myworkflow.t2flow" });
		assertNotNull(options.getInputValues());
		assertEquals(0,options.getInputValues().length);
	}

	@Test
	public void hasInputs() throws Exception {
		CommandLineOptions options = new CommandLineOptionsImpl(new String[] {
				"-inputfile", "fred", "fred.txt", "myworkflow.t2flow" });
		assertTrue(options.hasInputFiles());

		options = new CommandLineOptionsImpl(new String[] { "myworkflow.t2flow" });
		assertFalse(options.hasInputFiles());
	}

	@Test
	public void noWorkflowNameButStartDB() throws Exception {
		// should not throw an error
		CommandLineOptions options = new CommandLineOptionsImpl(
				new String[] { "-startdb" });
		assertTrue(options.getStartDatabase());
		assertTrue(options.getStartDatabaseOnly());
	}

	@Test
	public void workflowNameAndStartDB() throws Exception {
		// should not throw an error
		CommandLineOptions options = new CommandLineOptionsImpl(new String[] {
				"-startdb", "myworkflow.t2flow" });
		assertTrue(options.getStartDatabase());
		assertFalse(options.getStartDatabaseOnly());
	}

	@Test(expected = InvalidOptionException.class)
	public void provenanceButNoDatabase() throws Exception {
		new CommandLineOptionsImpl(new String[] { "-provenance",
				"myworkflow.t2flow" });
	}

	@Test(expected = InvalidOptionException.class)
	public void provenanceButNoDatabase2() throws Exception {
		new CommandLineOptionsImpl(new String[] { "-provenance", "-inmemory",
				"myworkflow.t2flow" });
	}

	@Test
	public void provenanceDatabase() throws Exception {
		// should be no errors
		new CommandLineOptionsImpl(new String[] { "-provenance", "-embedded",
				"myworkflow.t2flow" });
		new CommandLineOptionsImpl(new String[] { "-provenance", "-clientserver",
				"myworkflow.t2flow" });
		new CommandLineOptionsImpl(new String[] { "-provenance", "-dbproperties",
				"dbproperties.properties", "myworkflow.t2flow" });
	}

	@Test
	public void testHasInputDelimiter() throws Exception {
		CommandLineOptions options = new CommandLineOptionsImpl(new String[] {
				"-inputvalue","in1","1,2,3","-inputdelimiter","in1",",","-inputdelimiter","in2",",","myworkflow.t2flow" });
		assertTrue(options.hasDelimiterFor("in1"));
		assertTrue(options.hasDelimiterFor("in2"));
		assertFalse(options.hasDelimiterFor("in3"));
	}

	@Test(expected = InvalidOptionException.class)
	public void testInputDelimiterInvalidWithInputDoc() throws Exception {
		new CommandLineOptionsImpl(new String[] {
				"-inputdoc","doc.xml","-inputdelimiter","in1",",","myworkflow.t2flow" });
	}


	@Test
	public void testInputDelimiter() throws Exception {
		CommandLineOptions options = new CommandLineOptionsImpl(new String[] {
				"-inputvalue","in1","1,2,3","-inputdelimiter","in1",",","-inputdelimiter","in2","!","myworkflow.t2flow" });
		assertEquals(",",options.inputDelimiter("in1"));
		assertEquals("!",options.inputDelimiter("in2"));
		assertNull(options.inputDelimiter("in3"));
	}

	@Test
	public void testInMemory() throws Exception {
		CommandLineOptions handler = new CommandLineOptionsImpl(new String[] {
				"-inmemory", "myworkflow.t2flow" });
		assertTrue(handler.hasOption("inmemory"));
	}

	@Test
	public void testEmbedded() throws Exception {
		CommandLineOptions handler = new CommandLineOptionsImpl(new String[] {
				"-embedded", "myworkflow.t2flow" });
		assertTrue(handler.hasOption("embedded"));
	}

	@Test
	public void testClientServer() throws Exception {
		CommandLineOptions handler = new CommandLineOptionsImpl(new String[] {
				"-clientserver", "myworkflow.t2flow" });
		assertTrue(handler.hasOption("clientserver"));
	}

	@Test(expected = InvalidOptionException.class)
	public void testInvalidEmbeddedAndClientServer() throws Exception {
		new CommandLineOptionsImpl(new String[] { "-clientserver", "-embedded",
				"myworkflow.t2flow" });
	}

	@Test(expected = InvalidOptionException.class)
	public void testInvalidEmbeddedAndMemory() throws Exception {
		new CommandLineOptionsImpl(new String[] { "-embedded", "-inmemory",
				"myworkflow.t2flow" });
	}

	@Test(expected = InvalidOptionException.class)
	public void testInvalidClientServerAndInMemory() throws Exception {
		new CommandLineOptionsImpl(new String[] { "-clientserver", "-inmemory",
				"myworkflow.t2flow" });
	}

	@Test
	public void isInMemory() throws Exception {
		CommandLineOptions options = new CommandLineOptionsImpl(new String[] {
				"-inmemory", "myworkflow.t2flow" });

		assertTrue(options.isInMemory());
		assertFalse(options.isClientServer());
		assertFalse(options.isEmbedded());
	}

	@Test
	public void isClientServer() throws Exception {
		CommandLineOptions options = new CommandLineOptionsImpl(new String[] {
				"-clientserver", "myworkflow.t2flow" });

		assertTrue(options.isClientServer());
		assertFalse(options.isInMemory());
		assertFalse(options.isEmbedded());
	}

	@Test
	public void hasLogFile() throws Exception {
		CommandLineOptions options = new CommandLineOptionsImpl(new String[] {
				"-logfile","/tmp/logging", "myworkflow.t2flow" });

		assertTrue(options.hasLogFile());
		assertEquals("/tmp/logging", options.getLogFile());
	}

	@Test(expected = InvalidOptionException.class)
	@SuppressWarnings("unused")
	public void hasLogFileNotValidWithoutWorkflow() throws Exception{
		CommandLineOptions options = new CommandLineOptionsImpl(new String[] {
				"-logfile","/tmp/logging"});
	}

	@Test
	public void isEmbedded() throws Exception {
		CommandLineOptions options = new CommandLineOptionsImpl(new String[] {
				"-embedded", "myworkflow.t2flow" });

		assertTrue(options.isEmbedded());
		assertFalse(options.isInMemory());
		assertFalse(options.isClientServer());
	}

}
