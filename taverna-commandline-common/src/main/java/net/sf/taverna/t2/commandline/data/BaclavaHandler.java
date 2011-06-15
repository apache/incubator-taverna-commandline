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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import net.sf.taverna.t2.commandline.exceptions.ReadInputException;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

/**
 * Handles creating and reading of Baclava documents. 
 * Baclava is a standard originating from Taverna 1.
 * 
 * @author Stuart Owen
 *
 */
public class BaclavaHandler {

	private static final Logger logger = Logger
			.getLogger(BaclavaHandler.class);

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

	
}
