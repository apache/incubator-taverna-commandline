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
package net.sf.taverna.t2.commandline;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.commandline.exceptions.ReadInputException;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.DereferenceException;
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.ReferencedDataNature;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import eu.medsea.mimeutil.MimeType;

public class BaclavaDocumentHandler {

	private static Namespace namespace = Namespace.getNamespace("b",
			"http://org.embl.ebi.escience/baclava/0.1alpha");

	private static final Logger logger = Logger
			.getLogger(BaclavaDocumentHandler.class);

	public Map<String, DataThing> readInputDocument(String inputDocPath)
			throws ReadInputException {
		URL url;
		try {
			url = new URL("file:");
		} catch (MalformedURLException e1) {
			// Should never happen, but just incase:
			throw new ReadInputException(
					"The was an internal error setting up the URL to open the inputs. You should contact Taverna support.",
					e1);
		}
		URL inputDocURL;
		try {
			inputDocURL = new URL(url, inputDocPath);
		} catch (MalformedURLException e1) {
			throw new ReadInputException(
					"The a error reading the input document from : "
							+ inputDocPath + ", " + e1.getMessage(), e1);
		}
		SAXBuilder builder = new SAXBuilder();
		Document inputDoc;
		try {
			inputDoc = builder.build(inputDocURL.openStream());
		} catch (IOException e) {
			throw new ReadInputException(
					"There was an error reading the input document file: "
							+ e.getMessage(), e);
		} catch (JDOMException e) {
			throw new ReadInputException(
					"There was a error processing the input document XML: "
							+ e.getMessage(), e);
		}
		Map<String, DataThing> things = DataThingXMLFactory
				.parseDataDocument(inputDoc);
		return things;
	}

	public void storeDocument(Map<String, WorkflowDataToken> finalResults,
			File outputFile) throws DereferenceException, IOException {

		Map<String, DataThing> dataThings = backDataThingMapFromWorkflowDataTokens(finalResults);

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
			InvocationContext context) {

		if (reference.getReferenceType() == T2ReferenceType.ReferenceSet) {
			// Dereference the object
			Object dataValue;

			dataValue = context.getReferenceService().renderIdentifier(
					reference, Object.class, context);			
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
	 * Returns a org.jdom.Document from a map of port named to DataThingS
	 * containing the port's results.
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
	protected Map<String, DataThing> bakeDataThingMap(
			Map<String, Object> resultMap) {

		Map<String, DataThing> dataThingMap = new HashMap<String, DataThing>();
		for (Iterator<String> i = resultMap.keySet().iterator(); i.hasNext();) {
			String portName = (String) i.next();
			dataThingMap.put(portName, DataThingFactory.bake(resultMap
					.get(portName)));
		}
		return dataThingMap;
	}

	protected Map<String, DataThing> backDataThingMapFromWorkflowDataTokens(
			Map<String, WorkflowDataToken> tokenMap)
			throws DereferenceException, IOException {
		Map<String, DataThing> dataThingMap = new HashMap<String, DataThing>();
		for (Iterator<String> i = tokenMap.keySet().iterator(); i.hasNext();) {
			String portName = (String) i.next();
			WorkflowDataToken token = tokenMap.get(portName);
			InvocationContext context = token.getContext();
			List<String> mimeTypeList = new ArrayList<String>();

			if (token.getData().getReferenceType() == T2ReferenceType.ReferenceSet) {
				
				mimeTypeList
						.addAll(MimeTypeHandler.determineMimeTypes(token.getData(), context));				
				DataThing thingy = DataThingFactory.bake(convertReferencesToObjects(token.getData(), context));
				thingy.getMetadata().setMIMETypes(mimeTypeList);				
				dataThingMap.put(portName, thingy);
				
			} else if (token.getData().getReferenceType() == T2ReferenceType.ErrorDocument) {
				
				DataThing thingy = DataThingFactory.bake(convertReferencesToObjects(token.getData(), context));
				thingy.getMetadata().addMIMEType("text/plain");
				dataThingMap.put(portName, thingy);
				
			} else {
				
				IdentifiedList<T2Reference> identifiedList = context
						.getReferenceService().getListService().getList(
								token.getData());
				List<Object> list = new ArrayList<Object>();

				for (int j = 0; j < identifiedList.size(); j++) {
					T2Reference ref = identifiedList.get(j);
					list.add(convertReferencesToObjects(ref, context));
				}

				// ripple through to get the leaf, to find its mimetype. Seems a
				// limitation of Baclava
				// is that the mime-type is set on the entire list, and is not
				// possible for individual items
				T2Reference ref = token.getData();
				while (ref.getReferenceType() != T2ReferenceType.ReferenceSet
						&& ref.getReferenceType() != T2ReferenceType.ErrorDocument) {
					identifiedList = context.getReferenceService()
							.getListService().getList(ref);
					ref = identifiedList.get(0);
				}
				mimeTypeList.addAll(MimeTypeHandler.determineMimeTypes(ref, context));
				DataThing thingy = DataThingFactory.bake(list);
				thingy.getMetadata().setMIMETypes(mimeTypeList);
				dataThingMap.put(portName, thingy);
				
			}
		}
		return dataThingMap;
	}

	
}
