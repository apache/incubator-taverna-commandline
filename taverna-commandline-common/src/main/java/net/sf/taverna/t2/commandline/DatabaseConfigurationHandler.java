package net.sf.taverna.t2.commandline;

import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workbench.reference.config.DataManagementHelper;

public class DatabaseConfigurationHandler {

	private final CommandLineOptionsHandler options;
	private static DataManagementConfiguration dbConfig;

	public DatabaseConfigurationHandler(CommandLineOptionsHandler options) {
		this.options = options;		
		dbConfig  = DataManagementConfiguration.getInstance();
		dbConfig.disableAutoSave();
	}
	
	public void configureDatabase() {
		overrideDefaults();
		useOptions();
		if (dbConfig.getStartInternalDerbyServer()) {
        	DataManagementHelper.startDerbyNetworkServer();
        	System.out.println("Started Derby Server on Port: "+dbConfig.getCurrentPort());
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
	
	private void overrideDefaults() {
		//FIXME: change this to read from a file. 
		//FIXME: It must also set a default for each value - don't rely on the Taverna defaults as they could have been changed in the workbench.
		dbConfig.setInMemory(false);
		dbConfig.setStartInternalDerbyServer(true);
		dbConfig.setDriverClassName("org.apache.derby.jdbc.ClientDriver");
	}
 }
