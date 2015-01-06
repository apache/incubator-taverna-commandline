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
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.commandline.exceptions.ArgumentsParsingException;
import net.sf.taverna.t2.commandline.exceptions.InvalidOptionException;
import net.sf.taverna.t2.commandline.options.CommandLineOptions;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import uk.org.taverna.commons.profile.xml.jaxb.ApplicationProfile;
import uk.org.taverna.commons.profile.xml.jaxb.BundleInfo;
import uk.org.taverna.commons.profile.xml.jaxb.FrameworkConfiguration;
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

	private static final String COMMANDLINE_BUNDLE_NAME = "org.apache.taverna.commandline.taverna-commandline-common";

	private static File commandlineBundle = null;

	private static ApplicationConfiguration applicationConfiguration = new ApplicationConfigurationImpl();

	private static Log4JConfiguration log4jConfiguration = new Log4JConfiguration();

	/**
	 * Starts the Taverna Command Line Tool.
	 *
	 * @param args
	 *            Taverna Command Line arguments
	 */
	public static void main(final String[] args) {
		try {
			CommandLineOptions commandLineOptions = new CommandLineOptionsImpl(args);
			if (commandLineOptions.askedForHelp()) {
				commandLineOptions.displayHelp();
			} else {
				log4jConfiguration.setApplicationConfiguration(applicationConfiguration);
				log4jConfiguration.prepareLog4J();
				setDerbyPaths();
				OsgiLauncher osgilauncher = new OsgiLauncher(getAppDirectory(), getBundleURIs());
				setFrameworkConfiguration(osgilauncher);
				osgilauncher.start();
				BundleContext context = osgilauncher.getContext();
				context.registerService("net.sf.taverna.t2.commandline.options.CommandLineOptions",
						commandLineOptions, null);
				osgilauncher.startServices(true);
        if (commandlineBundle == null) {
          System.err.println("Can't locate command line bundle " + COMMANDLINE_BUNDLE_NAME);
          System.exit(1);
        }
				osgilauncher.startBundle(osgilauncher.installBundle(commandlineBundle.toURI()));
			}
		} catch (ArgumentsParsingException e) {
			System.out.println(e.getMessage());
		} catch (InvalidOptionException e) {
			System.out.println(e.getMessage());
		} catch (BundleException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Sets the OSGi Framework configuration.
	 *
	 * @param osgilauncher
	 */
	private static void setFrameworkConfiguration(OsgiLauncher osgilauncher) {
		ApplicationProfile applicationProfile = applicationConfiguration.getApplicationProfile();
		List<FrameworkConfiguration> frameworkConfigurations = applicationProfile
				.getFrameworkConfiguration();
		if (!frameworkConfigurations.isEmpty()) {
			Map<String, String> configurationMap = new HashMap<String, String>();
			for (FrameworkConfiguration frameworkConfiguration : frameworkConfigurations) {
				configurationMap.put(frameworkConfiguration.getName(),
						frameworkConfiguration.getValue());
			}
			osgilauncher.setFrameworkConfiguration(configurationMap);
		}
	}

	private static List<URI> getBundleURIs() {
		List<URI> bundleURIs = new ArrayList<URI>();
		ApplicationProfile applicationProfile = applicationConfiguration.getApplicationProfile();
		File libDir = new File(applicationConfiguration.getStartupDir(), "lib");
		if (applicationProfile != null) {
			for (BundleInfo bundle : applicationProfile.getBundle()) {
				File bundleFile = new File(libDir, bundle.getFileName());
				if (bundle.getSymbolicName().equals(COMMANDLINE_BUNDLE_NAME)) {
					commandlineBundle = bundleFile;
				} else {
					bundleURIs.add(bundleFile.toURI());
				}
			}
		}
		return bundleURIs;
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
