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
package net.sf.taverna.t2.commandline.options;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import net.sf.taverna.t2.commandline.exceptions.InvalidOptionException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 * Handles the processing of command line arguments for enacting a workflow.
 * This class encapsulates all command line options, and exposes them through
 * higher-level accessors. Upon creation it checks the validity of the command
 * line options and raises an {@link InvalidOptionException} if they are
 * invalid.
 * 
 * @author Stuart Owen
 * 
 */
public class CommandLineOptions {
	private static final Logger logger = Logger
			.getLogger(CommandLineOptions.class);
	private Options options;
	private CommandLine commandLine;

	private static final String PROV_OPTION = "provenance";
	private static final String PROV_BUNDLE = "provbundle";
	public static final String CREDENTIAL_MANAGER_DIR_OPTION = "cmdir";
	public static final String CREDENTIAL_MANAGER_PASSWORD_OPTION = "cmpassword";
	private static final String INPUT_DELIMITER_LITERAL_OPTION = "inputdelimiter";
	private static final String INPUT_DELIMITER_FILE_OPTION = "inputdelimfile";
	private static final String INPUT_BACLAVA_OPTION = "inputdoc";
	private static final String INPUT_FILE_OPTION = "inputfile";
	private static final String INPUT_LITERAL_OPTION = "inputvalue";
	private static final String OUTPUT_DIRECTORY_OPTION = "outputdir";
	private static final String OUTPUT_BACLAVA_OPTION = "outputdoc";
	private static final String DB_EMBEDDED_OPTION = "embedded";
	private static final String DB_CLIENT_OPTION = "clientserver";
	private static final String DB_MEMORY_OPTION = "inmemory";
	private static final String DB_PROPS_OPTION = "dbproperties";
	private static final String DB_START_OPTION = "startdb";
	private static final String DB_PORT_OPTION = "port";
	private static final String LOG_OPTION = "logfile";
	private static final String HELP_OPTION = "help";

	public CommandLineOptions(String[] args) throws InvalidOptionException {
		this.options = intitialiseOptions();
		this.commandLine = processArgs(args);
		checkForInvalid();
	}

	public boolean askedForHelp() {
		return hasOption(HELP_OPTION)
				|| (getArgs().length == 0 && getOptions().length == 0);
	}

	public boolean isProvenanceEnabled() {
		return hasOption(PROV_OPTION) || hasOption(PROV_BUNDLE);
	}

	protected void checkForInvalid() throws InvalidOptionException {
		if (askedForHelp())
			return;
		if (isProvenanceEnabled()
				&& !(hasOption(DB_EMBEDDED_OPTION)
						|| hasOption(DB_CLIENT_OPTION) || hasOption(DB_PROPS_OPTION)))
			throw new InvalidOptionException(
					"You must be running with a disk-based database to use provenance");
		if (isProvenanceEnabled() && hasOption(DB_MEMORY_OPTION))
			throw new InvalidOptionException(
					"You must be running with a disk-based database to use provenance");
		if (hasOption(INPUT_FILE_OPTION) && hasOption(INPUT_BACLAVA_OPTION))
			throw new InvalidOptionException("You can't provide both -"
					+ INPUT_FILE_OPTION + " and -" + INPUT_BACLAVA_OPTION
					+ " arguments");
		if (hasOption(INPUT_LITERAL_OPTION) && hasOption(INPUT_BACLAVA_OPTION))
			throw new InvalidOptionException("You can't provide both -"
					+ INPUT_LITERAL_OPTION + " and -" + INPUT_BACLAVA_OPTION
					+ " arguments");

		if (hasOption(INPUT_DELIMITER_FILE_OPTION)
				&& hasOption(INPUT_BACLAVA_OPTION))
			throw new InvalidOptionException("You cannot combine the -"
					+ INPUT_DELIMITER_FILE_OPTION + " and -"
					+ INPUT_BACLAVA_OPTION + " arguments");
		if (hasOption(INPUT_DELIMITER_LITERAL_OPTION)
				&& hasOption(INPUT_BACLAVA_OPTION))
			throw new InvalidOptionException("You cannot combine the -"
					+ INPUT_DELIMITER_LITERAL_OPTION + " and -"
					+ INPUT_BACLAVA_OPTION + " arguments");

		if (getArgs().length == 0
				&& !(hasOption(HELP_OPTION) || hasOption(DB_START_OPTION)))
			throw new InvalidOptionException("You must specify a workflow");

		if (hasOption(DB_MEMORY_OPTION) && hasOption(DB_EMBEDDED_OPTION)
				|| hasOption(DB_MEMORY_OPTION) && hasOption(DB_CLIENT_OPTION)
				|| hasOption(DB_EMBEDDED_OPTION) && hasOption(DB_CLIENT_OPTION))
			throw new InvalidOptionException("The options -"
					+ DB_EMBEDDED_OPTION + ", -" + DB_CLIENT_OPTION + " and -"
					+ DB_MEMORY_OPTION + " cannot be used together");
	}

