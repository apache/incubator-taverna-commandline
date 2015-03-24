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

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.taverna.commandline.exceptions.CommandLineMasterPasswordException;
import org.apache.taverna.commandline.options.CommandLineOptions;
import org.apache.taverna.security.credentialmanager.MasterPasswordProvider;

import org.apache.log4j.Logger;

/**
 * An implementation of {@link MasterPasswordProvider} that reads Credential Manager's
 * master password from stdin (pipe or terminal) is -cmpassword option is present in command
 * line arguments. Otherwise it tries to read it from a special file password.txt in a special
 * directory specified by -cmdir option, if present.
 *
 * @author Alex Nenadic
 */
public class CommandLineMasterPasswordProvider implements MasterPasswordProvider {

	private static final String CREDENTIAL_MANAGER_MASTER_PASSWORD_OPTION = "cmpassword";
	private static final String CREDENTIAL_MANAGER_DIRECTORY_OPTION = "cmdir";

	private static Logger logger = Logger.getLogger(CommandLineMasterPasswordProvider.class);

	private String masterPassword = null;
	private int priority = 200;

	private boolean finishedReadingPassword = false;
	private final CommandLineOptions commandLineOptions;

	public CommandLineMasterPasswordProvider(CommandLineOptions commandLineOptions) {
		this.commandLineOptions = commandLineOptions;
	}

	@Override
	public String getMasterPassword(boolean firstTime) {
		if (!finishedReadingPassword) {
			// -cmpassword option was present in the command line arguments
			if (commandLineOptions.hasOption(CREDENTIAL_MANAGER_MASTER_PASSWORD_OPTION)) {
				// Try to read the password from stdin (terminal or pipe)
				try {
					masterPassword = getCredentialManagerPasswordFromStdin();
				} catch (CommandLineMasterPasswordException e) {
					masterPassword = null;
				}
			}
			// -cmpassword option was not present in the command line arguments
			// and -cmdir option was there - try to get the master password from
			// the "special" password file password.txt inside the Cred. Manager directory.
			else {
				if (commandLineOptions.hasOption(CREDENTIAL_MANAGER_DIRECTORY_OPTION)) {
					// Try to read the password from a special file located in
					// Credential Manager directory (if the dir was not null)
					try {
						masterPassword = getCredentialManagerPasswordFromFile();
					} catch (CommandLineMasterPasswordException ex) {
						masterPassword = null;
					}
				}
			}
			finishedReadingPassword = true; // we do not want to attempt to read from stdin several
											// times
		}
		return masterPassword;
	}

	public void setMasterPassword(String masterPassword) {
		this.masterPassword = masterPassword;
		finishedReadingPassword = true;
	}

	@Override
	public int getProviderPriority() {
		return priority;
	}

	private String getCredentialManagerPasswordFromStdin()
			throws CommandLineMasterPasswordException {

		String password = null;

		Console console = System.console();

		if (console == null) { // password is being piped in, not entered in the terminal by user
			BufferedReader buffReader = null;
			try {
				buffReader = new BufferedReader(new InputStreamReader(System.in));
				password = buffReader.readLine();
			} catch (IOException ex) {
				// For some reason the error of the exception thrown
				// does not get printed from the Launcher so print it here as
				// well as it gives more clue as to what is going wrong.
				logger.error(
						"An error occured while trying to read Credential Manager's password that was piped in: "
								+ ex.getMessage(), ex);
				throw new CommandLineMasterPasswordException(
						"An error occured while trying to read Credential Manager's password that was piped in: "
								+ ex.getMessage(), ex);
			} finally {
				try {
					buffReader.close();
				} catch (Exception ioe1) {
					// Ignore
				}
			}
		} else { // read the password from the terminal as entered by the user
			try {
				// Block until user enters password
				char passwordArray[] = console.readPassword("Password for Credential Manager: ");
				if (passwordArray != null) { // user did not abort input
					password = new String(passwordArray);
				} // else password will be null

			} catch (Exception ex) {
				// For some reason the error of the exception thrown
				// does not get printed from the Launcher so print it here as
				// well as it gives more clue as to what is going wrong.
				logger.error(
						"An error occured while trying to read Credential Manager's password from the terminal: "
								+ ex.getMessage(), ex);
				throw new CommandLineMasterPasswordException(
						"An error occured while trying to read Credential Manager's password from the terminal: "
								+ ex.getMessage(), ex);
			}
		}
		return password;
	}

	private String getCredentialManagerPasswordFromFile() throws CommandLineMasterPasswordException {
		String password = null;
		if (commandLineOptions.hasOption(CREDENTIAL_MANAGER_DIRECTORY_OPTION)) {
			String cmDir = commandLineOptions.getCredentialManagerDir();

			File passwordFile = new File(cmDir, "password.txt");
			BufferedReader buffReader = null;
			try {
				buffReader = new BufferedReader(new FileReader(passwordFile));
				password = buffReader.readLine();
			} catch (IOException ioe) {
				// For some reason the error of the exception thrown
				// does not get printed from the Launcher so print it here as
				// well as it gives more clue as to what is going wrong.
				logger.error("There was an error reading the Credential Manager password from "
						+ passwordFile.toString() + ": " + ioe.getMessage(), ioe);
				throw new CommandLineMasterPasswordException(
						"There was an error reading the Credential Manager password from "
								+ passwordFile.toString() + ": " + ioe.getMessage(), ioe);
			} finally {
				try {
					buffReader.close();
				} catch (Exception ioe1) {
					// Ignore
				}
			}
		}
		return password;
	}
}
