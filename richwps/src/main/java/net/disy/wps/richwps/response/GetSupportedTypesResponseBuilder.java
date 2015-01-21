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
import org.n52.wps.io.BasicXMLTypeFactory;
import org.n52.wps.io.GeneratorFactory;
import org.n52.wps.io.IGenerator;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.ILiteralData;
import org.n52.wps.io.data.binding.literal.LiteralAnyURIBinding;
import org.n52.wps.io.data.binding.literal.LiteralBase64BinaryBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralByteBinding;
import org.n52.wps.io.data.binding.literal.LiteralDateTimeBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralFloatBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralLongBinding;
import org.n52.wps.io.data.binding.literal.LiteralShortBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.util.XMLBeansHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GetSupportedTypesResponseBuilder is able to create an initial
 * SupportedTypesResponse and to update its contents
 * 
 * @author woessner
 * 
 */
public class GetSupportedTypesResponseBuilder implements
		IRichWPSResponseBuilder {
	private static Logger LOGGER = LoggerFactory
			.getLogger(GetSupportedTypesResponseBuilder.class);

	protected SupportedTypesResponseDocument doc;

	private SupportedInputTypes supportedInputTypes;
	private SupportedOutputTypes supportedOutputTypes;
	private static Class[] literalBindings = { LiteralFloatBinding.class,
			LiteralDoubleBinding.class, LiteralLongBinding.class,
			LiteralIntBinding.class, LiteralShortBinding.class,
			LiteralByteBinding.class, LiteralBooleanBinding.class,
			LiteralStringBinding.class, LiteralDateTimeBinding.class,
			LiteralBase64BinaryBinding.class, LiteralAnyURIBinding.class };

	private boolean complexTypesOnly;

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
		supportedInputTypes = doc.getSupportedTypesResponse()
				.addNewSupportedInputTypes();
		supportedOutputTypes = doc.getSupportedTypesResponse()
				.addNewSupportedOutputTypes();

		complexTypesOnly = req.getComplexTypesOnly();
	}

	@SuppressWarnings("unchecked")
	public void updateSupportedTypes() {

		List<IParser> allParsers = ParserFactory.getInstance().getAllParsers();
		List<IGenerator> allGenerators = GeneratorFactory.getInstance()
				.getAllGenerators();

		// COMPLEX INPUT TYPES
		ComplexTypesType complexInputTypes = this.supportedInputTypes
				.addNewComplexTypes();

		for (IParser parser : allParsers) {
			String[] schemas = parser.getSupportedSchemas();
			String[] mimetypes = parser.getSupportedFormats();
			String[] encodings = parser.getSupportedEncodings();
			for (int i = 0; i < schemas.length; i++) {
				LOGGER.debug("GetSupportedTypes: ComplexInputType: "
						+ schemas[i] + " " + encodings[i] + " " + mimetypes[i]);
				ComplexDataDescriptionType type = complexInputTypes
						.addNewType();
				type.setEncoding(encodings[i]);
				type.setMimeType(mimetypes[i]);
				type.setSchema(schemas[i]);
			}
		}

		// COMPLEX OUTPUT TYPES
		ComplexTypesType complexOutputTypes = this.supportedOutputTypes
				.addNewComplexTypes();

		for (IGenerator generators : allGenerators) {
			String schemaValue, mimetypeValue, encodingValue;
			String[] schemas = generators.getSupportedSchemas();
			String[] mimetypes = generators.getSupportedFormats();
			String[] encodings = generators.getSupportedEncodings();
			Integer loopcount = getLargerArray(schemas,
					getLargerArray(mimetypes, encodings)).length;

			for (int i = 0; i < loopcount; i++) {
				schemaValue = getArrayValue(schemas, i);
				mimetypeValue = getArrayValue(mimetypes, i);
				encodingValue = getArrayValue(encodings, i);

				LOGGER.debug("GetSupportedTypes: ComplexOutputType: "
						+ schemaValue + " " + encodingValue + " "
						+ mimetypeValue);
				ComplexDataDescriptionType type = complexOutputTypes
						.addNewType();
				type.setEncoding(encodingValue);
				type.setMimeType(mimetypeValue);
				type.setSchema(schemaValue);
			}
		}

		// LITERAL INPUT AND OUTPUT TYPES
		if (!this.complexTypesOnly) {
			LiteralTypesType literalInputTypes = this.supportedInputTypes
					.addNewLiteralTypes();
			LiteralTypesType literalOutputTypes = this.supportedOutputTypes
					.addNewLiteralTypes();

			for (Class<? extends ILiteralData> clazz : literalBindings) {
				String xmltype = BasicXMLTypeFactory
						.getXMLDataTypeforBinding(clazz);
				literalInputTypes.addNewDataType().setReference(xmltype);
				literalOutputTypes.addNewDataType().setReference(xmltype);
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

	public String[] getLargerArray(String[] arr1, String[] arr2) {
		if (arr1.length >= arr2.length) {
			return arr1;
		} else {
			return arr2;
		}
	}

	public String getArrayValue(String[] arr, Integer index) {
		String value;
		try {
			value = arr[index];
			return value;
		} catch (Exception e) {
			return "";
		}
	}

}
