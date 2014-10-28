package net.disy.wps.richwps.request;

import net.disy.wps.richwps.response.GetSupportedTypesResponseBuilder;
import net.opengis.wps.x100.GetSupportedTypesDocument;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.transactional.request.DeployProcessRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class GetSupportedTypesRequest implements IRichWPSRequest {

	private static Logger LOGGER = LoggerFactory.getLogger(DeployProcessRequest.class);
	
	private GetSupportedTypesDocument supportedTypesDoc;
	private boolean complexTypesOnly;
	
	private GetSupportedTypesResponseBuilder responseBuilder;
	
	public GetSupportedTypesRequest(Document doc) throws ExceptionReport{
		
		responseBuilder = new GetSupportedTypesResponseBuilder(this);
		
		try {
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			this.supportedTypesDoc = GetSupportedTypesDocument.Factory.parse(doc,option);
			if (supportedTypesDoc == null) {
				LOGGER.error("GetSupportedTypesDocument is null");
				throw new ExceptionReport("Error while parsing post data",
						ExceptionReport.MISSING_PARAMETER_VALUE);
			}
		}
		catch (XmlException e){
			throw new ExceptionReport("Error while parsing post data",
					ExceptionReport.MISSING_PARAMETER_VALUE, e);
		}
		
		this.complexTypesOnly = supportedTypesDoc.getGetSupportedTypes().getComplexTypesOnly();

		if (this.complexTypesOnly) {
			LOGGER.info("GetSupportedTypes for complex types only.");
		}
		else {
			LOGGER.info("GetSupportedTypes for all available types.");
		}
		
		responseBuilder.updateSupportedTypes();
	}
	
	public boolean getComplexTypesOnly() {
		return this.getComplexTypesOnly();
	}
	
	public GetSupportedTypesResponseBuilder getSupportedTypesResponseBuilder() {
		return this.responseBuilder;
	}
}
