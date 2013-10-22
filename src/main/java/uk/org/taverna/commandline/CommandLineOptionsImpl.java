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
package uk.org.taverna.commandline;

import java.io.IOException;
import java.io.InputStream;

import net.sf.taverna.t2.commandline.exceptions.ArgumentsParsingException;
import net.sf.taverna.t2.commandline.exceptions.InvalidOptionException;
import net.sf.taverna.t2.commandline.options.CommandLineOptions;

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
 * This class encapsulates all command line options, and exposes them through higher-level
 * accessors. Upon creation it checks the validity of the command line options and raises an
 * {@link InvalidOptionException} if they are invalid.
 *
 * @author Stuart Owen
 * @author David Withers
 */
public class CommandLineOptionsImpl implements CommandLineOptions {

	private static final Logger logger = Logger.getLogger(CommandLineOptionsImpl.class);
	private Options options;
	private CommandLine commandLine;

	public CommandLineOptionsImpl(String[] args) throws ArgumentsParsingException, InvalidOptionException {
		this.options = intitialiseOptions();
		this.commandLine = processArgs(args);
		checkForInvalid();
	}

	@Override
	public boolean askedForHelp() {
		return hasOption("help") || (getArgs().length==0 && getOptions().length==0);
	}

	@Override
	public boolean isProvenanceEnabled() {
		return hasOption("provenance");
	}

	protected void checkForInvalid() throws InvalidOptionException {
		if (askedForHelp()) return;
		if (isProvenanceEnabled()
				&& !(hasOption("embedded") || hasOption("clientserver") || hasOption("dbproperties")))
			throw new InvalidOptionException(
					"You should be running with a database to use provenance");
		if (isProvenanceEnabled() && hasOption("inmemory"))
			throw new InvalidOptionException(
					"You should be running with a database to use provenance");
		if ((hasOption("inputfile") || hasOption("inputvalue"))
				&& hasOption("inputdoc"))
			throw new InvalidOptionException(
					"You can't provide both -input and -inputdoc arguments");

		if (hasOption("inputdelimiter") && hasOption("inputdoc"))
			throw new InvalidOptionException("You cannot combine the -inputdelimiter and -inputdoc arguments");

		if (getArgs().length == 0
				&& !(hasOption("help") || hasOption("startdb")))
			throw new InvalidOptionException("You must specify a workflow");

		if (hasOption("inmemory") && hasOption("embedded"))
			throw new InvalidOptionException(
					"The options -embedded, -clientserver and -inmemory cannot be used together");
		if (hasOption("inmemory") && hasOption("clientserver"))
			throw new InvalidOptionException(
					"The options -embedded, -clientserver and -inmemory cannot be used together");
		if (hasOption("embedded") && hasOption("clientserver"))
			throw new InvalidOptionException(
					"The options -embedded, -clientserver and -inmemory cannot be used together");
	}

	@Override
	public void displayHelp() {
		boolean full = false;
		if (hasOption("help")) full=true;
		displayHelp(full);
	}

	@Override
	public void displayHelp(boolean showFullText) {

		HelpFormatter formatter = new HelpFormatter();
		try {
			formatter
					.printHelp("executeworkflow [options] [workflow]", options);
			if (showFullText) {
				InputStream helpStream = CommandLineOptionsImpl.class
						.getClassLoader().getResourceAsStream("help.txt");
				String helpText = IOUtils.toString(helpStream);
				System.out.println(helpText);
			}

		} catch (IOException e) {
			logger.error("Failed to load the help document", e);
			System.out.println("Failed to load the help document");
			//System.exit(-1);
		}
	}

	@Override
	public String[] getArgs() {
		return commandLine.getArgs();
	}

	/**
	 *
	 * @return the port that the database should run on
	 */
	@Override
	public String getDatabasePort() {
		return getOptionValue("port");
	}

	/**
	 *
	 * @return a path to a properties file that contains database configuration
	 *         settings
	 */
	@Override
	public String getDatabaseProperties() {
		return getOptionValue("dbproperties");
	}

	/**
	 *
	 * @return the path to the input document
	 */
	@Override
	public String getInputDocument() {
		return getOptionValue("inputdoc");
	}

	/**
	 * Returns an array that alternates between a portname and path to a file
	 * containing the input values. Therefore the array will always contain an
	 * even number of elements
	 *
	 * @return an array of portname and path to files containing individual
	 *         inputs.
	 */
	@Override
	public String[] getInputFiles() {
		if (hasInputFiles()) {
			return getOptionValues("inputfile");
		} else {
			return new String[] {};
		}
	}

	@Override
	public String[] getInputValues() {
		if (hasInputValues()) {
			return getOptionValues("inputvalue");
		} else {
			return new String[] {};
		}
	}

	@Override
	public String getLogFile() {
		return getOptionValue("logfile");
	}

