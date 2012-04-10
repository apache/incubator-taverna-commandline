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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import uk.org.taverna.platform.data.api.Data;
import uk.org.taverna.platform.data.api.ErrorValue;

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
	 */
	public static String buildErrorValueString(ErrorValue errorValue) {

		String errDocumentString = errorValue.getMessage() + "\n";

		String exceptionMessage = errorValue.getExceptionMessage();
		if (exceptionMessage != null && !exceptionMessage.equals("")) {
			DefaultMutableTreeNode exceptionMessageNode = new DefaultMutableTreeNode(
					exceptionMessage);
			errDocumentString += exceptionMessageNode + "\n";
			List<StackTraceElement> stackTrace = errorValue
					.getStackTrace();
			for (StackTraceElement stackTraceElement : stackTrace) {
				errDocumentString += getStackTraceElementString(stackTraceElement)
						+ "\n";
			}
		}

		Set<ErrorValue> errorReferences = errorValue.getCauses();
		if (!errorReferences.isEmpty()) {
			errDocumentString += "Set of cause errors to follow." + "\n";
		}
		int errorCounter = 1;
		for (ErrorValue cause : errorReferences) {
			errDocumentString += "ErrorValue " + (errorCounter++) + "\n";
			errDocumentString += buildErrorValueString(cause) + "\n";
		}

		return errDocumentString;
	}

//	public static void buildErrorDocumentTree(DefaultMutableTreeNode node,
//			ErrorValue errorDocument, InvocationContext context) {
//		DefaultMutableTreeNode child = new DefaultMutableTreeNode(errorDocument);
//		String exceptionMessage = errorDocument.getExceptionMessage();
//		if (exceptionMessage != null && !exceptionMessage.equals("")) {
//			DefaultMutableTreeNode exceptionMessageNode = new DefaultMutableTreeNode(
//					exceptionMessage);
//			child.add(exceptionMessageNode);
//			List<StackTraceElement> stackTrace = errorDocument.getStackTrace();
//			if (stackTrace.size() > 0) {
//				for (StackTraceElement stackTraceElement : stackTrace) {
//					exceptionMessageNode.add(new DefaultMutableTreeNode(
//							getStackTraceElementString(stackTraceElement)));
//				}
//			}
//
//		}
//		node.add(child);
//
//		Set<T2Reference> errorReferences = errorDocument.getErrorReferences();
//		for (T2Reference reference : errorReferences) {
//			if (reference.getReferenceType().equals(
//					T2ReferenceType.ErrorDocument)) {
//				ErrorDocumentService errorDocumentService = context
//						.getReferenceService().getErrorDocumentService();
//				ErrorDocument causeErrorDocument = errorDocumentService
//						.getError(reference);
//				if (errorReferences.size() == 1) {
//					buildErrorDocumentTree(node, causeErrorDocument, context);
//				} else {
//					buildErrorDocumentTree(child, causeErrorDocument, context);
//				}
//			} else if (reference.getReferenceType().equals(
//					T2ReferenceType.IdentifiedList)) {
//				List<ErrorDocument> errorDocuments = getErrorDocuments(
//						reference, context);
//				if (errorDocuments.size() == 1) {
//					buildErrorDocumentTree(node, errorDocuments.get(0), context);
//				} else {
//					for (ErrorDocument errorDocument2 : errorDocuments) {
//						buildErrorDocumentTree(child, errorDocument2, context);
//					}
//				}
//			}
//		}
//	}

	private static String getStackTraceElementString(
			StackTraceElement stackTraceElement) {
		StringBuilder sb = new StringBuilder();
		sb.append(stackTraceElement.getClassName());
		sb.append('.');
		sb.append(stackTraceElement.getMethodName());
		if (stackTraceElement.getFileName() == null) {
			sb.append("(unknown file)");
		} else {
			sb.append('(');
			sb.append(stackTraceElement.getFileName());
			sb.append(':');
			sb.append(stackTraceElement.getLineNumber());
			sb.append(')');
		}
		return sb.toString();
	}

	public static List<ErrorValue> getErrorValues(Data data) {
		List<ErrorValue> errorValues = new ArrayList<ErrorValue>();
		if (data.isError()) {
			errorValues.add((ErrorValue) data.getValue());
		} else if (data.getDepth() > 0) {
			for (Data dataElement : data.getElements()) {
				errorValues.addAll(getErrorValues(dataElement));
			}
		}
		return errorValues;
	}
}
