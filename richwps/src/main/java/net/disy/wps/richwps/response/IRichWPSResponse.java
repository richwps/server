package net.disy.wps.richwps.response;

import java.io.InputStream;
import org.n52.wps.server.ExceptionReport;

/**
 * 
 * @author woessner
 *
 */
public interface IRichWPSResponse {

	public abstract InputStream getAsStream() throws ExceptionReport;

}