package net.sf.taverna.t2.commandline;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class CommandLineOptionsHandler {

	private Options options;
	private CommandLine commandLine;		

	public String[] getArgs() {
		return commandLine.getArgs();
	}

	public CommandLineOptionsHandler(String [] args) {
		this.options = intitialiseOptions();
		this.commandLine = processArgs(args);	
		checkForHelp();
		
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

		Option inMemOption = new Option("inmemory","runs the workflow with data stored in-memory rather than in a database. This can give performance inprovements, at the cost of overall memory usage");

		Options options = new Options();
		options.addOption(helpOption);
		options.addOption(inputOption);
		options.addOption(inputdocOption);
		options.addOption(outputOption);
		options.addOption(outputdocOption);		
		options.addOption(inMemOption);
		
		return options;
		
	}
}
