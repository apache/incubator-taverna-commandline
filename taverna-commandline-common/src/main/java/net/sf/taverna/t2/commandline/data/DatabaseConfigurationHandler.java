/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.commandline.data;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.NamingException;

import net.sf.taverna.t2.commandline.exceptions.DatabaseConfigurationException;
import net.sf.taverna.t2.commandline.options.CommandLineOptions;
import net.sf.taverna.t2.workbench.reference.config.DataManagementConfiguration;
import net.sf.taverna.t2.workbench.reference.config.DataManagementHelper;

import org.apache.log4j.Logger;

public class DatabaseConfigurationHandler {

	private final CommandLineOptions options;
	private DataManagementConfiguration dbConfig;
	private static Logger logger = Logger.getLogger(DatabaseConfigurationHandler.class);

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

	public DataManagementConfiguration getDBConfig() {
		return dbConfig;
	}
	
	public void testDatabaseConnection()
			throws DatabaseConfigurationException, NamingException, SQLException {
		//try and get a connection
		Connection con = null;
		try {
			con = DataManagementHelper.openConnection();
		} finally {
			if (con!=null)
				try {
					con.close();
				} catch (SQLException e) {
					logger.warn("There was an SQL error whilst closing the test connection: "+e.getMessage(),e);
				}
		}
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
