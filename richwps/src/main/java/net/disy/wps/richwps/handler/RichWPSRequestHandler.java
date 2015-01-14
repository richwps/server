package net.disy.wps.richwps.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import net.disy.wps.richwps.request.GetSupportedTypesRequest;
import net.disy.wps.richwps.request.IRichWPSRequest;
import net.disy.wps.richwps.request.ProfileProcessRequest;
import net.disy.wps.richwps.request.TestProcessRequest;
import net.disy.wps.richwps.response.GetSupportedTypesResponse;
import net.disy.wps.richwps.response.IRichWPSResponse;
import net.disy.wps.richwps.response.ProfileProcessResponse;
import net.disy.wps.richwps.response.TestProcessResponse;
import net.disy.wps.richwps.service.RichWebProcessingService;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.handler.RequestExecutor;
import org.n52.wps.server.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * This implementation handles requests which can contain demands for
 * installing, deinstalling, testing and profiling of WPS-Processes.
 * 
 * <p>
 * These WPS-Processes can be composed of several WPS-Processes orchestrated by
 * the domain specific language rola.
 * </p>
 * 
 * @author woessner
 * @author faltin
 *
 */
public class RichWPSRequestHandler {

	private static Logger LOGGER = LoggerFactory
			.getLogger(RichWPSRequestHandler.class);
	protected OutputStream os;
	protected IRichWPSRequest req;
	/** Computation timeout in seconds */
	protected static RequestExecutor pool = new RequestExecutor();

	/**
	 * 
	 * @param is
	 *            InputStream containing information of transferred document.
	 * @param os
	 *            OutputStream for returning response.
	 * @throws ExceptionReport
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	public RichWPSRequestHandler(InputStream is, OutputStream os)
			throws ExceptionReport, TransformerFactoryConfigurationError,
			TransformerException {
		Document doc;
		this.os = os;

		try {
			DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
			fac.setNamespaceAware(true);// this prevents "xmlns="""
			fac.setIgnoringElementContentWhitespace(true);

			DocumentBuilder documentBuilder = fac.newDocumentBuilder();
			doc = documentBuilder.parse(is);

			Node child = doc.getFirstChild();

			while (child.getNodeName().compareTo("#comment") == 0) {
				child = child.getNextSibling();
			}

			String requestType = RichWebProcessingService.getRequestType(doc
					.getFirstChild());

			LOGGER.info("Request type: " + requestType);

			if (requestType == null) {
				throw new ExceptionReport("Request not valid",
						ExceptionReport.OPERATION_NOT_SUPPORTED);
			} else if (requestType
					.equals(RichWebProcessingService.TESTPROCESS_REQUEST)) {
				this.req = new TestProcessRequest(doc);
			} else if (requestType
					.equals(RichWebProcessingService.PROFILEPROCESS_REQUEST)) {
				this.req = new ProfileProcessRequest(doc);
			} else if (requestType
					.equals(RichWebProcessingService.GETSUPPORTEDTYPES_REQUEST)) {
				this.req = new GetSupportedTypesRequest(doc);
			} else {
				throw new ExceptionReport("Request type unknown ("
						+ requestType
						+ ") Must be DeployProcess or UnDeployProcess",
						ExceptionReport.OPERATION_NOT_SUPPORTED);
			}

		} catch (SAXException e) {
			throw new ExceptionReport(
					"There went something wrong with parsing the POST data: "
							+ e.getMessage(),
					ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (IOException e) {
			throw new ExceptionReport(
					"There went something wrong with the network connection.",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (ParserConfigurationException e) {
			throw new ExceptionReport(
					"There is a internal parser configuration error",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		}

	}

	public IRichWPSResponse handle() throws ExceptionReport {
		if (this.req == null)
			throw new ExceptionReport("Internal Error", "");
		if (req instanceof TestProcessRequest) {
			return handleTest((TestProcessRequest) req);
		} else if (req instanceof ProfileProcessRequest) {
			return handleProfile((ProfileProcessRequest) req);
		} else if (req instanceof GetSupportedTypesRequest) {
			return new GetSupportedTypesResponse((GetSupportedTypesRequest) req);
		} else {
			throw new ExceptionReport("Error. Could not handle request",
					ExceptionReport.OPERATION_NOT_SUPPORTED);
		}
	}

	/**
	 * Handles a TestProcessRequest.
	 * 
	 * @param testProcessRequest
	 *            the TestProcessRequest
	 * @return the response on the request
	 * @throws ExceptionReport
	 */
	private IRichWPSResponse handleTest(TestProcessRequest testProcessRequest)
			throws ExceptionReport {

		Response response = null;
		testProcessRequest.updateStatusAccepted();

		ExceptionReport exceptionReport = null;
		try {
			if (testProcessRequest.isStoreResponse()) {
				pool.submit(testProcessRequest);
				return new TestProcessResponse(testProcessRequest);
			}
			try {
				// retrieve status with timeout enabled
				try {
					response = pool.submit(testProcessRequest).get();
				} catch (ExecutionException ee) {
					LOGGER.warn("exception while handling TestProcessRequest.");
					// the computation threw an error
					// probably the client input is not valid
					if (ee.getCause() instanceof ExceptionReport) {
						exceptionReport = (ExceptionReport) ee.getCause();
					} else {
						exceptionReport = new ExceptionReport(
								"An error occurred in the computation: "
										+ ee.getMessage(),
								ExceptionReport.NO_APPLICABLE_CODE);
					}
				} catch (InterruptedException ie) {
					LOGGER.warn("interrupted while handling TestProcessRequest.");
					// interrupted while waiting in the queue
					exceptionReport = new ExceptionReport(
							"The computation in the process was interrupted.",
							ExceptionReport.NO_APPLICABLE_CODE);
				}
			} finally {
				if (exceptionReport != null) {
					LOGGER.debug("ExceptionReport not null: "
							+ exceptionReport.getMessage());
					// NOT SURE, if this exceptionReport is also written to
					// the DB, if required... test please!
					throw exceptionReport;
				}
				// send the result to the outputstream of the client.
				/*
				 * if(((ExecuteRequest) req).isQuickStatus()) { resp = new
				 * ExecuteResponse(execReq); }
				 */
				else if (response == null) {
					LOGGER.warn("null response handling TestProcessRequest.");
					throw new ExceptionReport(
							"Problem with handling threads in RequestHandler",
							ExceptionReport.NO_APPLICABLE_CODE);
				}
			}
		} catch (RejectedExecutionException ree) {
			LOGGER.warn("exception handling TestProcessRequest.", ree);
			// server too busy?
			throw new ExceptionReport(
					"The requested process was rejected. Maybe the server is flooded with requests.",
					ExceptionReport.SERVER_BUSY);
		} catch (Exception e) {
			LOGGER.error("exception handling TestProcessRequest.", e);
			if (e instanceof ExceptionReport) {
				throw (ExceptionReport) e;
			}
			throw new ExceptionReport("Could not read from response stream.",
					ExceptionReport.NO_APPLICABLE_CODE);
		}
		return new TestProcessResponse(testProcessRequest);
	}

