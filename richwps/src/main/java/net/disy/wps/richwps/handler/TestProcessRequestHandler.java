package net.disy.wps.richwps.handler;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import net.disy.wps.richwps.request.TestProcessRequest;
import net.disy.wps.richwps.response.IRichWPSResponse;
import net.disy.wps.richwps.response.TestProcessResponse;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.handler.RequestExecutor;
import org.n52.wps.server.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * A TestProcessRequestHandler provides functionality for handling a request
 * containing information about a Test of a specified process.
 * 
 * @author faltin
 *
 */
public class TestProcessRequestHandler implements IRequestHandler {
	private static Logger LOGGER = LoggerFactory.getLogger(TestProcessRequestHandler.class);
	private TestProcessRequest testProcessRequest;
	protected static RequestExecutor requestExecutor = new RequestExecutor();

	public TestProcessRequestHandler(Document doc) throws ExceptionReport,
			ParserConfigurationException, TransformerFactoryConfigurationError,
			TransformerException, SAXException, IOException {
		this.testProcessRequest = new TestProcessRequest(doc);
	}

	@Override
	public IRichWPSResponse handle() throws ExceptionReport {
		testProcessRequest.updateStatusAccepted();
		IRichWPSResponse response;
		if (testProcessRequest.isStoreResponse()) {
			response = handleAsynchronous();
		} else {
			response = handleSynchronous();
		}
		return response;
	}

	private IRichWPSResponse handleSynchronous() throws ExceptionReport {
		Response response = null;
		ExceptionReport exceptionReport = null;
		try {
			response = requestExecutor.submit(testProcessRequest).get();
		} catch (ExecutionException ee) {
			LOGGER.warn("exception while handling TestProcessRequest.");
			if (ee.getCause() instanceof ExceptionReport) {
				exceptionReport = (ExceptionReport) ee.getCause();
			} else {
				exceptionReport = new ExceptionReport("An error occurred in the computation: "
						+ ee.getMessage(), ExceptionReport.NO_APPLICABLE_CODE);
			}
		} catch (InterruptedException ie) {
			LOGGER.warn("interrupted while handling TestProcessRequest.");
			exceptionReport = new ExceptionReport(
					"The computation in the process was interrupted.",
					ExceptionReport.NO_APPLICABLE_CODE);
		} catch (RejectedExecutionException ree) {
			LOGGER.warn("exception handling TestProcessRequest.", ree);
			exceptionReport = new ExceptionReport(
					"The requested process was rejected. Maybe the server is flooded with requests.",
					ExceptionReport.SERVER_BUSY);
		} catch (Exception e) {
			LOGGER.error("exception handling ProfileProcessRequest.", e);
			if (e instanceof ExceptionReport) {
				exceptionReport = (ExceptionReport) e;
			}
			exceptionReport = new ExceptionReport("Could not read from response stream.",
					ExceptionReport.NO_APPLICABLE_CODE);
		} finally {
			if (exceptionReport != null) {
				LOGGER.debug("ExceptionReport not null: " + exceptionReport.getMessage());
				throw exceptionReport;
			} else if (response == null) {
				LOGGER.warn("null response handling TestProcessRequest.");
				throw new ExceptionReport("Problem with handling threads in RequestHandler",
						ExceptionReport.NO_APPLICABLE_CODE);
			}
		}
		return (IRichWPSResponse) response;
	}

	private IRichWPSResponse handleAsynchronous() throws ExceptionReport {
		requestExecutor.submit(testProcessRequest);
		return new TestProcessResponse(testProcessRequest);
	}
}
