/*******************************************************************************
 * Copyright (C) 2013 The University of Manchester
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
package net.sf.taverna.t2.commandline.options;

import net.sf.taverna.t2.commandline.exceptions.InvalidOptionException;

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
	 *
	 * @return the path to the input document
	 */
	public String getInputDocument();

	/**
	 * Returns an array that alternates between a portname and path to a file
	 * containing the input values. Therefore the array will always contain an
	 * even number of elements
	 *
	 * @return an array of portname and path to files containing individual
	 *         inputs.
	 */
	public String[] getInputFiles();

	public String[] getInputValues();

	public String getLogFile();

	/**
	 *
	 * @return the directory to write the results to
	 */
	public String getOutputDirectory();

	/**
	 *
	 * @return the path to the output document
	 */
	public String getOutputDocument();

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

	public boolean hasLogFile();

	public boolean hasOption(String option);

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

}