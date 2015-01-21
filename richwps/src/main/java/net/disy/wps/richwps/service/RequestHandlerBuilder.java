package net.disy.wps.richwps.service;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import net.disy.wps.richwps.handler.RichWPSRequestHandler;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.handler.IHandler;
import org.n52.wps.transactional.handler.TransactionalRequestHandler;

/**
 * A RequestHandlerBuilder constructs a specific request handler depending on
 * the request type. It creates RichWPSRequestHandler in case of TestProcess-,
 * ProfilingProcess- and GetSupportedTypesRequest. In case of Deploy- or
 * UndeployprocessRequest it creates a TransactionalRequestHandler.
 * 
 * @author faltin
 *
 */
public class RequestHandlerBuilder {
	private String requestType;
	private String documentString;
	private IHandler handler;

	/**
	 * Returns the constructed RequestHandlerBuilder. If any part of the handler
	 * was changed a new handler is created and returned.
	 * 
	 * @return the constructed ReqeustHandlerBuilder
	 * @throws UnsupportedEncodingException
	 * @throws ExceptionReport
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	public IHandler getResult() throws UnsupportedEncodingException, ExceptionReport,
			TransformerFactoryConfigurationError, TransformerException {
		if (handler == null) {
			if (requestType == RichWebProcessingService.DEPLOYPROCESS_REQUEST
					|| requestType == RichWebProcessingService.UNDEPLOYPROCESS_REQUEST) {
				handler = TransactionalRequestHandler.newInstance(new ByteArrayInputStream(
						documentString.getBytes("UTF-8")), null);
			} else {
				handler = RichWPSRequestHandler.newInstance(
						new ByteArrayInputStream(documentString.getBytes("UTF-8")), requestType);
			}
		}
		return handler;
	}

	/**
	 * Factorymethod for a RequestHandlerBuilder
	 * 
	 * @return a new RequestHandlerBuilder
	 */
	public static RequestHandlerBuilder newInstance() {
		return new RequestHandlerBuilder();
	}

	private RequestHandlerBuilder() {
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
	 * @param documentString
	 */
	public void setDocumentString(String documentString) {
		if (this.documentString != null) {
			if (!this.documentString.equals(documentString)) {
				handler = null;
			}
		}
		this.documentString = documentString;
	}
}
