package net.disy.wps.richwps.response;

import java.io.InputStream;

import net.disy.wps.richwps.request.GetSupportedTypesRequest;
import net.disy.wps.richwps.request.IRichWPSRequest;

import org.n52.wps.server.ExceptionReport;

public class GetSupportedTypesResponse implements IRichWPSResponse {

	private IRichWPSRequest request;
	private GetSupportedTypesResponseBuilder builder;
	
	public GetSupportedTypesResponse(IRichWPSRequest request) {
		this.request = request;
		this.builder = ((GetSupportedTypesRequest) this.request).getSupportedTypesResponseBuilder();
	}

	public InputStream getAsStream() throws ExceptionReport {
		return this.builder.getAsStream();
	}

}
