package net.sf.taverna.t2.commandline;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CommandLineOptions {

	private Options options;
	private CommandLine commandLine;			

	public CommandLineOptions(String [] args) throws InvalidOptionException{
		this.options = intitialiseOptions();
		this.commandLine = processArgs(args);	
		checkForInvalid();
		checkForHelp();		
	}
	
	protected void checkForInvalid() throws InvalidOptionException {
		if (hasOption("inmemory") && hasOption("embedded")) throw new InvalidOptionException("The options -embedded, -clientserver and -inmemory cannot be used together");
		if (hasOption("inmemory") && hasOption("clientserver")) throw new InvalidOptionException("The options -embedded, -clientserver and -inmemory cannot be used together");
		if (hasOption("embedded") && hasOption("clientserver")) throw new InvalidOptionException("The options -embedded, -clientserver and -inmemory cannot be used together");
	}
	
	
	
	public String getWorkflow() throws InvalidOptionException {
		if (getArgs().length!=1) {
			throw new InvalidOptionException("You must specify a workflow");
		}
		return getArgs()[0];
	}
	
	public String[] getArgs() {
		return commandLine.getArgs();
	}
	
	private void checkForHelp() {
		if (hasOption("help")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("executeworkflow <workflow> [..]\n"
					+ "Execute workflow and save outputs. "
					+ "Inputs can be specified by multiple "
					+ "-input options, or loaded from an "
					+ "XML input document as saved from "
					+ "Taverna. By default, a new directory "
					+ "is created named workflow.xml_output "
					+ "unless the -output or -outputdoc"
					+ "options are given. All files to be read "
					+ "can be either a local file or an URL.", options);
			System.exit(0);
		}		
	}
	
	public String getOptionValue(String opt) {
		return commandLine.getOptionValue(opt);
	}

	public String[] getOptionValues(String arg0) {
		return commandLine.getOptionValues(arg0);
	}
	
	/**
	 * Save the results to a directory if -output has been explicitly defined, and/or if -outputdoc hasn't been defined
	 * @return boolean
	 */
	public boolean saveResultsToDirectory() {
		return (options.hasOption("output") || !options.hasOption("outputdoc"));
	}
	
	public String outputDocument() {
		return getOptionValue("outputdoc");
	}
	
	public String inputDocument() {
		return getOptionValue("inputdoc");
	}

	public boolean hasOption(String option) {
		return commandLine.hasOption(option);
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

	private Options intitialiseOptions() {
		Option helpOption = new Option("help", "print this message");

		Option outputOption = OptionBuilder
				.withArgName("directory")
				.hasArg()
				.withDescription(
						"save outputs as files in directory, default "
								+ "is to make a new directory workflowName_output")
				.create("output");
		Option outputdocOption = OptionBuilder.withArgName("document").hasArg()
				.withDescription("save outputs to a new XML document").create(
						"outputdoc");		
		Option inputdocOption = OptionBuilder.withArgName("document").hasArg()
				.withDescription("load inputs from XML document").create(
						"inputdoc");

		Option inputOption = OptionBuilder.withArgName("name filename")
				.hasArgs(2).withValueSeparator('=').withDescription(
						"load the named input from file or URL")
				.create("input");
		
		Option dbProperties = OptionBuilder.withArgName("filename").hasArg().withDescription(
				"loads a properties file to configure the database").create("dbproperties");
		
		Option port = OptionBuilder.withArgName("portnumber").hasArg().withDescription(
		"the port that the database is running on. If set requested to start its own internal server, this is the start port that will be used.").create("port");
		
		

		Option embedded = new Option("embedded","connects to an embedded Derby database. This can prevent mulitple invocations");
		Option clientserver = new Option("clientserver","connects as a client to a derby server instance.");
		Option inMemOption = new Option("inmemory","runs the workflow with data stored in-memory rather than in a database. This can give performance inprovements, at the cost of overall memory usage");
		Option startDB = new Option("startdb","automatically starts an internal Derby database server.");
		Option provenance = new Option("provenance","generates provenance information and stores it in the database.");
		
		Options options = new Options();
		options.addOption(helpOption);
		options.addOption(inputOption);
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
		
		return options;
		
	}
}
