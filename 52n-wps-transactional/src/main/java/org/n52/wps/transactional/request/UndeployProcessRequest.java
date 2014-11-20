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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.UndeployProcessDocument;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.n52.wps.transactional.response.UndeployProcessResponseBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class UndeployProcessRequest implements ITransactionalRequest {
	
	private static Logger LOGGER = LoggerFactory.getLogger(UndeployProcessRequest.class);
	
	protected UndeployProcessDocument undeployDoc;
	protected String processId;
	protected boolean keepExecUnit;
	
	private UndeployProcessResponseBuilder responseBuilder;

	public UndeployProcessRequest(Document doc) throws ExceptionReport {
		
		// Create initial response
		responseBuilder = new UndeployProcessResponseBuilder(this);
		
		try {
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			this.undeployDoc = UndeployProcessDocument.Factory.parse(doc,option);
			
			if (undeployDoc == null) {
				LOGGER.error("UndeployProcessDocument is null");
				throw new ExceptionReport("Error while parsing post data",
						ExceptionReport.MISSING_PARAMETER_VALUE);
			}
		}
		catch (XmlException e){
			throw new ExceptionReport("Error while parsing post data",
					ExceptionReport.MISSING_PARAMETER_VALUE, e);
		}
		
		processId = undeployDoc.getUndeployProcess().getProcess().getIdentifier().getStringValue().trim();
		keepExecUnit = undeployDoc.getUndeployProcess().getProcess().getKeepExecutionUnit();
		
		responseBuilder.updateUndeployment(true);
		
		LOGGER.info("Undeployment of process with processId: " + processId);
	}

	public String getProcessID() {
		return processId;
	}
	
	public boolean getKeepExecutionUnit() {
		return keepExecUnit;
	}
	
	public void updateResponseProcessDescriptions (ITransactionalAlgorithmRepository repository) {
		Map<String, ProcessDescriptionType> processDescriptions = new HashMap<String, ProcessDescriptionType>();
		Collection<String> identifiers = repository.getAlgorithmNames();
		ProcessDescriptionType processDescr;
		
		for (String identifier : identifiers) {
			processDescr = repository.getProcessDescription(identifier);
			processDescriptions.put(identifier, processDescr);
		}
		this.responseBuilder.updateProcessOfferings(processDescriptions);
	}
	
	public UndeployProcessResponseBuilder getUndeployResponseBuilder() {
		return this.responseBuilder;
	}
}
