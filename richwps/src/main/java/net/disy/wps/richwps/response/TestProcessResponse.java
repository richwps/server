package net.disy.wps.richwps.response;

import java.io.InputStream;

import net.disy.wps.richwps.request.IRichWPSRequest;
import net.disy.wps.richwps.request.TestProcessRequest;

import org.n52.wps.server.ExceptionReport;

/**
 * 
 * @author faltin
 *
 */
public class TestProcessResponse implements IRichWPSResponse {
	private IRichWPSRequest request;
	private TestProcessResponseBuilder builder;

	public TestProcessResponse(TestProcessRequest request)
			throws ExceptionReport {
		this.request = (IRichWPSRequest) request;
		this.builder = ((TestProcessRequest) this.request)
				.getTestProcessResponseBuilder();
		// TODO run process
		this.builder.update();
	}

	@Override
	public InputStream getAsStream() throws ExceptionReport {
		return this.builder.getAsStream();
	}
}
