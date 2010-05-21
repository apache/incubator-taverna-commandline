package net.sf.taverna.t2.commandline;

import static org.junit.Assert.assertEquals;
import net.sf.taverna.t2.commandline.options.CommandLineOptions;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;

import org.junit.Test;

public class TestDatabaseConfigurationHandler {

	@Test
	public void testDefaults() throws Exception {
		CommandLineOptions opts = new CommandLineOptions(new String[]{"myworkflow.t2flow"});
		DatabaseConfigurationHandler handler = new DatabaseConfigurationHandler(opts);
		handler.configureDatabase();
		assertEquals("org.apache.derby.jdbc.EmbeddedDriver", DataManagementConfiguration.getInstance().getDriverClassName());
		assertEquals(false, DataManagementConfiguration.getInstance().getStartInternalDerbyServer());
	}
}
