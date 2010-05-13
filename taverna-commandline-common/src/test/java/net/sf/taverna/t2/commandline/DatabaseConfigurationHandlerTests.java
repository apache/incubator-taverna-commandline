package net.sf.taverna.t2.commandline;

import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DatabaseConfigurationHandlerTests {

	@Test
	public void testDefaults() throws Exception {
		CommandLineOptionsHandler opts = new CommandLineOptionsHandler(new String[]{});
		DatabaseConfigurationHandler handler = new DatabaseConfigurationHandler(opts);
		handler.configureDatabase();
		assertEquals("org.apache.derby.jdbc.ClientDriver", DataManagementConfiguration.getInstance().getDriverClassName());
		assertEquals(false, DataManagementConfiguration.getInstance().getStartInternalDerbyServer());
	}
}
