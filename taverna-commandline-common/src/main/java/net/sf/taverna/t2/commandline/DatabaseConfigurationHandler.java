package net.sf.taverna.t2.commandline;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import net.sf.taverna.t2.commandline.exceptions.DatabaseConfigurationException;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workbench.reference.config.DataManagementHelper;

public class DatabaseConfigurationHandler {

	private final CommandLineOptions options;
	private static DataManagementConfiguration dbConfig;

	public DatabaseConfigurationHandler(CommandLineOptions options) {
		this.options = options;
		dbConfig = DataManagementConfiguration.getInstance();
		dbConfig.disableAutoSave();
	}

	public void configureDatabase() throws DatabaseConfigurationException {
		overrideDefaults();
		useOptions();
		if (dbConfig.getStartInternalDerbyServer()) {
			DataManagementHelper.startDerbyNetworkServer();
			System.out.println("Started Derby Server on Port: "
					+ dbConfig.getCurrentPort());
		}
		DataManagementHelper.setupDataSource();
	}

	public void useOptions() throws DatabaseConfigurationException {
		
		if (options.hasOption("port")) {			
			dbConfig.setPort(options.getDatabasePort());		
		}
		
		if (options.hasOption("startdb")) {
			dbConfig.setStartInternalDerbyServer(true);
		}
		
		if (options.hasOption("inmemory")) {			
			dbConfig.setInMemory(true);		
		}
		
		if (options.hasOption("embedded")) {
			dbConfig.setInMemory(false);
			dbConfig.setDriverClassName("org.apache.derby.jdbc.EmbeddedDriver");
		}
		
		if (options.hasOption("provenance")) {
			dbConfig.setProvenanceEnabled(true);
		}
		
		if (options.hasOption("clientserver")) {
			dbConfig.setInMemory(false);
			dbConfig.setDriverClassName("org.apache.derby.jdbc.ClientDriver");
			dbConfig.setJDBCUri("jdbc:derby://localhost:" + dbConfig.getPort() + "/t2-database;create=true;upgrade=true");			
		}		
		
		if (options.hasOption("dbproperties")) {
			try {
				readConfigirationFromFile(options.getDatabaseProperties());
			} catch (IOException e) {
				throw new DatabaseConfigurationException("There was an error reading the database configuration options at "+options.getDatabaseProperties()+" : "+e.getMessage(),e);
			}
		}
	}

	protected void overrideDefaults() throws DatabaseConfigurationException {
		
		InputStream inStr = DatabaseConfigurationHandler.class.getClassLoader().getResourceAsStream("database-defaults.properties");
		try {
			importConfigurationFromStream(inStr);
		} catch (IOException e) {
			throw new DatabaseConfigurationException("There was an error reading the default database configuration settings: "+e.getMessage(),e);
		}
	}

	private void importConfigurationFromStream(InputStream inStr)
			throws IOException {
		Properties p = new Properties();
		p.load(inStr);		
		for (Object key : p.keySet()) {
			dbConfig.setProperty((String)key, p.getProperty((String)key).trim());
		}
	}
	
	protected void readConfigirationFromFile(String filename) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(filename);
		importConfigurationFromStream(fileInputStream);
		fileInputStream.close();
	}

}
