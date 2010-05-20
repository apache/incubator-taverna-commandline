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
package net.sf.taverna.t2.commandline.exceptions;

import java.util.Set;

public class InputMismatchException extends InvalidOptionException {	
	
	private static final long serialVersionUID = -5368068332397293706L;
	private final Set<String> expectedInputNames;
	private final Set<String> providedInputNames;
	
	public InputMismatchException(String msg, Set<String> expectedInputNames, Set<String> providedInputNames) {
		super(msg);
		this.expectedInputNames = expectedInputNames;
		this.providedInputNames = providedInputNames;				
	}

	public String getMessage() {
		String result = super.getMessage();
		result += "\n" + expectedInputNames.size() + " inputs were expected";
		if (expectedInputNames.size()>0) result += " which are:\n";
		for (String name : expectedInputNames) {
			result += "'"+name+"' ";			
		}
		
		result += "\n" + providedInputNames.size() + " inputs were provided";
		if (providedInputNames.size()>0) result += " which are:\n";
		for (String name : providedInputNames) {
			result += "'"+name+"' ";			
		}
		return result;
	}
	

}
