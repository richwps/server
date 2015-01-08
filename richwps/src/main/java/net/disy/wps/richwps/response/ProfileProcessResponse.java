package net.disy.wps.richwps.response;

import java.io.InputStream;

import net.disy.wps.richwps.request.IRichWPSRequest;
import net.disy.wps.richwps.request.ProfileProcessRequest;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.response.Response;

/**
 * This implementation represents the Response on a ProfileProcessRequest.
 * 
 * @author faltin
 *
 */
public class ProfileProcessResponse extends Response implements
		IRichWPSResponse {
	private IRichWPSRequest request;
	private ProfileProcessResponseBuilder builder;

	/**
	 * Constructs a new ProfileProcessResponse
	 * 
	 * @param request
	 *            the ProfileProcessRequest
	 * @throws ExceptionReport
	 */
	public ProfileProcessResponse(ProfileProcessRequest request)
			throws ExceptionReport {
		super(request);
		this.request = (IRichWPSRequest) request;
		this.builder = ((ProfileProcessRequest) this.request)
				.getProfileProcessResponseBuilder();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.wps.server.response.Response#getAsStream()
	 */
	@Override
	public InputStream getAsStream() throws ExceptionReport {
		return this.builder.getAsStream();
	}

	/**
	 * Returns the ProfileProcessResponseBuilder
	 * 
	 * @return the ProfileProcessResponseBuilder
	 */
	public ProfileProcessResponseBuilder getProfileProcessResponseBuilder() {
		return builder;
	}
}
