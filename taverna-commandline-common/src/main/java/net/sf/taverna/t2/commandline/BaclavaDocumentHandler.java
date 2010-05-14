package net.sf.taverna.t2.commandline;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class BaclavaDocumentHandler {

	private static Namespace namespace = Namespace.getNamespace("b","http://org.embl.ebi.escience/baclava/0.1alpha");
	
	private static final Logger logger = Logger
			.getLogger(BaclavaDocumentHandler.class);
	
	

	public void storeDocument(Map<String, WorkflowDataToken> finalResults,
			File outputFile) throws Exception {
		// Build the DataThing map from the chosenReferences
		// First convert map of references to objects into a map of real result
		// objects
		Map<String, Object> resultMap = new HashMap<String, Object>();
		for (String portName : finalResults.keySet()) {
			resultMap.put(portName, convertReferencesToObjects(finalResults.get(portName).getData(), finalResults.get(portName).getContext()));
		}
		Map<String, DataThing> dataThings = bakeDataThingMap(resultMap);

		// Build the string containing the XML document from the result map
		Document doc = getDataDocument(dataThings);
		XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
		String xmlString = xo.outputString(doc);
		PrintWriter out = new PrintWriter(new FileWriter(outputFile));
		out.print(xmlString);
		out.flush();
		out.close();
	}

	protected Object convertReferencesToObjects(T2Reference reference,
			InvocationContext context) throws Exception {

		if (reference.getReferenceType() == T2ReferenceType.ReferenceSet) {
			// Dereference the object
			Object dataValue;
			try {
				dataValue = context.getReferenceService().renderIdentifier(
						reference, Object.class, context);
			} catch (ReferenceServiceException rse) {
				String message = "Problem rendering T2Reference in convertReferencesToObjects().";
				logger.error("SaveAllResultsAsXML Error: " + message, rse);
				throw new Exception(message);
			}
			return dataValue;
		} else if (reference.getReferenceType() == T2ReferenceType.ErrorDocument) {
			// Dereference the ErrorDocument and convert it to some string
			// representation
			ErrorDocument errorDocument = (ErrorDocument) context
					.getReferenceService().resolveIdentifier(reference, null,
							context);
			String errorString = ErrorDocumentHandler.buildErrorDocumentString(
					errorDocument, context);
			return errorString;			
		} else { // it is an IdentifiedList<T2Reference> - go recursively
			IdentifiedList<T2Reference> identifiedList = context
					.getReferenceService().getListService().getList(reference);
			List<Object> list = new ArrayList<Object>();

			for (int j = 0; j < identifiedList.size(); j++) {
				T2Reference ref = identifiedList.get(j);
				list.add(convertReferencesToObjects(ref, context));
			}
			return list;
		}
	}		
	
	/**
	 * Returns a org.jdom.Document from a map of port named to DataThingS containing
	 * the port's results.
	 */
	public static Document getDataDocument(Map<String, DataThing> dataThings) {
		Element rootElement = new Element("dataThingMap", namespace);
		Document theDocument = new Document(rootElement);
		for (Iterator<String> i = dataThings.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			DataThing value = (DataThing) dataThings.get(key);
			Element dataThingElement = new Element("dataThing", namespace);
			dataThingElement.setAttribute("key", key);
			dataThingElement.addContent(value.getElement());
			rootElement.addContent(dataThingElement);
		}
		return theDocument;
	}

	/**
	 * Returns a map of port names to DataThings from a map of port names to a 
	 * list of (lists of ...) result objects.
	 */
	protected Map<String, DataThing> bakeDataThingMap(Map<String, Object> resultMap) {
		
		Map<String, DataThing> dataThingMap = new HashMap<String, DataThing>();
		for (Iterator<String> i = resultMap.keySet().iterator(); i.hasNext();) {
			String portName = (String) i.next();
			dataThingMap.put(portName, DataThingFactory.bake(resultMap.get(portName)));
		}
		return dataThingMap;
	}
}
