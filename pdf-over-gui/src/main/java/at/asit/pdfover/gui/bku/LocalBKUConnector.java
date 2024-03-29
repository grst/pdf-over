/*
 * Copyright 2012 by A-SIT, Secure Information Technology Center Austria
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */
package at.asit.pdfover.gui.bku;

// Imports
import java.io.IOException;


import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.gui.Constants;
import at.asit.pdfover.gui.utils.FileUploadSource;
import at.asit.pdfover.signator.BkuSlConnector;
import at.asit.pdfover.signator.SLRequest;
import at.asit.pdfover.signator.SLResponse;
import at.asit.pdfover.signator.SignatureException;

/**
 * 
 */
public class LocalBKUConnector implements BkuSlConnector {
	/**
	 * SLF4J Logger instance
	 **/
	private static final Logger log = LoggerFactory
			.getLogger(LocalBKUConnector.class);

	/**
	 * HTTP Response server HEADER
	 */
	public final static String BKU_RESPONSE_HEADER_SERVER = "server"; //$NON-NLS-1$

	/**
	 * HTTP Response user-agent HEADER
	 */
	public final static String BKU_RESPONSE_HEADER_USERAGENT = "user-agent"; //$NON-NLS-1$

	/**
	 * HTTP Response SignatureLayout HEADER
	 */
	public final static String BKU_RESPONSE_HEADER_SIGNATURE_LAYOUT = "SignatureLayout"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see at.asit.pdfover.signator.BkuSlConnector#handleSLRequest(java.lang.String)
	 */
	@Override
	public SLResponse handleSLRequest(SLRequest request) throws SignatureException {
		try {
			HttpClient client = BKUHelper.getHttpClient();
			PostMethod method = new PostMethod(Constants.LOCAL_BKU_URL);

			String sl_request = request.getRequest();
			if (request.getSignatureData() == null) {
				method.addParameter("XMLRequest", sl_request); //$NON-NLS-1$
			} else {
				StringPart xmlpart = new StringPart(
						"XMLRequest", sl_request, "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$

				FilePart filepart = new FilePart("fileupload",	//$NON-NLS-1$
						new FileUploadSource(request.getSignatureData()));

				Part[] parts = { xmlpart, filepart };

				method.setRequestEntity(new MultipartRequestEntity(parts, method
						.getParams()));
			}
			log.trace("SL REQUEST: " + sl_request); //$NON-NLS-1$

			int returnCode = client.executeMethod(method);

			if (returnCode != HttpStatus.SC_OK) {
				throw new HttpException(
						method.getResponseBodyAsString());
			}
				
			String server = getResponseHeader(method, BKU_RESPONSE_HEADER_SERVER);
			if (server == null)
				server = ""; //$NON-NLS-1$
			String userAgent = getResponseHeader(method, BKU_RESPONSE_HEADER_USERAGENT);
			if (userAgent == null)
				userAgent = ""; //$NON-NLS-1$
			String signatureLayout = getResponseHeader(method, BKU_RESPONSE_HEADER_SIGNATURE_LAYOUT);

			String response = method.getResponseBodyAsString();
			log.debug("SL Response: " + response); //$NON-NLS-1$
			SLResponse slResponse = new SLResponse(response, server,
					userAgent, signatureLayout);
			return slResponse;
		} catch (HttpException e) {
			log.error("LocalBKUConnector: ", e); //$NON-NLS-1$
			throw new SignatureException(e);
		} catch (IOException e) {
			log.error("LocalBKUConnector: ", e); //$NON-NLS-1$
			throw new SignatureException(e);
		}
	}

	/**
	 * Returns the value corresponding to the given header name
	 * @param method the HTTP method
	 * @param headerName the header name
	 * @return the header value (or null if not found)
	 */
	private static String getResponseHeader(HttpMethod method, String headerName) {
		if (method.getResponseHeader(headerName) == null)
			return null;
		return method.getResponseHeader(headerName).getValue();
	}
}
