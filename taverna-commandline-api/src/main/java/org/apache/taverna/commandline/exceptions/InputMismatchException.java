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

package org.apache.taverna.commandline.exceptions;

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
		
		if (expectedInputNames != null){
			result += "\n" + expectedInputNames.size() + " inputs were expected";
			if (expectedInputNames.size()>0) result += " which are:\n";
			for (String name : expectedInputNames) {
				result += "'"+name+"' ";			
			}			
		}
		
		if (providedInputNames != null){
			result += "\n" + providedInputNames.size()
					+ " inputs were provided";
			if (providedInputNames.size() > 0)
				result += " which are:\n";
			for (String name : providedInputNames) {
				result += "'" + name + "' ";
			}
		}
		return result;
	}
	

}
