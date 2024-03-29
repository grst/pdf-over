package at.asit.pdfover.signer.pdfas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.asit.pdfover.signator.BKUs;
import at.asit.pdfover.signator.ByteArrayDocumentSource;
import at.asit.pdfover.signator.SLResponse;
import at.asit.pdfover.signator.SignResult;
import at.asit.pdfover.signator.SignResultImpl;
import at.asit.pdfover.signator.SignatureException;
import at.asit.pdfover.signator.SignatureParameter;
import at.asit.pdfover.signator.SignaturePosition;
import at.asit.pdfover.signator.Signer;
import at.asit.pdfover.signator.SigningState;
import at.asit.pdfover.signer.pdfas.exceptions.PDFASSLRequestException;
import at.gv.egiz.pdfas.api.PdfAs;
import at.gv.egiz.pdfas.api.commons.Constants;
import at.gv.egiz.pdfas.api.exceptions.PdfAsException;
import at.gv.egiz.pdfas.api.internal.LocalBKUParams;
import at.gv.egiz.pdfas.api.internal.PdfAsInternal;
import at.gv.egiz.pdfas.api.sign.SignParameters;
import at.gv.egiz.pdfas.api.sign.SignatureDetailInformation;
import at.gv.egiz.pdfas.io.ByteArrayDataSink;

/**
 * PDF AS Signer Implementation
 */
public class PDFASSigner implements Signer {

	/**
	 * SLF4J Logger instance
	 **/
	static final Logger log = LoggerFactory.getLogger(PDFASSigner.class);

	/**
	 * The template URL
	 */
	protected static final String URL_TEMPLATE = "http://pdfover.4.gv.at/template";

	/**
	 * Location reference string
	 */
	protected static final String LOC_REF = "<sl:LocRefContent>" + URL_TEMPLATE
			+ "</sl:LocRefContent>";

	@Override
	public SigningState prepare(SignatureParameter parameter)
			throws SignatureException {
		try {
			PdfAsSignatureParameter sign_para = null;

			if (PdfAsSignatureParameter.class.isInstance(parameter)) {
				sign_para = PdfAsSignatureParameter.class.cast(parameter);
			}

			if (sign_para == null) {
				throw new SignatureException("Incorrect SignatureParameter!");
			}

			PdfAs pdfas = PDFASHelper.getPdfAs();

			PDFASSigningState state = new PDFASSigningState();

			SignParameters params = new SignParameters();
			params.setSignaturePositioning(sign_para.getPDFASPositioning());

			if (parameter.getSignatureDevice() == BKUs.LOCAL) {
				params.setSignatureDevice(Constants.SIGNATURE_DEVICE_BKU);
			} else if (parameter.getSignatureDevice() == BKUs.MOBILE) {
				params.setSignatureDevice(Constants.SIGNATURE_DEVICE_MOBILE);
				// params.setSignatureDevice(Constants.SIGNATURE_DEVICE_MOBILETEST);
			}
			params.setSignatureType(Constants.SIGNATURE_TYPE_BINARY);

			params.setSignatureProfileId(sign_para.getSignatureProfileID());

			params.setOutput(new ByteArrayDataSink());
			if (parameter.getEmblem() != null) {
				params.setProfileOverrideValue("SIG_LABEL", parameter
						.getEmblem().getFileName());
			}
			
			if(parameter.getProperty("SIG_NOTE") != null) {
				params.setProfileOverrideValue("SIG_NOTE", parameter.getProperty("SIG_NOTE"));
			}

			// Prepare Output sink
			state.setOutput(new ByteArrayDataSink());
			params.setOutput(state.getOutput());

			params.setDocument(sign_para.getPDFASDataSource());

			state.setSignParameters(params);

			PdfAsInternal pdfasInternal = PDFASHelper.getPdfAsInternal();

			// Prepares the document
			SignatureDetailInformation sdi = pdfas.prepareSign(params);

			state.setSignatureDetailInformation(sdi);

			// Retrieve the SL Request
			String slRequest = pdfasInternal.prepareLocalSignRequest(params,
					false, URL_TEMPLATE, sdi);

			at.gv.egiz.pdfas.api.io.DataSource sig_data = sdi
					.getSignatureData();

			PDFASSLRequest request = new PDFASSLRequest(slRequest,
					sig_data.getAsByteArray());

			state.setSignatureRequest(request);

			return state;
		} catch (PDFASSLRequestException e) {
			throw new SignatureException(e);
		} catch (PdfAsException e) {
			throw new SignatureException(e);
		} catch (Throwable t) {
			throw new SignatureException(t);
		}
	}

	@Override
	public SignResult sign(SigningState state) throws SignatureException {
		try {
			PDFASSigningState sstate = null;

			if (PDFASSigningState.class.isInstance(state)) {
				sstate = PDFASSigningState.class.cast(state);
			}

			if (sstate == null) {
				throw new SignatureException("Incorrect SigningState!");
			}

			// Retrieve objects
			PdfAs pdfas = PDFASHelper.getPdfAs();

			PdfAsInternal pdfasInternal = PDFASHelper.getPdfAsInternal();

			SignParameters params = sstate.getSignParameters();

			SignatureDetailInformation sdi = sstate
					.getSignatureDetailInformation();

			SLResponse slResponse = sstate.getSignatureResponse();

			LocalBKUParams bkuParams = new LocalBKUParams(
					slResponse.getServer(), slResponse.getUserAgent(),
					slResponse.getSignaturLayout());

			// Perform signature
			at.gv.egiz.pdfas.api.sign.SignResult signResult = pdfasInternal
					.finishLocalSign(pdfas, params, sdi, bkuParams, false,
							slResponse.getSLRespone());

			// Preparing Result Response
			SignResultImpl result = new SignResultImpl();

			// Set Signer Certificate
			result.setSignerCertificate(signResult.getSignerCertificate());
			at.gv.egiz.pdfas.api.sign.pos.SignaturePosition pdfasPos = signResult
					.getSignaturePosition();

			// Set Signature position
			SignaturePosition pos = new SignaturePosition(pdfasPos.getX(),
					pdfasPos.getY(), pdfasPos.getPage());
			/*
			 * pos.SetAuto(sstate.getPDFAsSignatureParameter()
			 * .getSignaturePosition().useAutoPositioning());
			 */
			result.setSignaturePosition(pos);

			// Set signed Document
			result.setSignedDocument(new ByteArrayDocumentSource(
					((ByteArrayDataSink) sstate.getOutput()).getData()));

			return result;
		} catch (PdfAsException e) {
			throw new SignatureException(e);
		}
	}

	@Override
	public SignatureParameter newParameter() {
		return new PdfAsSignatureParameter();
	}
}
