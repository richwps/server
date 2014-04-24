package net.disy.richwps.wpsclient;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.junit.Test;
import org.n52.wps.client.ExecuteRequestBuilder;
import org.n52.wps.client.WPSClientSession;
import org.n52.wps.io.data.IData;
import org.n52.wps.transactional.algorithm.OutputParser;

public class SimpleExecuteTest {
	String url = "http://geoprocessing.demo.52north.org:8080/wps/WebProcessingService";
	String processId = "org.n52.wps.server.algorithm.test.DummyTestClass";
	String inputString = "Hello RichWPS!";

	WPSClientSession wpsClient = WPSClientSession.getInstance();

	@Test
	public void testExecuteRequest() throws Exception {

		wpsClient.connect(url);

		// get process description
		ProcessDescriptionType processDescription = getProcessDescription(url,
				processId);

		// define inputs
		HashMap<String, Object> inputs = new HashMap<String, Object>();
		inputs.put("LiteralInputData", inputString);

		IData resultData = executeProcess(url, processId, processDescription,
				inputs);
		String resultString = (String) resultData.getPayload();

		System.out.println("LiteralInputData: " + inputString);
		System.out.println("LiteralOutputData: " + resultString);
		
		assertEquals(inputString, resultString);
	}

	public ProcessDescriptionType getProcessDescription(String url,
			String processID) throws IOException {

		ProcessDescriptionType processDescription = wpsClient
				.getProcessDescription(url, processID);

		InputDescriptionType[] inputList = processDescription.getDataInputs()
				.getInputArray();

		for (InputDescriptionType input : inputList) {
			System.out.println("Input: "
					+ input.getIdentifier().getStringValue());
		}

		OutputDescriptionType[] outputList = processDescription
				.getProcessOutputs().getOutputArray();

		for (OutputDescriptionType output : outputList) {
			System.out.println("Output: "
					+ output.getIdentifier().getStringValue());
		}

		return processDescription;
	}

	public IData executeProcess(String url, String processId,
			ProcessDescriptionType processDescription,
			HashMap<String, Object> inputs) throws Exception {

		ExecuteRequestBuilder executeBuilder = new ExecuteRequestBuilder(
				processDescription);

		for (InputDescriptionType input : processDescription.getDataInputs()
				.getInputArray()) {
			String inputName = input.getIdentifier().getStringValue();
			Object inputValue = inputs.get(inputName);
			if (input.getLiteralData() != null) {
				if (inputValue instanceof String) {
					executeBuilder.addLiteralData(inputName,
							(String) inputValue);
				}
			} 
			/*
			else if (input.getComplexData() != null) {
				// Complexdata by value
				if (inputValue instanceof FeatureCollection) {
					IData data = new GTVectorDataBinding(
							(FeatureCollection) inputValue);
					executeBuilder
							.addComplexData(
									inputName,
									data,
									"http://schemas.opengis.net/gml/3.1.1/base/feature.xsd",
									"UTF-8", "text/xml");
				}
				// Complexdata Reference
				if (inputValue instanceof String) {
					executeBuilder
							.addComplexDataReference(
									inputName,
									(String) inputValue,
									"http://schemas.opengis.net/gml/3.1.1/base/feature.xsd",
									"UTF-8", "text/xml");
				}

				if (inputValue == null && input.getMinOccurs().intValue() > 0) {
					throw new IOException("Property not set, but mandatory: "
							+ inputName);
				}
			}
			*/
		}

		executeBuilder.addOutput("LiteralOutputData");

		ExecuteDocument execute = executeBuilder.getExecute();
		execute.getExecute().setService("WPS");

		WPSClientSession wpsClient = WPSClientSession.getInstance();
		Object responseObject = wpsClient.execute(url, execute);

		if (responseObject instanceof ExecuteResponseDocument) {
			ExecuteResponseDocument response = (ExecuteResponseDocument) responseObject;
			IData data = null;

			OutputDataType[] resultValues = response.getExecuteResponse()
					.getProcessOutputs().getOutputArray();

			OutputDataType resultValue = resultValues[0];

			if (resultValue.getData().getLiteralData() != null) {
				data = OutputParser.handleLiteralValue(resultValue);
			}

			return data;
		}
		throw new Exception("Exception: " + responseObject.toString());
	}
}
