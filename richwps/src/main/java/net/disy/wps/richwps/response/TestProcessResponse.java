package net.disy.wps.richwps.response;

import java.io.InputStream;

import net.disy.wps.richwps.request.IRichWPSRequest;
import net.disy.wps.richwps.request.TestProcessRequest;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.response.Response;

/**
 * This implementation represents the Response on a TestProcessRequest.
 * 
 * @author faltin
 *
 */
public class TestProcessResponse extends Response implements IRichWPSResponse {
	private IRichWPSRequest request;
	private TestProcessResponseBuilder builder;

	/**
	 * Constructs a new TestProcessResponse.
	 * 
	 * @param request
	 *            the TestProcessRequest
	 * @throws ExceptionReport
	 */
	public TestProcessResponse(TestProcessRequest request)
			throws ExceptionReport {
		super(request);
		this.request = (IRichWPSRequest) request;
		this.builder = ((TestProcessRequest) this.request)
				.getTestProcessResponseBuilder();
	}

	@Override
	public InputStream getAsStream() throws ExceptionReport {
		return this.builder.getAsStream();
	}

	/**
	 * Returns the TestProcessResponseBuilder
	 * 
	 * @return the TestProcessResponseBuilder
	 */
	public TestProcessResponseBuilder getTestProcessResponseBuilder() {
		return builder;
	}
}
