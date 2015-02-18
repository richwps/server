package net.disy.wps.richwps.handler;

import net.disy.wps.richwps.request.GetSupportedTypesRequest;
import net.disy.wps.richwps.response.GetSupportedTypesResponse;
import net.disy.wps.richwps.response.IRichWPSResponse;

import org.n52.wps.server.ExceptionReport;
import org.w3c.dom.Document;

public class GetSupportedTypesRequestHandler implements IRequestHandler {

	GetSupportedTypesRequest request;
	
	public GetSupportedTypesRequestHandler(Document doc) throws ExceptionReport {
		request = new GetSupportedTypesRequest(doc);
	}

	@Override
	public IRichWPSResponse handle() {
		return new GetSupportedTypesResponse(request);
	}

}
