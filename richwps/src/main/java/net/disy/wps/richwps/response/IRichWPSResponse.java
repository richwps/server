package net.disy.wps.richwps.response;

import java.io.InputStream;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.response.IResponse;

/**
 * 
 * @author woessner
 *
 */
public interface IRichWPSResponse extends IResponse {

	public abstract InputStream getAsStream() throws ExceptionReport;

}