	public void displayHelp() {
		displayHelp(hasOption(HELP_OPTION));
	}

	public void displayHelp(boolean showFullText) {
		try {
			HelpFormatter formatter = new HelpFormatter();
			formatter
					.printHelp("executeworkflow [options] [workflow]", options);
			if (showFullText)
				try (InputStream helpStream = CommandLineOptions.class
						.getClassLoader().getResourceAsStream("help.txt")) {
					System.out.println(IOUtils.toString(helpStream));
				}
		} catch (IOException e) {
			logger.error("Error reading the help document", e);
			System.exit(-1);
		}
	}

	public String[] getArgs() {
		return commandLine.getArgs();
	}

	/**
	 * 
	 * @return the port that the database should run on
	 */
	public String getDatabasePort() {
		return getOptionValue(DB_PORT_OPTION);
	}

	/**
	 * 
	 * @return a path to a properties file that contains database configuration
	 *         settings
	 */
	public String getDatabaseProperties() {
		return getOptionValue(DB_PROPS_OPTION);
	}

	/**
	 * 
	 * @return the path to the input document
	 */
	public String getInputDocument() {
		return getOptionValue(INPUT_BACLAVA_OPTION);
	}

	/**
	 * Returns an array that alternates between a portname and path to a file
	 * containing the input values. Therefore the array will always contain an
	 * even number of elements
	 * 
	 * @return an array of portname and path to files containing individual
	 *         inputs.
	 */
	public String[] getInputFiles() {
		if (hasInputFiles())
			return getOptionValues(INPUT_FILE_OPTION);
		return new String[] {};
	}

	public String[] getInputValues() {
		if (hasInputValues())
			return getOptionValues(INPUT_LITERAL_OPTION);
		return new String[] {};
	}

	public String getLogFile() {
		return getOptionValue(LOG_OPTION);
	}

	public Option[] getOptions() {
		return commandLine.getOptions();
	}

	private String getOptionValue(String opt) {
		return commandLine.getOptionValue(opt);
	}

	private String[] getOptionValues(String arg0) {
		return commandLine.getOptionValues(arg0);
	}

	/**
	 * 
	 * @return the directory to write the results to
	 */
	public String getOutputDirectory() {
		return getOptionValue(OUTPUT_DIRECTORY_OPTION);
	}

	/**
	 * 
	 * @return the path to the output document
	 */
	public String getOutputDocument() {
		return getOptionValue(OUTPUT_BACLAVA_OPTION);
	}

	public boolean getStartDatabase() {
		return hasOption(DB_START_OPTION);
	}

	/**
	 * @return the directory with Credential Manager's files
	 */
	public File getCredentialManagerDir() {
		return getOptionValue(CREDENTIAL_MANAGER_DIR_OPTION) == null ? null
				: new File(getOptionValue(CREDENTIAL_MANAGER_DIR_OPTION));
	}

	public boolean getStartDatabaseOnly() throws InvalidOptionException {
		return (getStartDatabase() && (getWorkflow() == null));
	}

	public String getWorkflow() throws InvalidOptionException {
		if (getArgs().length == 0)
			return null;
		else if (getArgs().length != 1)
			throw new InvalidOptionException(
					"You should only specify one workflow file");
		else
			return getArgs()[0];
	}

