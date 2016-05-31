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

package org.apache.taverna.commandline.options;

import org.apache.taverna.commandline.exceptions.InvalidOptionException;

/**
 *
 *
 * @author David Withers
 */
public interface CommandLineOptions {

	public static final String CREDENTIAL_MANAGER_DIR_OPTION = "cmdir";
	public static final String CREDENTIAL_MANAGER_PASSWORD_OPTION = "cmpassword";

	public boolean askedForHelp();

	public boolean isProvenanceEnabled();

	public void displayHelp();

	public void displayHelp(boolean showFullText);

	public String[] getArgs();

	/**
	 *
	 * @return the port that the database should run on
	 */
	public String getDatabasePort();

	/**
	 *
	 * @return a path to a properties file that contains database configuration
	 *         settings
	 */
	public String getDatabaseProperties();

	/**
	 * Returns an array that alternates between a portname and path to a file
	 * containing the input values. Therefore the array will always contain an
	 * even number of elements
	 *
	 * @return an array of portname and path to files containing individual
	 *         inputs.
	 */
	public String[] getInputFiles();

	/**
	 * Returns an array that alternates between a portname and an
	 * input values. Therefore the array will always contain an
	 * even number of elements.
	 *
	 * @return an array of portname and individual
	 *         inputs.
	 */	
	public String[] getInputValues();
	
	/**
	 * Return the path for a data bundle which should be used for 
	 * input values. This option can't be used together with
	 * {@link #getInputFiles()} or {@link #getInputValues()}
	 * 
	 */
	public String getInputBundle();

	public String getLogFile();

	/**
	 *
	 * @return the directory to write the results to
	 */
	public String getOutputDirectory();

	public boolean getStartDatabase();

	/**
	 * @return the directory with Credential Manager's files
	 */
	public String getCredentialManagerDir();

	public boolean getStartDatabaseOnly() throws InvalidOptionException;

	public String getWorkflow() throws InvalidOptionException;

	public boolean hasDelimiterFor(String inputName);

	public boolean hasInputFiles();

	public boolean hasInputValues();
	
	public boolean hasInputBundle();

	public boolean hasLogFile();

	public boolean hasOption(String option);
	
	public boolean hasSaveResultsToBundle() ;

	public String inputDelimiter(String inputName);

	public boolean isClientServer();

	public boolean isEmbedded();

	public boolean isInMemory();

	/**
	 * Save the results to a directory if -outputdir has been explicitly defined,
	 * or if -outputdoc has not been defined.
	 *
	 * @return boolean
	 */
	public boolean saveResultsToDirectory();

	public String getSaveResultsToBundle();

}