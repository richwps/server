package org.n52.wps.transactional.response;

import java.io.InputStream;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.transactional.request.ITransactionalRequest;
import org.n52.wps.transactional.request.UndeployProcessRequest;

public class UndeployProcessResponse implements ITransactionalResponse {

	private ITransactionalRequest request;
	private UndeployProcessResponseBuilder builder;
	
	public UndeployProcessResponse(ITransactionalRequest request) {
		this.request = request;
		this.builder = ((UndeployProcessRequest) this.request).getUndeployResponseBuilder();
	}

	public InputStream getAsStream() throws ExceptionReport {
		return this.builder.getAsStream();
	}

}
