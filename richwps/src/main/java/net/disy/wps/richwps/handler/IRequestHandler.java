package net.disy.wps.richwps.handler;

import net.disy.wps.richwps.response.IRichWPSResponse;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.handler.IHandler;

/**
 * Represents an RichWPS request Handler.
 * 
 * <p>
 * All known implementing classes: TestProcessRequestHandler,
 * ProfileProcessRequestHandler, GetSupportedTypesRequestHandler. <\p>
 * 
 * @author faltin
 *
 */
public interface IRequestHandler extends IHandler {

	IRichWPSResponse handle() throws ExceptionReport;

}
