package net.disy.wps.richwps.request;

import net.disy.wps.richwps.response.TestProcessResponseBuilder;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.TestProcessDocument;
import net.opengis.wps.x100.TestProcessDocument.TestProcess;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.server.ExceptionReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * 
 * @author faltin
 *
 */
public class TestProcessRequest implements IRichWPSRequest {
	private static Logger LOGGER = LoggerFactory
			.getLogger(TestProcessRequest.class);
	protected TestProcessDocument testDoc;
	protected String processId, schema, executionUnit;
	protected ProcessDescriptionType processDescription;
	private TestProcessResponseBuilder responseBuilder;

	public TestProcessRequest(Document doc) throws ExceptionReport {

		try {
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			this.testDoc = TestProcessDocument.Factory.parse(doc, option);
			if (testDoc == null) {
				LOGGER.error("TestProcessDocument is null");
				throw new ExceptionReport("Error while parsing post data",
						ExceptionReport.MISSING_PARAMETER_VALUE);
			}
		} catch (XmlException e) {
			throw new ExceptionReport("Error while parsing post data",
					ExceptionReport.MISSING_PARAMETER_VALUE, e);
		}

		processDescription = testDoc.getTestProcess().getProcessDescription();
		processId = processDescription.getIdentifier().getStringValue().trim();
		executionUnit = testDoc.getTestProcess().getExecutionUnit().xmlText();
		responseBuilder = new TestProcessResponseBuilder(this);
		LOGGER.info("Test of process with processId: " + processId);
	}

	public TestProcessResponseBuilder getTestProcessResponseBuilder() {
		return responseBuilder;
	}

	public TestProcess getTestProcess() {
		return testDoc.getTestProcess();
	}

	public TestProcessDocument getTestDoc() {
		return testDoc;
	}

	public String getProcessId() {
		return processId;
	}

	public String getSchema() {
		return schema;
	}

	public String getExecutionUnit() {
		return executionUnit;
	}

	public ProcessDescriptionType getProcessDescription() {
		return processDescription;
	}

	public TestProcessResponseBuilder getResponseBuilder() {
		return responseBuilder;
	}

}