	public boolean hasDelimiterFor(String inputName) {
		if (hasOption(INPUT_DELIMITER_FILE_OPTION)) {
			String[] values = getOptionValues(INPUT_DELIMITER_FILE_OPTION);
			for (int i = 0; i < values.length - 1; i += 2) {
				if (!values[i].equals(inputName))
					continue;
				File f = new File(values[i + 1]);
				if (f.exists() && f.isFile() && f.canRead())
					return true;
			}
		}
		if (hasOption(INPUT_DELIMITER_LITERAL_OPTION)) {
			String[] values = getOptionValues(INPUT_DELIMITER_LITERAL_OPTION);
			for (int i = 0; i < values.length; i += 2)
				if (values[i].equals(inputName))
					return true;
		}
		return false;
	}

	public boolean hasInputFiles() {
		return hasOption(INPUT_FILE_OPTION);
	}

	public boolean hasInputValues() {
		return hasOption(INPUT_LITERAL_OPTION);
	}

	public boolean hasLogFile() {
		return hasOption(LOG_OPTION);
	}

	public boolean hasOption(String option) {
		return commandLine.hasOption(option);
	}

	public String inputDelimiter(String inputName) {
		String result = null;
		if (hasOption(INPUT_DELIMITER_FILE_OPTION)) {
			String[] values = getOptionValues(INPUT_DELIMITER_FILE_OPTION);
			for (int i = 0; i < values.length - 1; i += 2) {
				if (!values[i].equals(inputName))
					continue;
				File f = new File(values[i + 1]);
				if (f.exists() && f.isFile() && f.canRead())
					try (Reader r = new FileReader(f)) {
						int ch = r.read();
						if (ch >= 0)
							return "" + (char) ch;
					} catch (IOException e) {
						// Ignore! Not much we can do
					}
			}
		}
		if (hasOption(INPUT_DELIMITER_LITERAL_OPTION)) {
			String[] values = getOptionValues(INPUT_DELIMITER_LITERAL_OPTION);
			for (int i = 0; i < values.length; i += 2)
				if (values[i].equals(inputName))
					return values[i + 1];
		}
		return result;
	}

