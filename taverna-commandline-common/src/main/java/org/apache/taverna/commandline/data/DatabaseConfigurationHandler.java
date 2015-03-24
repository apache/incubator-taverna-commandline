/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.taverna.commandline.data;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.NamingException;

import org.apache.taverna.commandline.exceptions.DatabaseConfigurationException;
import org.apache.taverna.commandline.options.CommandLineOptions;

import org.apache.log4j.Logger;

import org.apache.taverna.configuration.database.DatabaseConfiguration;
import org.apache.taverna.configuration.database.DatabaseManager;

/**
 * Handles the initialisation and configuration of the data source according to
 * the command line arguments, or a properties file.
 * This also handles starting a network based instance of a Derby server, if requested.
 *
 * @author Stuart Owen
 *
 */
public class DatabaseConfigurationHandler {

	private static Logger logger = Logger.getLogger(DatabaseConfigurationHandler.class);
	private final CommandLineOptions options;
	private final DatabaseConfiguration dbConfig;
	private final DatabaseManager databaseManager;

	public DatabaseConfigurationHandler(CommandLineOptions options, DatabaseConfiguration databaseConfiguration, DatabaseManager databaseManager) {
		this.options = options;
		this.dbConfig = databaseConfiguration;
		this.databaseManager = databaseManager;
		databaseConfiguration.disableAutoSave();
	}

	public void configureDatabase() throws DatabaseConfigurationException {
		overrideDefaults();
		useOptions();
		if (dbConfig.getStartInternalDerbyServer()) {
			databaseManager.startDerbyNetworkServer();
			System.out.println("Started Derby Server on Port: "
					+ dbConfig.getCurrentPort());
		}
	}

	public DatabaseConfiguration getDBConfig() {
		return dbConfig;
	}

	private void importConfigurationFromStream(InputStream inStr)
			throws IOException {
		Properties p = new Properties();
		p.load(inStr);
		for (Object key : p.keySet()) {
			dbConfig.setProperty((String)key, p.getProperty((String)key).trim());
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

	protected void readConfigirationFromFile(String filename) throws IOException {
		FileInputStream fileInputStream = new FileInputStream(filename);
		importConfigurationFromStream(fileInputStream);
		fileInputStream.close();
	}

	public void testDatabaseConnection()
			throws DatabaseConfigurationException, NamingException, SQLException {
		//try and get a connection
		Connection con = null;
		try {
			con = databaseManager.getConnection();
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

		if (options.isProvenanceEnabled()) {
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

}