	public Option [] getOptions() {
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
	@Override
	public String getOutputDirectory() {
		return getOptionValue("outputdir");
	}

	/**
	 *
	 * @return the path to the output document
	 */
	@Override
	public String getOutputDocument() {
		return getOptionValue("outputdoc");
	}

	@Override
	public boolean getStartDatabase() {
		return hasOption("startdb");
	}

	/**
	 * @return the directory with Credential Manager's files
	 */
	@Override
	public String getCredentialManagerDir() {
		return getOptionValue(CREDENTIAL_MANAGER_DIR_OPTION);
	}

	@Override
	public boolean getStartDatabaseOnly() throws InvalidOptionException {
		return (getStartDatabase() && (getWorkflow() == null));
	}

	@Override
	public String getWorkflow() throws InvalidOptionException {
		if (getArgs().length == 0) {
			return null;
		} else if (getArgs().length != 1) {
			throw new InvalidOptionException(
					"You should only specify one workflow file");
		} else {
			return getArgs()[0];
		}
	}

	@Override
	public boolean hasDelimiterFor(String inputName) {
		boolean result = false;
		if (hasOption("inputdelimiter")) {
			String [] values = getOptionValues("inputdelimiter");
			for (int i=0;i<values.length;i+=2) {
				if (values[i].equals(inputName))
				{
					result=true;
					break;
				}
			}
		}
		return result;
	}

	@Override
	public boolean hasInputFiles() {
		return hasOption("inputfile");
	}

	@Override
	public boolean hasInputValues() {
		return hasOption("inputvalue");
	}

	@Override
	public boolean hasLogFile() {
		return hasOption("logfile");
	}

	@Override
	public boolean hasOption(String option) {
		return commandLine.hasOption(option);
	}

	@Override
	public String inputDelimiter(String inputName) {
		String result = null;
		if (hasOption("inputdelimiter")) {
			String [] values = getOptionValues("inputdelimiter");
			for (int i=0;i<values.length;i+=2) {
				if (values[i].equals(inputName))
				{
					result=values[i+1];
					break;
				}
			}
		}
		return result;
	}

	@SuppressWarnings("static-access")
	private Options intitialiseOptions() {
		Option helpOption = new Option("help", "Display comprehensive help information.");

		Option outputOption = OptionBuilder
				.withArgName("directory")
				.hasArg()
				.withDescription(
						"Save outputs as files in directory, default "
								+ "is to make a new directory workflowName_output.")
				.create("outputdir");

		Option outputdocOption = OptionBuilder.withArgName("document").hasArg()
				.withDescription("Save outputs to a new Baclava document.")
				.create("outputdoc");

		Option logFileOption = OptionBuilder
				.withArgName("filename")
				.hasArg()
				.withDescription(
						"The logfile to which more verbose logging will be written to.")
				.create("logfile");

		Option inputdocOption = OptionBuilder.withArgName("document").hasArg()
				.withDescription("Load inputs from a Baclava document.").create(
						"inputdoc");

		Option inputFileOption = OptionBuilder
				.withArgName("inputname filename").hasArgs(2)
				.withValueSeparator(' ').withDescription(
						"Load the named input from file or URL.").create(
						"inputfile");

		Option inputValueOption = OptionBuilder.withArgName("inputname value")
				.hasArgs(2).withValueSeparator(' ').withDescription(
						"Directly use the value for the named input.").create(
						"inputvalue");

		Option inputDelimiterOption = OptionBuilder
				.withArgName("inputname delimiter")
				.hasArgs(2)
				.withValueSeparator(' ')
				.withDescription(
						"Cause an inputvalue or inputfile to be split into a list according to the delimiter. The associated workflow input must be expected to receive a list.")
				.create("inputdelimiter");

		Option dbProperties = OptionBuilder.withArgName("filename").hasArg()
				.withDescription(
						"Load a properties file to configure the database.")
				.create("dbproperties");

		Option port = OptionBuilder
				.withArgName("portnumber")
				.hasArg()
				.withDescription(
						"The port that the database is running on. If set requested to start its own internal server, this is the start port that will be used.")
				.create("port");

		Option embedded = new Option("embedded",
				"Connect to an embedded Derby database. This can prevent mulitple invocations.");
		Option clientserver = new Option("clientserver",
				"Connect as a client to a derby server instance.");
		Option inMemOption = new Option(
				"inmemory",
				"Run the workflow with data stored in-memory rather than in a database (this is the default option). This can give performance inprovements, at the cost of overall memory usage.");
		Option startDB = new Option("startdb",
				"Automatically start an internal Derby database server.");
		Option provenance = new Option("provenance",
				"Generate provenance information and store it in the database.");


		Option credentialManagerDirectory = OptionBuilder.withArgName("directory path").
		hasArg().withDescription(
				"Absolute path to a directory where Credential Manager's files (keystore and truststore) are located.")
		.create(CREDENTIAL_MANAGER_DIR_OPTION);
		Option credentialManagerPassword = new Option(CREDENTIAL_MANAGER_PASSWORD_OPTION, "Indicate that the master password for Credential Manager will be provided on standard input."); // optional password option, to be read from standard input

		Options options = new Options();
		options.addOption(helpOption);
		options.addOption(inputFileOption);
		options.addOption(inputValueOption);
		options.addOption(inputDelimiterOption);
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
		options.addOption(logFileOption);
		options.addOption(credentialManagerDirectory);
		options.addOption(credentialManagerPassword);

		return options;
	}

	@Override
	public boolean isClientServer() {
		return hasOption("clientserver");
	}

	@Override
	public boolean isEmbedded() {
		return hasOption("embedded");
	}

	@Override
	public boolean isInMemory() {
		return hasOption("inmemory");
	}

	private CommandLine processArgs(String[] args) throws ArgumentsParsingException {
		CommandLineParser parser = new GnuParser();
		CommandLine line = null;
		try {
			// parse the command line arguments
			line = parser.parse(options, args);
		} catch (ParseException exp) {
			// oops, something went wrong
//			System.err.println("Taverna command line arguments' parsing failed. Reason: " + exp.getMessage());
//			System.exit(1);
			throw new ArgumentsParsingException("Taverna command line arguments' parsing failed. Reason: " + exp.getMessage(), exp);
		}
		return line;
	}

	/**
	 * Save the results to a directory if -outputdir has been explicitly defined,
	 * or if -outputdoc has not been defined.
	 *
	 * @return boolean
	 */
	@Override
	public boolean saveResultsToDirectory() {
		return (options.hasOption("outputdir") || !options
				.hasOption("outputdoc"));
	}

}
