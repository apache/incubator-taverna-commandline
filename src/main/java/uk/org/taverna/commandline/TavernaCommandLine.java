/*******************************************************************************
 * Copyright (C) 2012 The University of Manchester
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

import java.io.File;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.t2.commandline.exceptions.ArgumentsParsingException;
import net.sf.taverna.t2.commandline.exceptions.InvalidOptionException;
import net.sf.taverna.t2.commandline.options.CommandLineOptions;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import uk.org.taverna.commandline.args.CommandLineArguments;
import uk.org.taverna.configuration.app.ApplicationConfiguration;
import uk.org.taverna.configuration.app.impl.ApplicationConfigurationImpl;
import uk.org.taverna.configuration.app.impl.Log4JConfiguration;
import uk.org.taverna.osgi.OsgiLauncher;

/**
 * Main entry point for starting the Taverna Command Line Tool.
 *
 * @author David Withers
 */
public class TavernaCommandLine {

	private static final String BUNDLE_DIRECTORY = "bundles";

	private static File tavernaCommandlineCommon;

	private static ApplicationConfiguration applicationConfiguration = new ApplicationConfigurationImpl();

	private static Log4JConfiguration log4jConfiguration = new Log4JConfiguration();

	private static final String extraSystemPackages = "org.apache.log4j;version=1.2.16,uk.org.taverna.commandline.args;version=0.1.1";

	/**
	 * Starts the Taverna Command Line Tool.
	 *
	 * @param args Taverna Command Line arguments
	 */
	public static void main(final String[] args) {
		CommandLineOptions commandLineOptions = null;
		try {
			commandLineOptions = new CommandLineOptions(args);
			if (commandLineOptions.askedForHelp()) {
				commandLineOptions.displayHelp();
			} else {
				log4jConfiguration.setApplicationConfiguration(applicationConfiguration);
				log4jConfiguration.prepareLog4J();
				setDerbyPaths();
				OsgiLauncher osgiStarter = new OsgiLauncher(getAppDirectory(), getBundleURIs());
				osgiStarter.setCleanStorageDirectory(true);
				osgiStarter.addSystemPackages(extraSystemPackages);
				osgiStarter.start();
				osgiStarter.startServices(true);
				BundleContext context = osgiStarter.getContext();
				context.registerService("uk.org.taverna.commandline.args.CommandLineArguments",
						new CommandLineArguments() {
							public String[] getCommandLineArguments() {
								return args;
							}
						}, null);
				osgiStarter
						.startBundle(osgiStarter.installBundle(tavernaCommandlineCommon.toURI()));
			}
		} catch (ArgumentsParsingException e) {
			System.out.println(e.getMessage());
		} catch (InvalidOptionException e) {
			System.out.println(e.getMessage());
		} catch (BundleException e) {
			System.out.println(e.getMessage());
		}
	}

	private static List<URI> getBundleURIs() {
		List<URI> bundleURIs = new ArrayList<URI>();
		File[] files = getBundleDirectory().listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});
		for (File file : files) {
			if (file.getName().startsWith("taverna-commandline-common")) {
				tavernaCommandlineCommon = file;
			} else {
				bundleURIs.add(file.toURI());
			}
		}
		return bundleURIs;
	}

	private static File getBundleDirectory() {
		return new File(System.getProperty("taverna.app.startup"), BUNDLE_DIRECTORY);
	}

	private static File getAppDirectory() {
		return new File(applicationConfiguration.getApplicationHomeDir().getAbsolutePath());
	}

	private static void setDerbyPaths() {
		System.setProperty("derby.system.home", getAppDirectory().getAbsolutePath());
		File logFile = new File(applicationConfiguration.getLogDir(), "derby.log");
		System.setProperty("derby.stream.error.file", logFile.getAbsolutePath());
	}

}
