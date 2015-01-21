package org.n52.wps.server.response;

import java.io.InputStream;

import org.n52.wps.server.ExceptionReport;

public interface IResponse {
	InputStream getAsStream() throws ExceptionReport;
}
