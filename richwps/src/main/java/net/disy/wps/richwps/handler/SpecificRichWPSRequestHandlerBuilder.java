package net.disy.wps.richwps.handler;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import net.disy.wps.richwps.service.RichWebProcessingService;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.handler.IHandler;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A SpecificRichWPSRequestHandlerBuilder constructs a specific RichWPS request
 * handler. Depending on the request type it creates an appropriate
 * RequestHandler.
 * 
 * @author faltin
 *
 */
public class SpecificRichWPSRequestHandlerBuilder {
	private String requestType;
	private Document document;
	private IRequestHandler handler;

	private SpecificRichWPSRequestHandlerBuilder() {
	};

	/**
	 * Factorymethod for a SpecificRichWPSRequestHandlerBuilder.
	 * 
	 * @return a new SpecificRichWPSRequestHandlerBuilder
	 */
	public static SpecificRichWPSRequestHandlerBuilder newInstance() {
		return new SpecificRichWPSRequestHandlerBuilder();
	}

	/**
	 * * Returns the constructed RequestHandler. If any part of the handler was
	 * changed a new handler is created and returned.
	 * 
	 * @return the constructed RequestHandler
	 * @throws ExceptionReport
	 * @throws ParserConfigurationException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 * @throws SAXException
	 * @throws IOException
	 */
	public IHandler getResult() throws ExceptionReport, ParserConfigurationException,
			TransformerFactoryConfigurationError, TransformerException, SAXException, IOException {
		if (handler == null) {
			if (requestType == null) {
				throw new ExceptionReport("Request not valid",
						ExceptionReport.OPERATION_NOT_SUPPORTED);
			} else if (requestType.equals(RichWebProcessingService.TESTPROCESS_REQUEST)) {
				handler = new TestProcessRequestHandler(document);
			} else if (requestType.equals(RichWebProcessingService.PROFILEPROCESS_REQUEST)) {
				handler = new ProfileProcessRequestHandler(document);
			} else if (requestType.equals(RichWebProcessingService.GETSUPPORTEDTYPES_REQUEST)) {
				handler = new GetSupportedTypesRequestHandler(document);
			} else {
				throw new ExceptionReport("Request type unknown (" + requestType
						+ ") Must be TestProcess, ProfileProcess or GetSupportedTypes",
						ExceptionReport.OPERATION_NOT_SUPPORTED);
			}
		}
		return handler;
	}

	/**
	 * Sets the requestType which is used to identify the appropriate handler.
	 * 
	 * @param requestType
	 */
	public void setRequestType(String requestType) {
		if (this.requestType != null) {
			if (!this.requestType.equals(requestType)) {
				handler = null;
			}
		}
		this.requestType = requestType;
	}

	/**
	 * Sets the document used by creation of the specific handler
	 * 
	 * @param doc
	 */
	public void setDocument(Document doc) {
		if (this.document != null) {
			if (!this.document.equals(doc)) {
				handler = null;
			}
		}
		this.document = doc;
	}

}
