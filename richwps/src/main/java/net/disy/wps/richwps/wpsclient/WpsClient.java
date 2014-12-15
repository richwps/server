package net.disy.wps.richwps.wpsclient;

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
import org.n52.wps.server.ExceptionReport;

public class WpsClient {

	private final String wpsUrl;

	public WpsClient(String wpsUrl) {
		Validate.notEmpty(wpsUrl);
		this.wpsUrl = wpsUrl;
	}

	public Map<String, IData> executeProcess(String processId,
			Map<String, List<IData>> inputData, List<String> outputNames) {
		// Converting the sub-list of IData elements to take the first one and
		// conform to the API of the WPS client
		final Map<String, IData> inputs = new HashMap<String, IData>();
		for (Map.Entry<String, List<IData>> entry : inputData.entrySet()) {
			inputs.put(entry.getKey(), entry.getValue().get(0));
		}
		ProcessDescriptionType processDescription = getProcessDescription(processId);
		try {
			return executeProcess(processId, processDescription, inputs,
					outputNames);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private ProcessDescriptionType getProcessDescription(String processId) {
		WPSClientSession wpsClient = WPSClientSession.getInstance();
		try {
			return wpsClient.getProcessDescription(wpsUrl, processId);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, IData> executeProcess(String processID,
			ProcessDescriptionType processDescription,
			Map<String, IData> inputs, List<String> outputNames) throws WPSClientException, ExceptionReport {
		ExecuteDocument executeDocument = buildExecuteDocument(processDescription, inputs,
				outputNames);
		WPSClientSession wpsClient = WPSClientSession.getInstance();
		Object responseObject = wpsClient.execute(wpsUrl, executeDocument);
		if (responseObject instanceof ExecuteResponseDocument) {
			return getOutputDataFromResponse((ExecuteResponseDocument) responseObject, processDescription);
		}
		throw new RuntimeException("Exception: " + responseObject);
	}

	private ExecuteDocument buildExecuteDocument(
			ProcessDescriptionType processDescription,
			Map<String, IData> inputs, List<String> outputNames) {
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

				executeBuilder.addLiteralData(inputName,
						String.valueOf(inputValue.getPayload()));

			} else if (input.getComplexData() != null) {
				try {
					executeBuilder.addComplexData(
							inputName,
							inputValue,
							input.getComplexData().getDefault().getFormat().getSchema(),
							input.getComplexData().getDefault().getFormat().getEncoding(),
							input.getComplexData().getDefault().getFormat().getMimeType()
							);
				} catch (WPSClientException e) {
					throw new RuntimeException(e);
				}
			}
		}

		for (String outputName : outputNames) {
			executeBuilder.addOutput(outputName);
		}
		ExecuteDocument executeDocument = executeBuilder.getExecute();
		executeDocument.getExecute().setService("WPS");
		return executeDocument;
	}

	private Map<String, IData> getOutputDataFromResponse(
			ExecuteResponseDocument response, ProcessDescriptionType processDescription) throws ExceptionReport {
		Map<String, IData> resultData = new HashMap<String, IData>();
		OutputDataType[] resultValues = response.getExecuteResponse()
				.getProcessOutputs().getOutputArray();

		for (OutputDataType resultValue : resultValues) {

			String outputIdentifier = resultValue.getIdentifier().getStringValue();
			// Literal output
			if (resultValue.getData().getLiteralData() != null) {
				resultData.put(outputIdentifier,
						OutputHandler.handleLiteralValue(resultValue));
			}
			// Complex output
			if (resultValue.getData().getComplexData() != null) {
				resultData.put(outputIdentifier, OutputHandler.handleComplexValue(
						resultValue, processDescription));
			}
			// Reference output
			if (resultValue.getReference() != null) {
				// TODO handle this
				// download the data, parse it and put it in the hashmap
				// resultData.put(key,
				// OutputHandler.handleComplexValueReference(resultValue));
			}
			// BoundingBox output
			if (resultValue.getData().getBoundingBoxData() != null) {
				resultData.put(outputIdentifier, OutputHandler.handleBBoxValue(resultValue));
			}
		}
		return resultData;
	}
}
