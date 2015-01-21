package org.n52.wps.server.handler;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.response.IResponse;

/**
 * Represents an advanced WPS request Handler.
 * 
 * <p>
 * All known implementing classes: RichWPSRequestHandler,
 * TransactionalRequestHandler. <\p>
 * 
 * @author faltin
 *
 */
public interface IHandler {

	/**
	 * Handles the the task the handler was created for.
	 * 
	 * @return the response on the task.
	 * @throws ExceptionReport
	 */
	IResponse handle() throws ExceptionReport;

}
