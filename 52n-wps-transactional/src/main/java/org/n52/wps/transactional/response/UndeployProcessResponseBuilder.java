package org.n52.wps.transactional.response;

import java.io.InputStream;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import net.opengis.wps.x100.UndeployProcessResponseDocument;
import net.opengis.wps.x100.ProcessBriefType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.xmlbeans.XmlCursor;
import org.n52.wps.transactional.request.UndeployProcessRequest;
import org.n52.wps.util.XMLBeansHelper;

public class UndeployProcessResponseBuilder {
	protected UndeployProcessResponseDocument doc;

	public UndeployProcessResponseBuilder(UndeployProcessRequest req) {
		XmlCursor c;
		// TODO
		doc = UndeployProcessResponseDocument.Factory.newInstance();
		doc.addNewUndeployProcessResponse();
		c = doc.newCursor();
		c.toFirstChild();
		c.toLastAttribute();
		c.setAttributeText(new QName(
				XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"),
				"./wpsUndeployProcess_response.xsd");

		doc.getUndeployProcessResponse().addNewUndeployment();
		doc.getUndeployProcessResponse().getUndeployment().setDone(true);
		doc.getUndeployProcessResponse().addNewProcessOfferings();
	}

	public InputStream getAsStream() {
		try {
			return doc.newInputStream(XMLBeansHelper.getXmlOptions());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void updateUndeployment(boolean deploymentValue) {
		doc.getUndeployProcessResponse().getUndeployment()
				.setDone(deploymentValue);
	}

	public void updateProcessOfferings(
			Map<String, ProcessDescriptionType> processDescriptions) {

		for (Map.Entry<String, ProcessDescriptionType> entry : processDescriptions
				.entrySet()) {
			ProcessBriefType process = doc.getUndeployProcessResponse()
					.getProcessOfferings().addNewProcess();
			process.addNewIdentifier().setStringValue(entry.getKey());
			if (entry.getValue().getTitle() != null) {
				process.addNewTitle().setStringValue(
						entry.getValue().getTitle().getStringValue());
			}
			if (entry.getValue().getProcessVersion() != null) {
				process.setProcessVersion(entry.getValue().getProcessVersion());
			} else {
				process.setProcessVersion("1.0.0");
			}
		}
	}
}
