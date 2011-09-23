/*******************************************************************************
 * Copyright (C) 2008-2010 The University of Manchester   
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
package net.sf.taverna.t2.commandline;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import uk.org.taverna.commandline.args.CommandLineArguments;
//import org.taverna.launcher.environment.CommandLineArgumentProvider;

import net.sf.taverna.t2.commandline.exceptions.CommandLineMasterPasswordException;
import net.sf.taverna.t2.security.credentialmanager.MasterPasswordProvider;

/**
 * An implementation of {@link MasterPasswordProvider} that reads Credential Manager's  
 * master password from stdin (pipe or terminal) is -cmpassword option is present in command 
 * line arguments. Otherwise it tries to read it from a special file password.txt in a special 
 * directory specified by -cmdir option, if present. 
 * 
 * @author Alex Nenadic
 *
 */
public class CommandLineMasterPasswordProvider implements MasterPasswordProvider{

	private static final String CREDENTIAL_MANAGER_MASTER_PASSWORD_OPTION = "-cmpassword";
	private static final String CREDENTIAL_MANAGER_DIRECTORY_OPTION = "-cmdir";
			
	private static Logger logger = Logger.getLogger(CommandLineMasterPasswordProvider.class);

	private String masterPassword = null;
	private int priority = 200;
	private List<String> commandLineArgumentsList;
	
	private boolean finishedReadingPassword = false;

	public CommandLineMasterPasswordProvider(CommandLineArguments commandLineArgumentsService){
		this.commandLineArgumentsList = new ArrayList<String>(Arrays.asList(commandLineArgumentsService.getCommandLineArguments()));
	}

	@Override
	public String getMasterPassword(boolean firstTime) {
		if (!finishedReadingPassword){						
			// -cmpassword option was present in the command line arguments
			if (commandLineArgumentsList.contains(CREDENTIAL_MANAGER_MASTER_PASSWORD_OPTION)){		
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
			else{
				if (commandLineArgumentsList.contains(CREDENTIAL_MANAGER_DIRECTORY_OPTION)){
					// Try to read the password from a special file located in 
					// Credential Manager directory (if the dir was not null)
					try {
						masterPassword = getCredentialManagerPasswordFromFile();
					} catch (CommandLineMasterPasswordException ex) {
						masterPassword = null;
					}					
				}
			}
			finishedReadingPassword = true; // we do not want to attempt to read from stdin several times
		}
		return masterPassword;
	}
	
	public void setMasterPassword(String masterPassword){
		this.masterPassword = masterPassword;
		finishedReadingPassword = true;
	}

	@Override
	public int getProviderPriority() {
		return priority;
	}
	
	private String getCredentialManagerPasswordFromStdin() throws CommandLineMasterPasswordException{
		
		String password = null;
        
		Console console = System.console();		

		if (console == null) { // password is being piped in, not entered in the terminal by user
			BufferedReader buffReader = null;
    		try {
    			buffReader = new BufferedReader(new InputStreamReader(System.in));
    			password = buffReader.readLine();
    		} 
    		catch (IOException ex) {
    			// For some reason the error of the exception thrown 
    			// does not get printed from the Launcher so print it here as
    			// well as it gives more clue as to what is going wrong.
    			logger.error("An error occured while trying to read Credential Manager's password that was piped in: "
						+ ex.getMessage(), ex); 
    			throw new CommandLineMasterPasswordException(
						"An error occured while trying to read Credential Manager's password that was piped in: "
								+ ex.getMessage(), ex);
				} 
    		finally {
    			try {
    				buffReader.close();
    			} catch (Exception ioe1) {
    				// Ignore
    			}
    		}	  
		}
		else{ // read the password from the terminal as entered by the user
			try {
				// Block until user enters password
				char passwordArray[] = console
						.readPassword("Password for Credential Manager: ");
				if (passwordArray != null) { // user did not abort input
					password = new String(passwordArray);
				} // else password will be null

			} catch (Exception ex) {
    			// For some reason the error of the exception thrown 
    			// does not get printed from the Launcher so print it here as
    			// well as it gives more clue as to what is going wrong.
				logger.error("An error occured while trying to read Credential Manager's password from the terminal: "
								+ ex.getMessage(), ex);
				throw new CommandLineMasterPasswordException(
						"An error occured while trying to read Credential Manager's password from the terminal: "
								+ ex.getMessage(), ex);
			}
		}
        return password;
	}

	private String getCredentialManagerPasswordFromFile() throws CommandLineMasterPasswordException{
		
		String cmDir = null;
		
		if (commandLineArgumentsList.contains(CREDENTIAL_MANAGER_DIRECTORY_OPTION)){
			// Get the -cmdir option's parameter value - it is one place after the 
			// option in the arguments array
			int cmDirIndex = commandLineArgumentsList.indexOf(CREDENTIAL_MANAGER_DIRECTORY_OPTION) + 1;
			cmDir = commandLineArgumentsList.get(cmDirIndex);
		}
		
		if (cmDir == null){
			return null;
		}
		
		File passwordFile = new File(cmDir, "password.txt");
		String password = null;
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
		return password;
	}
}
