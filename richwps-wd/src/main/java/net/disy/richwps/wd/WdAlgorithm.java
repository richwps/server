package net.disy.richwps.wd;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionsDocument;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.AbstractTransactionalAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.transactional.algorithm.GenericTransactionalAlgorithm;
import org.n52.wps.transactional.deploy.IProcessManager;
import org.n52.wps.transactional.service.TransactionalHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WdAlgorithm extends AbstractTransactionalAlgorithm {

	private static Logger LOGGER = LoggerFactory.getLogger(WdAlgorithm.class);
	
	public WdAlgorithm(String algorithmID) {
		super(algorithmID);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Map<String, IData> run(Map<String, List<IData>> inputData)
			throws ExceptionReport {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getErrors() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcessDescriptionType getDescription() {
		// TODO use generate method from transactionalrequesthandler
		String fullPath =  GenericTransactionalAlgorithm.class.getProtectionDomain().getCodeSource().getLocation().toString();
		int searchIndex= fullPath.indexOf("WEB-INF");
		String subPath = fullPath.substring(0, searchIndex);
		String processID = getAlgorithmID();
		// sanitize processID: strip version number and namespace if passed in
		if (processID.contains("-"))
			processID = processID.split("-")[0];
		if (processID.contains("}"))
			processID = processID.split("}")[1];
		try {
			URI fileUri = new URL(subPath + File.separator+"WEB-INF"+File.separator+"ProcessDescriptions"+File.separator+processID+".xml").toURI();
			File xmlDesc = new File(fileUri);
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			ProcessDescriptionsDocument doc = ProcessDescriptionsDocument.Factory.parse(xmlDesc, option);
			if(doc.getProcessDescriptions().getProcessDescriptionArray().length == 0) {
				LOGGER.warn("ProcessDescription does not contain any description");
				return null;
			}
			
			doc.getProcessDescriptions().getProcessDescriptionArray(0).getIdentifier().setStringValue(processID);

			
			return doc.getProcessDescriptions().getProcessDescriptionArray(0);
		}
		catch(IOException e) {
			LOGGER.warn("Could not initialize algorithm, parsing error: " + getAlgorithmID(), e);
		}
		catch(XmlException e) {
			LOGGER.warn("Could not initialize algorithm, parsing error: " +getAlgorithmID(), e);
		} catch (URISyntaxException e) {
			LOGGER.warn("Could not initialize algorithm, parsing error: " +getAlgorithmID(), e);
		}
		return null;
	}

	@Override
	public String getWellKnownName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean processDescriptionIsValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Class<?> getInputDataType(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getOutputDataType(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, IData> run(ExecuteDocument document) {
		try {
			IProcessManager deployManager = TransactionalHelper.getProcessManagerForSchema("RichWpsWd.xsd");
			return deployManager.invoke(document, getAlgorithmID());
			// resultMap.put("result", new LiteralStringBinding("meinresult"));

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
