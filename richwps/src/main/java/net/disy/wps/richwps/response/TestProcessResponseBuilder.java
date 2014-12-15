package net.disy.wps.richwps.response;

import java.io.InputStream;
import java.util.Calendar;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import net.disy.wps.richwps.request.TestProcessRequest;
import net.opengis.wps.x100.DataInputsType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.TestProcessResponseDocument;

import org.apache.xmlbeans.XmlCursor;
import org.n52.wps.server.CapabilitiesConfiguration;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.request.Request;
import org.n52.wps.server.response.RawData;
import org.n52.wps.util.XMLBeansHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author faltin
 *
 */
public class TestProcessResponseBuilder {
	private static Logger LOGGER = LoggerFactory
			.getLogger(TestProcessResponseBuilder.class);

	private String identifier;
	private DataInputsType dataInputs;
	protected TestProcessResponseDocument doc;
	private TestProcessRequest request;
	private RawData rawDataHandler = null;
	private ProcessDescriptionType description;
	private Calendar creationTime;

	public TestProcessResponseBuilder(TestProcessRequest req) {
		this.request = req;
		doc = TestProcessResponseDocument.Factory.newInstance();
		doc.addNewTestProcessResponse();
		XmlCursor c = doc.newCursor();
		c.toFirstChild();
		c.toLastAttribute();
		c.setAttributeText(new QName(
				XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"),
				"./wpsTestProcess_response.xsd");
		doc.getTestProcessResponse().setServiceInstance(
				CapabilitiesConfiguration.WPS_ENDPOINT_URL
						+ "?REQUEST=GetCapabilities&SERVICE=WPS");
		doc.getTestProcessResponse().setLang(
				WebProcessingService.DEFAULT_LANGUAGE);
		doc.getTestProcessResponse().setService("WPS");
		doc.getTestProcessResponse().setVersion(Request.SUPPORTED_VERSION);

		doc.getTestProcessResponse().addNewProcess();
		doc.getTestProcessResponse().addNewStatus();
		doc.getTestProcessResponse().addNewDataInputs();
		doc.getTestProcessResponse().addNewProcessOutputs();

	}

	public InputStream getAsStream() {
		try {
			return doc.newInputStream(XMLBeansHelper.getXmlOptions());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void update() throws ExceptionReport {
		// copying the request parameters to the response
		TestProcessResponseDocument.TestProcessResponse testProcessResponse = doc
				.getTestProcessResponse();
		testProcessResponse.getStatus().setProcessSucceeded(
				"Process successful");
		// testProcessResponse.getProcess().addNewIdentifier()
		// .setStringValue(request.getProcessId());
		// testProcessResponse.getProcess().addNewTitle()
		// .setStringValue(request.getProcessId());

		// FIXME Replace test values with actual values after process execution
		// Test Inputs
		testProcessResponse.getProcess().addNewIdentifier()
				.setStringValue("test");
		testProcessResponse.getProcess().addNewTitle().setStringValue("test");
		testProcessResponse.getDataInputs().addNewInput();
		testProcessResponse.getDataInputs().getInputArray(0).addNewIdentifier()
				.setStringValue("literalInput");
		testProcessResponse.getDataInputs().getInputArray(0).addNewData()
				.addNewLiteralData().setStringValue("literalInputValue");
		testProcessResponse.getDataInputs().addNewInput();
		testProcessResponse.getDataInputs().getInputArray(1).addNewIdentifier()
				.setStringValue("referenceInput");
		testProcessResponse.getDataInputs().getInputArray(1).addNewReference()
				.setHref("http://foo.bar/some_source.xml");
		testProcessResponse.getDataInputs().addNewInput();
		// testProcessResponse.getDataInputs().getInputArray(2).addNewIdentifier()
		// .setStringValue("dataInput");
		// testProcessResponse
		// .getDataInputs()
		// .getInputArray(2)
		// .addNewData()
		// .addNewComplexData()
		// .setSchema(
		// "http://schemas.opengis.net/gml/3.1.1/base/gml.xsd#Polygon");
		// Test Outputs
		testProcessResponse.getProcessOutputs().addNewOutput();
		testProcessResponse.getProcessOutputs().getOutputArray(0)
				.addNewIdentifier().setStringValue("literalOutput");
		testProcessResponse.getProcessOutputs().getOutputArray(0).addNewData()
				.addNewLiteralData().setStringValue("literalOutputValue");
		testProcessResponse.getProcessOutputs().addNewOutput();
		testProcessResponse.getProcessOutputs().getOutputArray(1)
				.addNewIdentifier().setStringValue("referenceOutput");
		testProcessResponse.getProcessOutputs().getOutputArray(1)
				.addNewReference()
				.setHref("http://foo.bar/some_destination1.xml");

		testProcessResponse.getProcessOutputs().addNewOutput();
		testProcessResponse.getProcessOutputs().getOutputArray(2)
				.addNewIdentifier()
				.setStringValue("var.intermediateLiteralOutput");
		testProcessResponse.getProcessOutputs().getOutputArray(2).addNewData()
				.addNewLiteralData()
				.setStringValue("intermediateLiteralOutputValue");

		testProcessResponse.getProcessOutputs().addNewOutput();
		testProcessResponse.getProcessOutputs().getOutputArray(3)
				.addNewIdentifier()
				.setStringValue("var.intermediateReferenceOutput");
		testProcessResponse.getProcessOutputs().getOutputArray(3)
				.addNewReference()
				.setHref("http://foo.bar/some_destination2.xml");

		// Test IntermediateOutputs
		// testProcessResponse.getIntermediateProcessOutputs()
		// .addNewIntermediateOutput();
		// testProcessResponse.getIntermediateProcessOutputs()
		// .getIntermediateOutputArray(0).addNewIdentifier()
		// .setStringValue("var.intermediateLiteralOutput");
		// testProcessResponse.getIntermediateProcessOutputs()
		// .getIntermediateOutputArray(0).addNewData().addNewLiteralData()
		// .setStringValue("intermediateLiteralOutputValue");
		//
		// testProcessResponse.getIntermediateProcessOutputs()
		// .addNewIntermediateOutput();
		// testProcessResponse.getIntermediateProcessOutputs()
		// .getIntermediateOutputArray(1).addNewIdentifier()
		// .setStringValue("var.intermediateReferenceOutput");
		// testProcessResponse.getIntermediateProcessOutputs()
		// .getIntermediateOutputArray(1).addNewReference()
		// .setHref("http://foo.bar/some_destination2.xml");
	}
}
