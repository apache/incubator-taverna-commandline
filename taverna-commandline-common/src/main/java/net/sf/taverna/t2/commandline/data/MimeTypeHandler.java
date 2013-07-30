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

import org.apache.log4j.Logger;

/**
 * Handles identifying mime-types for a given data stream, or T2Reference
 *
 * @author Stuart Owen
 */
public class MimeTypeHandler {

	private static Logger logger = Logger.getLogger(MimeTypeHandler.class);

//	@SuppressWarnings("unchecked")
//	public static List<MimeType> getMimeTypes(InputStream inputStream,InvocationContext context) throws IOException {
//		List<MimeType> mimeList = new ArrayList<MimeType>();
//		MimeUtil2 mimeUtil = new MimeUtil2();
//		mimeUtil
//				.registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
//		mimeUtil
//				.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
//		mimeUtil
//				.registerMimeDetector("eu.medsea.mimeutil.detector.WindowsRegistryMimeDetector");
//		mimeUtil
//				.registerMimeDetector("eu.medsea.mimeutil.detector.ExtraMimeTypes");
//
//		try {
//			byte[] bytes = new byte[2048];
//			inputStream.read(bytes);
//			Collection mimeTypes2 = mimeUtil.getMimeTypes(bytes);
//			mimeList.addAll(mimeTypes2);
//		} finally {
//			try {
//				inputStream.close();
//			} catch (IOException e) {
//				logger.error(
//						"Failed to close stream after determining mimetype", e);
//			}
//		}
//		return mimeList;
//	}
//
//	public static List<String> determineMimeTypes(T2Reference reference,
//			InvocationContext context) throws IOException {
//		List<String> mimeTypeList = new ArrayList<String>();
//
//		if (reference.getReferenceType() == T2ReferenceType.ErrorDocument) {
//			mimeTypeList.add("text/plain");
//		} else {
//			ReferenceSet referenceSet = (ReferenceSet) context
//			.getReferenceService().resolveIdentifier(reference,
//					null, context);
//			if (!referenceSet.getExternalReferences().isEmpty()) {
//
//				ExternalReferenceSPI externalReference = referenceSet
//						.getExternalReferences().iterator().next();
//
//				List<MimeType> mimeTypes = getMimeTypes(
//						externalReference.openStream(context), context);
//
//				for (MimeType type : mimeTypes) {
//					if (!type.toString().equals("text/plain")
//							&& !type.toString().equals(
//									"application/octet-stream")) {
//						mimeTypeList.add(type.toString());
//					}
//				}
//				if (externalReference.getDataNature() == ReferencedDataNature.TEXT) {
//					mimeTypeList.add("text/plain");
//				} else {
//					mimeTypeList.add("application/octet-stream");
//				}
//			}
//
//		}
//
//		return mimeTypeList;
//	}
//
//	public static List<MimeType> getMimeTypes(
//			ExternalReferenceSPI externalReference, InvocationContext context) throws IOException {
//
//		InputStream inputStream = externalReference.openStream(context);
//		return getMimeTypes(inputStream, context);
//	}

}
