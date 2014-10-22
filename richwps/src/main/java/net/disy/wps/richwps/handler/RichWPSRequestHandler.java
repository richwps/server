package net.disy.wps.richwps.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.disy.wps.richwps.request.IRichWPSRequest;
import net.disy.wps.richwps.response.IRichWPSResponse;
import net.disy.wps.richwps.service.RichWebProcessingService;

import org.n52.wps.server.ExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class RichWPSRequestHandler {

	private static Logger LOGGER = LoggerFactory.getLogger(RichWPSRequestHandler.class);
	protected OutputStream os;
	protected IRichWPSRequest req;
	
	public RichWPSRequestHandler(InputStream is, OutputStream os) throws ExceptionReport {

		Document doc;
		this.os = os;
		
		try {
		DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
		fac.setNamespaceAware(true);//this prevents "xmlns="""
		fac.setIgnoringElementContentWhitespace(true);
		
		DocumentBuilder documentBuilder= fac.newDocumentBuilder();
		doc = documentBuilder.parse(is);
				
		Node child = doc.getFirstChild();
		
		while(child.getNodeName().compareTo("#comment")==0) {
			child = child.getNextSibling();
		}

		String requestType = RichWebProcessingService.getRequestType(doc.getFirstChild());

		LOGGER.info("Request type: " + requestType);
		
		if (requestType == null) {
			throw new ExceptionReport("Request not valid",
					ExceptionReport.OPERATION_NOT_SUPPORTED);
		} else if (requestType.equals(RichWebProcessingService.TESTPROCESS_REQUEST)) {
			//this.req = new DeployProcessRequest(doc);
		} else if (requestType.equals(RichWebProcessingService.PROFILEPROCESS_REQUEST)) {
			//this.req = new UndeployProcessRequest(doc);
		} else {
			throw new ExceptionReport("Request type unknown ("
					+ requestType
					+ ") Must be DeployProcess or UnDeployProcess",
					ExceptionReport.OPERATION_NOT_SUPPORTED);
		}

		}
		catch (SAXException e) {
			throw new ExceptionReport(
					"There went something wrong with parsing the POST data: "
							+ e.getMessage(),
					ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (IOException e) {
			throw new ExceptionReport(
					"There went something wrong with the network connection.",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (ParserConfigurationException e) {
			throw new ExceptionReport(
					"There is a internal parser configuration error",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		}
		
	}
	
	public IRichWPSResponse handle() {
		// TODO
		return null;
	}
}
