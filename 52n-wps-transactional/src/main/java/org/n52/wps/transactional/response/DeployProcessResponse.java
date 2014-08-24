package org.n52.wps.transactional.response;

import java.io.InputStream;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.transactional.request.DeployProcessRequest;
import org.n52.wps.transactional.request.ITransactionalRequest;


public class DeployProcessResponse implements ITransactionalResponse {

	private ITransactionalRequest request;
	private DeployProcessResponseBuilder builder;
	
	public DeployProcessResponse(ITransactionalRequest request) {
		this.request = request;
		this.builder = ((DeployProcessRequest) this.request).getDeployResponseBuilder();
	}

	public InputStream getAsStream() throws ExceptionReport {
		return this.builder.getAsStream();
	}

}
