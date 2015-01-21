package net.disy.wps.richwps.handler;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import net.disy.wps.richwps.request.ProfileProcessRequest;
import net.disy.wps.richwps.response.IRichWPSResponse;
import net.disy.wps.richwps.response.ProfileProcessResponse;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.handler.RequestExecutor;
import org.n52.wps.server.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class ProfileProcessRequestHandler implements IRequestHandler {
	ProfileProcessRequest profileProcessRequest;
	private static Logger LOGGER = LoggerFactory.getLogger(ProfileProcessRequestHandler.class);
	protected static RequestExecutor requestExecutor = new RequestExecutor();

	public ProfileProcessRequestHandler(Document doc) throws ExceptionReport,
			ParserConfigurationException, TransformerFactoryConfigurationError,
			TransformerException {
		this.profileProcessRequest = new ProfileProcessRequest(doc);
	}

	@Override
	public IRichWPSResponse handle() throws ExceptionReport {
		profileProcessRequest.updateStatusAccepted();
		IRichWPSResponse response;
		if (profileProcessRequest.isStoreResponse()) {
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
			response = requestExecutor.submit(profileProcessRequest).get();
		} catch (ExecutionException ee) {
			LOGGER.warn("exception while handling ProfileProcessRequest.");
			if (ee.getCause() instanceof ExceptionReport) {
				exceptionReport = (ExceptionReport) ee.getCause();
			} else {
				exceptionReport = new ExceptionReport("An error occurred in the computation: "
						+ ee.getMessage(), ExceptionReport.NO_APPLICABLE_CODE);
			}
		} catch (InterruptedException ie) {
			LOGGER.warn("interrupted while handling ProfileProcessRequest.");
			exceptionReport = new ExceptionReport(
					"The computation in the process was interrupted.",
					ExceptionReport.NO_APPLICABLE_CODE);
		} catch (RejectedExecutionException ree) {
			LOGGER.warn("exception handling ProfileProcessRequest.", ree);
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
				LOGGER.warn("null response handling ProfileProcessRequest.");
				throw new ExceptionReport("Problem with handling threads in RequestHandler",
						ExceptionReport.NO_APPLICABLE_CODE);
			}
		}
		return (IRichWPSResponse) response;
	}

	private IRichWPSResponse handleAsynchronous() throws ExceptionReport {
		requestExecutor.submit(profileProcessRequest);
		return new ProfileProcessResponse(profileProcessRequest);
	}

}