	@SuppressWarnings("static-access")
	private Options intitialiseOptions() {
		Option helpOption = new Option(HELP_OPTION,
				"Display comprehensive help information.");

		Option outputOption = OptionBuilder
				.withArgName("directory")
				.hasArg()
				.withDescription(
						"Save outputs as files in directory, default "
								+ "is to make a new directory workflowName_output.")
				.create(OUTPUT_DIRECTORY_OPTION);
		Option outputdocOption = OptionBuilder.withArgName("document").hasArg()
				.withDescription("Save outputs to a new Baclava document.")
				.create(OUTPUT_BACLAVA_OPTION);

		Option logFileOption = OptionBuilder
				.withArgName("filename")
				.hasArg()
				.withDescription(
						"The logfile to which more verbose logging will be written to.")
				.create(LOG_OPTION);

		Option inputdocOption = OptionBuilder.withArgName("document").hasArg()
				.withDescription("Load inputs from a Baclava document.")
				.create(INPUT_BACLAVA_OPTION);

		Option inputFileOption = OptionBuilder
				.withArgName("inputname filename").hasArgs(2)
				.withValueSeparator(' ')
				.withDescription("Load the named input from file or URL.")
				.create(INPUT_FILE_OPTION);
		Option inputValueOption = OptionBuilder.withArgName("inputname value")
				.hasArgs(2).withValueSeparator(' ')
				.withDescription("Directly use the value for the named input.")
				.create(INPUT_LITERAL_OPTION);

		Option inputDelimiterOption = OptionBuilder
				.withArgName("inputname delimiter")
				.hasArgs(2)
				.withValueSeparator(' ')
				.withDescription(
						"Cause an inputvalue or inputfile to be split into a "
						+ "list according to the delimiter. The associated "
						+ "workflow input must be expected to receive a list.")
				.create(INPUT_DELIMITER_LITERAL_OPTION);
		Option inputDelimiterFileOption = OptionBuilder
				.withArgName("inputname file")
				.hasArgs(2)
				.withValueSeparator(' ')
				.withDescription(
						"Cause an inputvalue or inputfile to be split into a "
								+ "list according to the delimiter character, "
								+ "which is the first character read from the "
								+ "given file. The associated workflow input "
								+ "must be expected to receive a list.")
				.create(INPUT_DELIMITER_FILE_OPTION);

		Option dbProperties = OptionBuilder
				.withArgName("filename")
				.hasArg()
				.withDescription(
						"Load a properties file to configure the database.")
				.create(DB_PROPS_OPTION);
		Option port = OptionBuilder
				.withArgName("portnumber")
				.hasArg()
				.withDescription(
						"The port that the database is running on. If set "
								+ "requested to start its own internal server, "
								+ "this is the start port that will be used.")
				.create(DB_PORT_OPTION);
		Option embedded = new Option(DB_EMBEDDED_OPTION,
				"Connect to an embedded Derby database. This can prevent "
						+ "mulitple invocations.");
		Option clientserver = new Option(DB_CLIENT_OPTION,
				"Connect as a client to a derby server instance.");
		Option inMemOption = new Option(
				DB_MEMORY_OPTION,
				"Run the workflow with data stored in-memory rather than in a "
						+ "database. This can give performance inprovements, "
						+ "at the cost of overall memory usage.");
		Option startDB = new Option(DB_START_OPTION,
				"Automatically start an internal Derby database server.");

		Option provenance = new Option(PROV_OPTION,
				"Generate provenance information and store it in the database.");
		Option provExport = OptionBuilder
				.hasArg()
				.withArgName("file")
				.withDescription(
						"Save provenance/trace of workflow execution as "
								+ "Research Object zip bundle to <file> specified.")
				.create(PROV_BUNDLE);

		Option credentialManagerDirectory = OptionBuilder
				.withArgName("directory path")
				.hasArg()
				.withDescription(
						"Absolute path to a directory where Credential Manager's "
								+ "files (keystore and truststore) are located. ")
				.create(CREDENTIAL_MANAGER_DIR_OPTION);
		Option credentialManagerPassword = new Option(
				CREDENTIAL_MANAGER_PASSWORD_OPTION,
				"Indicate that the master password for Credential Manager will be "
						+ "provided on standard input."); // optional

		Options options = new Options();
		options.addOption(helpOption);
		options.addOption(inputFileOption);
		options.addOption(inputValueOption);
		options.addOption(inputDelimiterOption);
		options.addOption(inputDelimiterFileOption);
		options.addOption(inputdocOption);
		options.addOption(outputOption);
		options.addOption(outputdocOption);
		options.addOption(inMemOption);
		options.addOption(embedded);
		options.addOption(clientserver);
		options.addOption(dbProperties);
		options.addOption(port);
		options.addOption(startDB);
		options.addOption(provenance);
		options.addOption(provExport);
		options.addOption(logFileOption);
		options.addOption(credentialManagerDirectory);
		options.addOption(credentialManagerPassword);

		return options;
	}

	public boolean isClientServer() {
		return hasOption(DB_CLIENT_OPTION);
	}

	public boolean isEmbedded() {
		return hasOption(DB_EMBEDDED_OPTION);
	}

	public boolean isInMemory() {
		return hasOption(DB_MEMORY_OPTION);
	}

	private CommandLine processArgs(String[] args) {
		CommandLineParser parser = new GnuParser();
		CommandLine line = null;
		try {
			// parse the command line arguments
			line = parser.parse(options, args);
		} catch (ParseException exp) {
			// oops, something went wrong
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
			System.exit(1);
		}
		return line;
	}

	/**
	 * Save the results to a directory if -outputdir has been explicitly
	 * defined, or if -outputdoc has not been defined
	 * 
	 * @return boolean
	 */
	public boolean saveResultsToDirectory() {
		return options.hasOption(OUTPUT_DIRECTORY_OPTION)
				|| !options.hasOption(OUTPUT_BACLAVA_OPTION)
				|| !options.hasOption(PROV_BUNDLE);
	}

	public String getProvBundle() {
		return getOptionValue(PROV_BUNDLE);
	}

	public boolean isProvBundle() {
		return hasOption(PROV_BUNDLE);
	}
}
