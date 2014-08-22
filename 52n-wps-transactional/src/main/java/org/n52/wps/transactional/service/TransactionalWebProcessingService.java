package org.n52.wps.transactional.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.n52.wps.server.ExceptionReport;
import org.n52.wps.transactional.handler.TransactionalExceptionHandler;
import org.n52.wps.transactional.handler.TransactionalRequestHandler;
import org.n52.wps.transactional.response.TransactionalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

public class TransactionalWebProcessingService extends HttpServlet{
	private static Logger LOGGER = LoggerFactory.getLogger(TransactionalWebProcessingService.class);
	
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
		LOGGER.info("Inbound HTTP-POST DeployProcess Request. " + new Date());
		TransactionalResponse response = null;
		try {
			InputStream is = req.getInputStream();
			if (req.getParameterMap().containsKey("request")){
				is = new ByteArrayInputStream(req.getParameter("request").getBytes("UTF-8"));
			}

//			 WORKAROUND	cut the parameter name "request" of the stream		
			BufferedReader br=new BufferedReader(new InputStreamReader(is,"UTF-8"));
    	    StringWriter writer=new StringWriter();
    	    int k;
    	    while((k=br.read())!=-1){
    	    	writer.write(k);
    	    }
    	    LOGGER.debug(writer.toString());
    	    String documentString;
    	    String reqContentType = req.getContentType();
    	    if (writer.toString().startsWith("request=")){
    	    	if(reqContentType.equalsIgnoreCase("text/plain")) {
    	    		documentString = writer.toString().substring(8);
    	    	}
    	    	else {
    	    		documentString = URLDecoder.decode(writer.toString().substring(8), "UTF-8");
    	    	}
    	    	LOGGER.debug(documentString);
    	    } else{
    	    	documentString = writer.toString();
    	    }
    	   
			TransactionalRequestHandler handler = new TransactionalRequestHandler(new ByteArrayInputStream(documentString.getBytes("UTF-8")), res.getOutputStream());
			response = handler.handle();
		} 
		catch (ExceptionReport exception) {
			TransactionalExceptionHandler.handleException(res,
					exception);
		} catch (Throwable t) {
			TransactionalExceptionHandler.handleException(res,
					new ExceptionReport("Unexpected error",
							ExceptionReport.NO_APPLICABLE_CODE));
		}
	}
	
	private String nodeToString(Node node) throws TransformerFactoryConfigurationError, TransformerException {
		StringWriter stringWriter = new StringWriter();
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		transformer.transform(new DOMSource(node), new StreamResult(
				stringWriter));

		return stringWriter.toString();
	}
}
