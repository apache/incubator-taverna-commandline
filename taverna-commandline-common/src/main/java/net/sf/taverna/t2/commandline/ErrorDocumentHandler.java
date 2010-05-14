package net.sf.taverna.t2.commandline;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ErrorDocumentService;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ListService;
import net.sf.taverna.t2.reference.StackTraceElementBean;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;

public class ErrorDocumentHandler {

	/**
	 * Creates a string representation of the ErrorDocument.
	 */
	public static String buildErrorDocumentString(ErrorDocument errDocument,
			InvocationContext context) {

		String errDocumentString = "";

		String exceptionMessage = errDocument.getExceptionMessage();
		if (exceptionMessage != null && !exceptionMessage.equals("")) {
			DefaultMutableTreeNode exceptionMessageNode = new DefaultMutableTreeNode(
					exceptionMessage);
			errDocumentString += exceptionMessageNode + "\n";
			List<StackTraceElementBean> stackTrace = errDocument
					.getStackTraceStrings();
			if (stackTrace.size() > 0) {
				for (StackTraceElementBean stackTraceElement : stackTrace) {
					errDocumentString += getStackTraceElementString(stackTraceElement)
							+ "\n";
				}
			}

		}

		Set<T2Reference> errorReferences = errDocument.getErrorReferences();
		if (!errorReferences.isEmpty()) {
			errDocumentString += "Set of ErrorDocumentS to follow." + "\n";
		}
		int errorCounter = 1;
		int listCounter = 0;
		for (T2Reference reference : errorReferences) {
			if (reference.getReferenceType().equals(
					T2ReferenceType.ErrorDocument)) {
				ErrorDocumentService errorDocumentService = context
						.getReferenceService().getErrorDocumentService();
				ErrorDocument causeErrorDocument = errorDocumentService
						.getError(reference);
				if (listCounter == 0) {
					errDocumentString += "ErrorDocument " + (errorCounter++)
							+ "\n";
				} else {
					errDocumentString += "ErrorDocument " + listCounter + "."
							+ (errorCounter++) + "\n";
				}
				errDocumentString += buildErrorDocumentString(
						causeErrorDocument, context)
						+ "\n";
			} else if (reference.getReferenceType().equals(
					T2ReferenceType.IdentifiedList)) {
				List<ErrorDocument> errorDocuments = getErrorDocuments(
						reference, context);
				errDocumentString += "ErrorDocument list " + (++listCounter)
						+ "\n";
				for (ErrorDocument causeErrorDocument : errorDocuments) {
					errDocumentString += buildErrorDocumentString(
							causeErrorDocument, context)
							+ "\n";
				}
			}
		}

		return errDocumentString;
	}

	public static void buildErrorDocumentTree(DefaultMutableTreeNode node,
			ErrorDocument errorDocument, InvocationContext context) {
		DefaultMutableTreeNode child = new DefaultMutableTreeNode(errorDocument);
		String exceptionMessage = errorDocument.getExceptionMessage();
		if (exceptionMessage != null && !exceptionMessage.equals("")) {
			DefaultMutableTreeNode exceptionMessageNode = new DefaultMutableTreeNode(
					exceptionMessage);
			child.add(exceptionMessageNode);
			List<StackTraceElementBean> stackTrace = errorDocument
					.getStackTraceStrings();
			if (stackTrace.size() > 0) {
				for (StackTraceElementBean stackTraceElement : stackTrace) {
					exceptionMessageNode.add(new DefaultMutableTreeNode(
							getStackTraceElementString(stackTraceElement)));
				}
			}

		}
		node.add(child);

		Set<T2Reference> errorReferences = errorDocument.getErrorReferences();
		for (T2Reference reference : errorReferences) {
			if (reference.getReferenceType().equals(
					T2ReferenceType.ErrorDocument)) {
				ErrorDocumentService errorDocumentService = context
						.getReferenceService().getErrorDocumentService();
				ErrorDocument causeErrorDocument = errorDocumentService
						.getError(reference);
				if (errorReferences.size() == 1) {
					buildErrorDocumentTree(node, causeErrorDocument, context);
				} else {
					buildErrorDocumentTree(child, causeErrorDocument, context);
				}
			} else if (reference.getReferenceType().equals(
					T2ReferenceType.IdentifiedList)) {
				List<ErrorDocument> errorDocuments = getErrorDocuments(
						reference, context);
				if (errorDocuments.size() == 1) {
					buildErrorDocumentTree(node, errorDocuments.get(0), context);
				} else {
					for (ErrorDocument errorDocument2 : errorDocuments) {
						buildErrorDocumentTree(child, errorDocument2, context);
					}
				}
			}
		}
	}

	private static String getStackTraceElementString(
			StackTraceElementBean stackTraceElement) {
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

	public static List<ErrorDocument> getErrorDocuments(T2Reference reference,
			InvocationContext context) {
		List<ErrorDocument> errorDocuments = new ArrayList<ErrorDocument>();
		if (reference.getReferenceType().equals(T2ReferenceType.ErrorDocument)) {
			ErrorDocumentService errorDocumentService = context
					.getReferenceService().getErrorDocumentService();
			errorDocuments.add(errorDocumentService.getError(reference));
		} else if (reference.getReferenceType().equals(
				T2ReferenceType.IdentifiedList)) {
			ListService listService = context.getReferenceService()
					.getListService();
			IdentifiedList<T2Reference> list = listService.getList(reference);
			for (T2Reference listReference : list) {
				errorDocuments
						.addAll(getErrorDocuments(listReference, context));
			}
		}
		return errorDocuments;
	}
}
