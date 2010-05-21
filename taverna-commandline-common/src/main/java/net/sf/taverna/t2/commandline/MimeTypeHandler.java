package net.sf.taverna.t2.commandline;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.reference.ExternalReferenceSPI;
import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil2;

public class MimeTypeHandler {
	
	private static Logger logger = Logger.getLogger(MimeTypeHandler.class);
	
	@SuppressWarnings("unchecked")
	public static List<MimeType> getMimeTypes(InputStream inputStream,InvocationContext context) throws IOException {
		List<MimeType> mimeList = new ArrayList<MimeType>();
		MimeUtil2 mimeUtil = new MimeUtil2();
		mimeUtil
				.registerMimeDetector("eu.medsea.mimeutil.detector.ExtensionMimeDetector");
		mimeUtil
				.registerMimeDetector("eu.medsea.mimeutil.detector.MagicMimeMimeDetector");
		mimeUtil
				.registerMimeDetector("eu.medsea.mimeutil.detector.WindowsRegistryMimeDetector");
		mimeUtil
				.registerMimeDetector("eu.medsea.mimeutil.detector.ExtraMimeTypes");
		
		try {
			byte[] bytes = new byte[2048];
			inputStream.read(bytes);
			Collection mimeTypes2 = mimeUtil.getMimeTypes(bytes);
			mimeList.addAll(mimeTypes2);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				logger.error(
						"Failed to close stream after determining mimetype", e);
			}
		}
		return mimeList;
	}
		
	public static List<MimeType> getMimeTypes(
			ExternalReferenceSPI externalReference, InvocationContext context) throws IOException {
		
		InputStream inputStream = externalReference.openStream(context);
		return getMimeTypes(inputStream, context);
	}

}