	/**
	 * Handles a ProfileProcessRequest.
	 * 
	 * @param profileProcessRequest
	 *            the request
	 * @return the response on the request
	 * @throws ExceptionReport
	 */
	private IRichWPSResponse handleProfile(
			ProfileProcessRequest profileProcessRequest) throws ExceptionReport {

		Response response = null;
		profileProcessRequest.updateStatusAccepted();

		ExceptionReport exceptionReport = null;
		try {
			if (profileProcessRequest.isStoreResponse()) {
				pool.submit(profileProcessRequest);
				return new ProfileProcessResponse(profileProcessRequest);
			}
			try {
				// retrieve status with timeout enabled
				try {
					response = pool.submit(profileProcessRequest).get();
				} catch (ExecutionException ee) {
					LOGGER.warn("exception while handling ProfileProcessRequest.");
					// the computation threw an error
					// probably the client input is not valid
					if (ee.getCause() instanceof ExceptionReport) {
						exceptionReport = (ExceptionReport) ee.getCause();
					} else {
						exceptionReport = new ExceptionReport(
								"An error occurred in the computation: "
										+ ee.getMessage(),
								ExceptionReport.NO_APPLICABLE_CODE);
					}
				} catch (InterruptedException ie) {
					LOGGER.warn("interrupted while handling ProfileProcessRequest.");
					// interrupted while waiting in the queue
					exceptionReport = new ExceptionReport(
							"The computation in the process was interrupted.",
							ExceptionReport.NO_APPLICABLE_CODE);
				}
			} finally {
				if (exceptionReport != null) {
					LOGGER.debug("ExceptionReport not null: "
							+ exceptionReport.getMessage());
					// NOT SURE, if this exceptionReport is also written to
					// the DB, if required... test please!
					throw exceptionReport;
				}
				// send the result to the outputstream of the client.
				/*
				 * if(((ExecuteRequest) req).isQuickStatus()) { resp = new
				 * ExecuteResponse(execReq); }
				 */
				else if (response == null) {
					LOGGER.warn("null response handling ProfileProcessRequest.");
					throw new ExceptionReport(
							"Problem with handling threads in RequestHandler",
							ExceptionReport.NO_APPLICABLE_CODE);
				}
			}
		} catch (RejectedExecutionException ree) {
			LOGGER.warn("exception handling ProfileProcessRequest.", ree);
			// server too busy?
			throw new ExceptionReport(
					"The requested process was rejected. Maybe the server is flooded with requests.",
					ExceptionReport.SERVER_BUSY);
		} catch (Exception e) {
			LOGGER.error("exception handling ProfileProcessRequest.", e);
			if (e instanceof ExceptionReport) {
				throw (ExceptionReport) e;
			}
			throw new ExceptionReport("Could not read from response stream.",
					ExceptionReport.NO_APPLICABLE_CODE);
		}
		return new ProfileProcessResponse(profileProcessRequest);
	}

}
