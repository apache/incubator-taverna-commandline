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
package net.sf.taverna.t2.commandline.data;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import uk.org.taverna.databundle.DataBundles;
import uk.org.taverna.databundle.ErrorDocument;

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
