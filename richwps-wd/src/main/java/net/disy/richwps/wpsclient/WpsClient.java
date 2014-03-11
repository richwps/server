package net.disy.richwps.wpsclient;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.commons.lang.Validate;
import org.n52.wps.client.WPSClientException;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.transactional.algorithm.OutputParser;

public class WpsClient {

	private final String wpsUrl;
	
	public WpsClient(String wpsUrl) {
		Validate.notEmpty(wpsUrl);
		this.wpsUrl = wpsUrl;
	}
	
	public Map<String, IData> executeProcess(String processId, Map<String, List<IData>> inputData, List<String> outputNames) {
		final Map<String, IData> inputs = new HashMap<String, IData>();
		for (Map.Entry<String, List<IData>> entry : inputData.entrySet()) {
			inputs.put(entry.getKey(), entry.getValue().get(0));
		}
		ProcessDescriptionType processDescription = getProcessDescription(processId);
		try {
			return executeProcess(processId, processDescription, inputs, outputNames);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private ProcessDescriptionType getProcessDescription(String processId) {
		WPSClientSession wpsClient = WPSClientSession.getInstance();

		ProcessDescriptionType processDescription;
		try {
			processDescription = wpsClient.getProcessDescription(wpsUrl, processId);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return processDescription;
	}

	private Map<String, IData> executeProcess(String processID,
			ProcessDescriptionType processDescription,
			Map<String, IData> inputs, List<String> outputNames) throws WPSClientException, ExceptionReport {
		org.n52.wps.client.ExecuteRequestBuilder executeBuilder = new org.n52.wps.client.ExecuteRequestBuilder(
				processDescription);

		for (InputDescriptionType input : processDescription.getDataInputs()
				.getInputArray()) {
			String inputName = input.getIdentifier().getStringValue();
			IData inputValue = inputs.get(inputName);
			if (inputValue == null && input.getMinOccurs().intValue() > 0) {
				throw new RuntimeException("Property not set, but mandatory: "
						+ inputName);
			}
			if (inputValue == null) {
				continue;
			}
			if (input.getLiteralData() != null) {
				executeBuilder.addLiteralData(inputName, ((LiteralStringBinding) inputValue).getPayload());
			} else if (input.getComplexData() != null) {
				// TODO determine schema and other params from the matching binding
				executeBuilder.addComplexData(inputName, inputValue, null, "UTF-8", "text/xml");
				// Complexdata by value
/*				if (inputValue instanceof FeatureCollection) {
					IData data = new GTVectorDataBinding(
							(FeatureCollection) inputValue);
					try {
						executeBuilder
								.addComplexData(
										inputName,
										data,
										"http://schemas.opengis.net/gml/3.1.1/base/feature.xsd",
										"UTF-8", "text/xml");
					} catch (WPSClientException e) {
						throw new RuntimeException(e);
					}
				}
				// Complexdata Reference
				if (inputValue instanceof String) {
					executeBuilder
							.addComplexDataReference(
									inputName,
									(String) inputValue,
									"http://schemas.opengis.net/gml/3.1.1/base/feature.xsd",
									"UTF-8", "text/xml");
				} */
			}
		}
		
		// TODO only applies to complex data, not needed for literal data
		//executeBuilder.setMimeTypeForOutput("text/plain", "result");
//		executeBuilder.setSchemaForOutput(
//				"http://schemas.opengis.net/gml/3.1.1/base/feature.xsd",
//				"result");
	
		for (String outputName : outputNames) {
			executeBuilder.addOutput(outputName);	
		}
		
		ExecuteDocument execute = executeBuilder.getExecute();
		execute.getExecute().setService("WPS");
		WPSClientSession wpsClient = WPSClientSession.getInstance();
		Object responseObject = wpsClient.execute(wpsUrl, execute);
		// Improvement: Maybe we can improve the ExecuteResponseAnalyser to support all data types
		// because now it doesn't support literal data, that's why I took the code here from the draft in
		// GenericTransactionalAlgorithm.
		Map<String, IData> resultData = new HashMap<String, IData>();
		if (responseObject instanceof ExecuteResponseDocument) {
			ExecuteResponseDocument response = (ExecuteResponseDocument) responseObject;
			OutputDataType[] resultValues = response.getExecuteResponse().getProcessOutputs().getOutputArray();
			
			for (OutputDataType resultValue : resultValues) {
				//3.get the identifier as key
				String key = resultValue.getIdentifier().getStringValue();
				//4.the the literal value as String
				if(resultValue.getData().getLiteralData()!=null){
					resultData.put(key, OutputParser.handleLiteralValue(resultValue));
				}
				//5.parse the complex value
				if(resultValue.getData().getComplexData()!=null){
					
					// TODO gather outputdescription for handling the complex value
					//resultData.put(key, OutputParser.handleComplexValue(resultValue, getDescription()));
					
				}
				//6.parse the complex value reference
				if(resultValue.getReference()!=null){
					//TODO handle this
					//download the data, parse it and put it in the hashmap
					//resultHash.put(key, OutputParser.handleComplexValueReference(ioElement));
				}
				
				//7.parse Bounding Box value
				if(resultValue.getData().getBoundingBoxData()!=null){
					resultData.put(key, OutputParser.handleBBoxValue(resultValue));
				}	
			
			}
			return resultData;

		}
		throw new RuntimeException("Exception: " + responseObject);
	}
	

}
