package net.disy.wps.richwps.service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URLDecoder;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.disy.wps.richwps.handler.RichWPSRequestHandler;
import net.disy.wps.richwps.response.IRichWPSResponse;

import org.apache.commons.io.IOUtils;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.transactional.handler.TransactionalRequestHandler;
import org.n52.wps.transactional.response.ITransactionalResponse;
import org.n52.wps.util.XMLBeansHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *  
 * @author woessner
 *
 */
public class RichWebProcessingService extends HttpServlet{
	
	public static String SERVLET_PATH = "WebProcessingService";

	private static final String XML_CONTENT_TYPE = "text/xml";
	
	public static final String DEPLOYPROCESS_REQUEST = "DeployProcess";
	public static final String UNDEPLOYPROCESS_REQUEST = "UndeployProcess";
	public static final String TESTPROCESS_REQUEST = "TestProcess";
	public static final String PROFILEPROCESS_REQUEST = "ProfileProcess";
	public static final String GETSUPPORTEDTYPES_REQUEST = "GetSupportedTypes";

	private static final long serialVersionUID = 1L;
	private static Logger LOGGER = LoggerFactory.getLogger(RichWebProcessingService.class);
	
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		
		LOGGER.info("Inbound HTTP-POST RichWP Request. " + new Date());
		Document doc;
		
		InputStream resIs = null;
		
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

    	    DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
    		fac.setNamespaceAware(true);//this prevents "xmlns="""
    		fac.setIgnoringElementContentWhitespace(true);
    		
    		DocumentBuilder documentBuilder= fac.newDocumentBuilder();
    		doc = documentBuilder.parse(new ByteArrayInputStream(documentString.getBytes("UTF-8")));
    				
    		Node child = doc.getFirstChild();
    		
    		while(child.getNodeName().compareTo("#comment")==0) {
    			child = child.getNextSibling();
    		}

    		// determine requestType and switch to WPS-T or RichWPS handler
    		String requestType = getRequestType(doc.getFirstChild());

			if (requestType == RichWebProcessingService.DEPLOYPROCESS_REQUEST || requestType == RichWebProcessingService.UNDEPLOYPROCESS_REQUEST) {
				TransactionalRequestHandler handler = new TransactionalRequestHandler(new ByteArrayInputStream(documentString.getBytes("UTF-8")), res.getOutputStream());
				
				ITransactionalResponse response = handler.handle();
				resIs = response.getAsStream();
			}
			else {
				RichWPSRequestHandler handler = new RichWPSRequestHandler(new ByteArrayInputStream(documentString.getBytes("UTF-8")), res.getOutputStream());
				IRichWPSResponse response = handler.handle();
				resIs = response.getAsStream();
			}
			
			OutputStream resOs = res.getOutputStream();
			IOUtils.copy(resIs, resOs);
			is.close();
			res.setStatus(HttpServletResponse.SC_OK);
		}
		catch (SAXException e) {
			System.out.println(e.getMessage());
		}
		catch (ExceptionReport exception) {
			handleException(exception, res);
		} catch (Throwable t) {
			handleException(new ExceptionReport("Unexpected error",
							ExceptionReport.NO_APPLICABLE_CODE), res);
		}
	}
	
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		ExceptionReport er = new ExceptionReport("HTTP GET is not supported at this endpoint", ExceptionReport.NO_APPLICABLE_CODE);                    
        handleException(er, res);
    }
		
	public static String getRequestType(Node node) {
		String localName = node.getLocalName();
		if (localName.equalsIgnoreCase("undeployprocess")) {
			return RichWebProcessingService.UNDEPLOYPROCESS_REQUEST;
		}
		else if (localName.equalsIgnoreCase("deployprocess")) {
				return RichWebProcessingService.DEPLOYPROCESS_REQUEST;
		}
		else if (localName.equalsIgnoreCase("testprocess")) {
			return RichWebProcessingService.TESTPROCESS_REQUEST;
		}
		else if (localName.equalsIgnoreCase("profileprocess")) {
			return RichWebProcessingService.PROFILEPROCESS_REQUEST;
		}
		else if (localName.equalsIgnoreCase("getsupportedtypes")) {
			return RichWebProcessingService.GETSUPPORTEDTYPES_REQUEST;
		}
		else {
			return null;
		}

	}

	private static void handleException(ExceptionReport exception, HttpServletResponse res) {
        res.setContentType(XML_CONTENT_TYPE);
        try {
            LOGGER.debug(exception.toString());
            // DO NOT MIX getWriter and getOuputStream!
            exception.getExceptionDocument().save(res.getOutputStream(),
                                                  XMLBeansHelper.getXmlOptions());

            res.setStatus(HttpServletResponse.SC_OK);
        }
        catch (IOException e) {
            LOGGER.warn("exception occured while writing ExceptionReport to stream");
            try {
                res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                              "error occured, while writing OWS Exception output");
            }
            catch (IOException ex) {
                LOGGER.error("error while writing error code to client!");
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }
}
