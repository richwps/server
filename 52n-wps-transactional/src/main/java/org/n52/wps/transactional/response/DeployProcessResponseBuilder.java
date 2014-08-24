package org.n52.wps.transactional.response;

import java.io.InputStream;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import net.opengis.wps.x100.DeployProcessResponseDocument;
import net.opengis.wps.x100.ProcessBriefType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.xmlbeans.XmlCursor;
import org.n52.wps.transactional.request.DeployProcessRequest;
import org.n52.wps.util.XMLBeansHelper;

/**
 * DeployProcessResponseBuilder is able to create an initial DeployProcess
 * response and to update its contents
 * 
 * @author woessner
 * 
 */
public class DeployProcessResponseBuilder {
	protected DeployProcessResponseDocument doc;

	public DeployProcessResponseBuilder(DeployProcessRequest req) {
		XmlCursor c;

		doc = DeployProcessResponseDocument.Factory.newInstance();
		doc.addNewDeployProcessResponse();
		c = doc.newCursor();
		c.toFirstChild();
		c.toLastAttribute();
		c.setAttributeText(new QName(
				XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"),
				"./wpsDeployProcess_response.xsd");
		doc.getDeployProcessResponse().addNewDeployment();
		doc.getDeployProcessResponse().addNewProcessOfferings();
	}

	public InputStream getAsStream() {
		try {
			return doc.newInputStream(XMLBeansHelper.getXmlOptions());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void updateDeployment(boolean deploymentValue) {
		doc.getDeployProcessResponse().getDeployment().setDone(deploymentValue);
	}

	public void updateProcessOfferings(
			Map<String, ProcessDescriptionType> processDescriptions) {

		for (Map.Entry<String, ProcessDescriptionType> entry : processDescriptions
				.entrySet()) {
			ProcessBriefType process = doc.getDeployProcessResponse()
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
