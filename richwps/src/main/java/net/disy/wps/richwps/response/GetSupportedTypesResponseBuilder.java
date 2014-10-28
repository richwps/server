package net.disy.wps.richwps.response;

import java.io.InputStream;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import net.disy.wps.richwps.request.GetSupportedTypesRequest;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.ComplexTypesType;
import net.opengis.wps.x100.LiteralTypesType;
import net.opengis.wps.x100.SupportedTypesResponseDocument;
import net.opengis.wps.x100.SupportedTypesResponseDocument.SupportedTypesResponse.SupportedInputTypes;
import net.opengis.wps.x100.SupportedTypesResponseDocument.SupportedTypesResponse.SupportedOutputTypes;

import org.apache.xmlbeans.XmlCursor;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.util.XMLBeansHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GetSupportedTypesResponseBuilder is able to create an initial SupportedTypesResponse
 * and to update its contents
 * 
 * @author woessner
 * 
 */
public class GetSupportedTypesResponseBuilder implements IRichWPSResponseBuilder{
	private static Logger LOGGER = LoggerFactory.getLogger(GetSupportedTypesResponseBuilder.class);
	
	protected SupportedTypesResponseDocument doc;
	
	private SupportedInputTypes supportedInputTypes;
	private SupportedOutputTypes supportedOutputTypes;

	public GetSupportedTypesResponseBuilder(GetSupportedTypesRequest req) {
		XmlCursor c;

		doc = SupportedTypesResponseDocument.Factory.newInstance();
		doc.addNewSupportedTypesResponse();
		c = doc.newCursor();
		c.toFirstChild();
		c.toLastAttribute();
		c.setAttributeText(new QName(
				XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"),
				"./wpsGetSupportedTypes_response.xsd");
		supportedInputTypes = doc.getSupportedTypesResponse().addNewSupportedInputTypes();
		supportedOutputTypes = doc.getSupportedTypesResponse().addNewSupportedOutputTypes();
	}

	public void updateSupportedTypes () {

			List<IParser> allParsers = ParserFactory.getInstance().getAllParsers();
			List<IGenerator> allGenerators = GeneratorFactory.getInstance().getAllGenerators();
				
			// input types
			ComplexTypesType complexInputTypes = this.supportedInputTypes.addNewComplexTypes();
			LiteralTypesType literalInputTypes = this.supportedInputTypes.addNewLiteralTypes();

			for (IParser parser : allParsers) {
				String[] schemas = parser.getSupportedSchemas();
				String[] mimetypes = parser.getSupportedFormats();
				String[] encodings = parser.getSupportedEncodings();
				for(int i=0;i<schemas.length;i++) {
					LOGGER.debug("GetSupportedTypes: ComplexInputType: " + schemas[i] + " " + encodings[i] + " " + mimetypes[i]);
					ComplexDataDescriptionType type = complexInputTypes.addNewType();
					type.setEncoding(encodings[i]);
					type.setMimeType(mimetypes[i]);
					type.setSchema(schemas[i]);
				}
			}
			
			// output types
			ComplexTypesType complexOutputTypes = this.supportedOutputTypes.addNewComplexTypes();
			LiteralTypesType literalOutputTypes = this.supportedOutputTypes.addNewLiteralTypes();
			
			for (IGenerator generators : allGenerators) {
				String[] schemas = generators.getSupportedSchemas();
				String[] mimetypes = generators.getSupportedFormats();
				String[] encodings = generators.getSupportedEncodings();
				for(int i=0;i<schemas.length;i++) {
					LOGGER.debug("GetSupportedTypes: ComplexOutputType: " + schemas[i] + " " + encodings[i] + " " + mimetypes[i]);
					ComplexDataDescriptionType type = complexOutputTypes.addNewType();
					type.setEncoding(encodings[i]);
					type.setMimeType(mimetypes[i]);
					type.setSchema(schemas[i]);
				}
			}
	}

	@Override
	public InputStream getAsStream() throws ExceptionReport {
		try {
			return doc.newInputStream(XMLBeansHelper.getXmlOptions());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
