/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Bastian Schaeffer, Institute for Geoinformatics, Muenster, Germany

 Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 52n@conterra.de

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt); if not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA or visit the web page of the Free
 Software Foundation, http://www.fsf.org.

 ***************************************************************/

package org.n52.wps.transactional.request;

import net.opengis.wps.x100.DeployProcessDocument;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.transactional.deploymentprofiles.DeploymentProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class DeployProcessRequest implements ITransactionalRequest {
	
	private static Logger LOGGER = LoggerFactory.getLogger(DeployProcessRequest.class);
	
	protected DeployProcessDocument deployDoc;
	protected String processId, schema, executionUnit;
	protected ProcessDescriptionType processDescription;
	protected String deploymentProfileName;

	public DeployProcessRequest(Document doc) throws ExceptionReport {
		
		try {
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			this.deployDoc = DeployProcessDocument.Factory.parse(doc,option);
			if (deployDoc == null) {
				LOGGER.error("DeployProcessDocument is null");
				throw new ExceptionReport("Error while parsing post data",
						ExceptionReport.MISSING_PARAMETER_VALUE);
			}
		}
		catch (XmlException e){
			throw new ExceptionReport("Error while parsing post data",
					ExceptionReport.MISSING_PARAMETER_VALUE, e);
		}
				
		
		processDescription = deployDoc.getDeployProcess().getProcessDescription();
		processId = processDescription.getIdentifier().getStringValue().trim();
		executionUnit = clearExecutionUnit(((SimpleValue) deployDoc.getDeployProcess().getExecutionUnit()).getStringValue());
		deploymentProfileName = deployDoc.getDeployProcess().getDeploymentProfileName().trim();
		
		LOGGER.info("Deployment of process with processId: " + processId);
	}

	public String getProcessId() {
		return processId;
	}
	
	public ProcessDescriptionType getProcessDescription() {
		return processDescription;
	}

	public String getExecutionUnit() {
		return executionUnit;
	}

	public String getDeploymentProfileName() {
		return deploymentProfileName;
	}
	
	public DeploymentProfile getDeploymentProfile() {
		// TODO
		return null;
	}
	
	private String clearExecutionUnit(String execUnit) {
		String clearedExecUnit = execUnit.replaceAll("\t", "").trim();
		return clearedExecUnit;
	}
}
