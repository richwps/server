package net.disy.wps.richwps.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import net.disy.wps.richwps.response.IRichWPSResponse;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.handler.IHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * This implementation handles requests containing demands for installing,
 * deinstalling, testing and profiling of WPS-Processes.
 * 
 * <p>
 * These WPS-Processes can be composed of several WPS-Processes orchestrated by
 * a domain specific language.
 * </p>
 * 
 * @author woessner
 * @author faltin
 *
 */
public class RichWPSRequestHandler implements IHandler {

	private static Logger LOGGER = LoggerFactory.getLogger(RichWPSRequestHandler.class);
	private IHandler specificRichWPSRequestHandler;

	/**
	 * 
	 * @param is
	 *            InputStream containing information of transferred document.
	 * @param requestType
	 *            the type of the request
	 * @throws ExceptionReport
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	public RichWPSRequestHandler(InputStream is, String requestType) throws ExceptionReport,
			TransformerFactoryConfigurationError, TransformerException {

		try {
			DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
			fac.setNamespaceAware(true);
			fac.setIgnoringElementContentWhitespace(true);
			DocumentBuilder documentBuilder = fac.newDocumentBuilder();
			Document doc = documentBuilder.parse(is);
			Node child = doc.getFirstChild();

			while (child.getNodeName().compareTo("#comment") == 0) {
				child = child.getNextSibling();
			}

			LOGGER.info("Request type: " + requestType);
			SpecificRichWPSRequestHandlerBuilder handlerBuilder = SpecificRichWPSRequestHandlerBuilder
					.newInstance();
			handlerBuilder.setRequestType(requestType);
			handlerBuilder.setDocument(doc);
			specificRichWPSRequestHandler = handlerBuilder.getResult();

		} catch (SAXException e) {
			throw new ExceptionReport("There went something wrong with parsing the POST data: "
					+ e.getMessage(), ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (IOException e) {
			throw new ExceptionReport("There went something wrong with the network connection.",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (ParserConfigurationException e) {
			throw new ExceptionReport("There is a internal parser configuration error",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		}

	}

	public IRichWPSResponse handle() throws ExceptionReport {
		return (IRichWPSResponse) specificRichWPSRequestHandler.handle();
	}

	public static IHandler newInstance(ByteArrayInputStream is, String requestType)
			throws ExceptionReport, TransformerFactoryConfigurationError, TransformerException {
		return new RichWPSRequestHandler(is, requestType);
	}

}
