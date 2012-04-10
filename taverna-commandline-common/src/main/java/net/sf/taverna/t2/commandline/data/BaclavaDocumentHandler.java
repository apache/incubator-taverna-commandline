package net.sf.taverna.t2.commandline.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.taverna.t2.baclava.DataThing;
import net.sf.taverna.t2.baclava.factory.DataThingFactory;
import net.sf.taverna.t2.baclava.factory.DataThingXMLFactory;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import uk.org.taverna.platform.data.api.Data;
import uk.org.taverna.platform.data.api.ErrorValue;

/**
 * Handles the loading and saving of T2Reference data as Baclava documents
 *
 * @author Stuart Owen
 * @author David Withers
 */
public class BaclavaDocumentHandler {

	private Map<String, Data> chosenReferences;

	private static Namespace namespace = Namespace.getNamespace("b",
			"http://org.embl.ebi.escience/baclava/0.1alpha");

	/**
	 * Reads a baclava document from an InputStream and returns a map of DataThings mapped to the
	 * portName
	 *
	 * @throws IOException
	 *             , JDOMException
	 */
	public Map<String, DataThing> readData(InputStream inputStream) throws IOException,
			JDOMException {

		SAXBuilder builder = new SAXBuilder();
		Document inputDoc;
		inputDoc = builder.build(inputStream);

		return DataThingXMLFactory.parseDataDocument(inputDoc);
	}

	/**
	 * Saves the result data to an XML Baclava file.
	 *
	 * @throws IOException
	 */
	public void saveData(File file) throws IOException {
		// Build the string containing the XML document from the result map
		Document doc = getDataDocument();
		XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
		PrintWriter out = new PrintWriter(new FileWriter(file));
		xo.output(doc, out);
	}

	/**
	 * Returns a org.jdom.Document from a map of port named to DataThingS containing the port's
	 * results.
	 */
	public Document getDataDocument() {
		Element rootElement = new Element("dataThingMap", namespace);
		Document theDocument = new Document(rootElement);
		// Build the DataThing map from the chosenReferences
		// First convert map of references to objects into a map of real result
		// objects
		for (String portName : getChosenReferences().keySet()) {
			DataThing thing = DataThingFactory.bake(getObjectForName(portName));
			Element dataThingElement = new Element("dataThing", namespace);
			dataThingElement.setAttribute("key", portName);
			dataThingElement.addContent(thing.getElement());
			rootElement.addContent(dataThingElement);
		}
		return theDocument;
	}

	protected Object getObjectForName(String name) {
		Object result = null;
		if (getChosenReferences().containsKey(name)) {
			result = convertToObject(getChosenReferences().get(name));
		}
		if (result == null) {
			result = "null";
		}
		return result;
	}

	private Object convertToObject(Data data) {
		if (data.getDepth() > 0) {
			List<Object> objectList = new ArrayList<Object>();
			for (Data dataElement : data.getElements()) {
				objectList.add(convertToObject(dataElement));
			}
			return objectList;
		} else if (data.isError()) {
			return ErrorValueHandler.buildErrorValueString((ErrorValue) data.getValue());
		} else {
			return data.getValue();
		}
	}

	private Map<String, Data> getChosenReferences() {
		return chosenReferences;
	}

	public void setChosenReferences(Map<String, Data> chosenReferences) {
		this.chosenReferences = chosenReferences;
	}
}
