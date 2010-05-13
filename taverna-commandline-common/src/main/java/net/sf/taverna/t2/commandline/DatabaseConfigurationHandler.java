package net.sf.taverna.t2.commandline;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.ResourceBundle;

import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workbench.reference.config.DataManagementHelper;

public class DatabaseConfigurationHandler {

	private final CommandLineOptionsHandler options;
	private static DataManagementConfiguration dbConfig;

	public DatabaseConfigurationHandler(CommandLineOptionsHandler options) {
		this.options = options;
		dbConfig = DataManagementConfiguration.getInstance();
		dbConfig.disableAutoSave();
	}

	public void configureDatabase() throws IOException {
		overrideDefaults();
		useOptions();
		if (dbConfig.getStartInternalDerbyServer()) {
			DataManagementHelper.startDerbyNetworkServer();
			System.out.println("Started Derby Server on Port: "
					+ dbConfig.getCurrentPort());
		}
		DataManagementHelper.setupDataSource();
	}

	public void useOptions() {
		if (options.hasOption("inmemory")) {
			System.out.println("Running in memory");
			dbConfig.setInMemory(true);
			dbConfig.setStartInternalDerbyServer(false);
		}
	}

	protected void overrideDefaults() throws IOException {
		Properties p = new Properties();
		InputStream inStr = DatabaseConfigurationHandler.class.getClassLoader().getResourceAsStream("database-defaults.properties");				
		p.load(inStr);		
		for (Object key : p.keySet()) {
			dbConfig.setProperty((String)key, p.getProperty((String)key).trim());
		}
	}

}
