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

package org.apache.taverna.commandline;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.taverna.commandline.exceptions.ArgumentsParsingException;
import org.apache.taverna.commandline.exceptions.InvalidOptionException;
import org.apache.taverna.commandline.options.CommandLineOptions;
import org.apache.taverna.configuration.app.ApplicationConfiguration;
import org.apache.taverna.configuration.app.impl.ApplicationConfigurationImpl;
import org.apache.taverna.configuration.app.impl.Log4JConfiguration;
import org.apache.taverna.osgilauncher.OsgiLauncher;
import org.apache.taverna.profile.xml.jaxb.ApplicationProfile;
import org.apache.taverna.profile.xml.jaxb.BundleInfo;
import org.apache.taverna.profile.xml.jaxb.FrameworkConfiguration;

import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * Main entry point for starting the Taverna Command-line Tool.
 *
 * @author David Withers
 */
public class TavernaCommandLine {

	private static final String COMMANDLINE_BUNDLE_NAME = "org.apache.taverna.commandline.taverna-commandline-common";

	private static File commandlineBundle = null;

	private static ApplicationConfiguration applicationConfiguration = new ApplicationConfigurationImpl();

	private static Log4JConfiguration log4jConfiguration = new Log4JConfiguration();

	/**
	 * Starts the Taverna Command-line Tool.
	 *
	 * @param args
	 *            Taverna Command-line Tool arguments
	 */
	public static void main(final String[] args) {
		try {
			CommandLineOptions commandLineOptions = new CommandLineOptionsImpl(args);
			if (commandLineOptions.askedForHelp()) {
				commandLineOptions.displayHelp();
				System.exit(0);
			} else {
				log4jConfiguration.setApplicationConfiguration(applicationConfiguration);
				log4jConfiguration.prepareLog4J();
				setDerbyPaths();
				OsgiLauncher osgilauncher = new OsgiLauncher(getAppDirectory(), getBundleURIs());
				setFrameworkConfiguration(osgilauncher);
				osgilauncher.start();
				BundleContext context = osgilauncher.getContext();
				context.registerService("org.apache.taverna.commandline.options.CommandLineOptions",
						commandLineOptions, null);
				osgilauncher.startServices(true);
				if (commandlineBundle == null) {
					System.err.println("Can't locate command line bundle " + COMMANDLINE_BUNDLE_NAME);
					System.exit(1);
				}
				osgilauncher.startBundle(osgilauncher.installBundle(commandlineBundle.toURI()));
			}
		} catch (ArgumentsParsingException e) {
			System.err.println(e.getMessage());
                        System.exit(2);
		} catch (InvalidOptionException e) {
			System.err.println(e.getMessage());
                        System.exit(3);
		} catch (BundleException e) {
			System.err.println(e.getMessage());
                        e.printStackTrace();
                        System.exit(4);
                } catch (Throwable e) {
			System.err.println(e.getMessage());
                        e.printStackTrace();
                        System.exit(5);
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
		File libDir = new File(applicationConfiguration.getStartupDir().toFile(), "lib");
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
		return new File(applicationConfiguration.getApplicationHomeDir().toFile().getAbsolutePath());
	}

	private static void setDerbyPaths() {
		System.setProperty("derby.system.home", getAppDirectory().getAbsolutePath());
		File logFile = new File(applicationConfiguration.getLogDir().toFile(), "derby.log");
		System.setProperty("derby.stream.error.file", logFile.getAbsolutePath());
	}

}
