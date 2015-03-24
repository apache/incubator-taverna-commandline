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

package org.apache.taverna.commandline.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.taverna.databundle.DataBundles;
import org.apache.taverna.databundle.ErrorDocument;

/**
 * Handles ErrorValues and transforming them into String representations
 * that can be stored as a file, or within a Baclava document.
 *
 * @author Stuart Owen
 * @author David Withers
 */
public class ErrorValueHandler {

	/**
	 * Creates a string representation of the ErrorValue.
	 * @throws IOException
	 */
	public static String buildErrorValueString(ErrorDocument errorValue) throws IOException {

		String errDocumentString = errorValue.getMessage() + "\n";

		String exceptionMessage = errorValue.getMessage();
		if (exceptionMessage != null && !exceptionMessage.equals("")) {
			DefaultMutableTreeNode exceptionMessageNode = new DefaultMutableTreeNode(
					exceptionMessage);
			errDocumentString += exceptionMessageNode + "\n";
			errDocumentString += errorValue.getTrace();
		}

		List<Path> errorReferences = errorValue.getCausedBy();
		if (!errorReferences.isEmpty()) {
			errDocumentString += "Set of cause errors to follow." + "\n";
		}
		int errorCounter = 1;
		for (Path cause : errorReferences) {
			if (DataBundles.isError(cause)) {
			errDocumentString += "ErrorValue " + (errorCounter++) + "\n";
			errDocumentString += buildErrorValueString(DataBundles.getError(cause)) + "\n";
			}
		}

		return errDocumentString;
	}

}
