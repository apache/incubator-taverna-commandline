package net.sf.taverna.t2.commandline;

import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestDatabaseConfigurationHandler {

	@Test
	public void testDefaults() throws Exception {
		CommandLineOptions opts = new CommandLineOptions(new String[]{"myworkflow.t2flow"});
		DatabaseConfigurationHandler handler = new DatabaseConfigurationHandler(opts);
		handler.configureDatabase();
		assertEquals("org.apache.derby.jdbc.ClientDriver", DataManagementConfiguration.getInstance().getDriverClassName());
		assertEquals(false, DataManagementConfiguration.getInstance().getStartInternalDerbyServer());
	}
}
