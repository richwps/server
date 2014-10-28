package net.disy.wps.richwps.response;

import java.io.InputStream;

import org.n52.wps.server.ExceptionReport;

public interface IRichWPSResponseBuilder {

	public InputStream getAsStream() throws ExceptionReport;
	
}
