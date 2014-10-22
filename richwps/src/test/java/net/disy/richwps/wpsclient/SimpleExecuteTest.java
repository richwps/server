package net.disy.richwps.wpsclient;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.disy.wps.richwps.wpsclient.WpsClient;
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
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.transactional.algorithm.OutputParser;

public class SimpleExecuteTest {
	String url = "http://geoprocessing.demo.52north.org:8080/wps/WebProcessingService";
	//String url = "http://localhost:8085/wps/WebProcessingService";
	String processId;
	
	WpsClient wpsClient = new WpsClient(url);

	@Test
	public void testSimpleExecuteRequest() throws Exception {
		processId = "org.n52.wps.server.algorithm.test.DummyTestClass";
		String inputString = "Hello RichWPS!";
		
		HashMap<String, IData> results = new HashMap<String, IData>();
		
		Map<String, List<IData>> inputs = new HashMap<String, List<IData>>();
		ArrayList<IData> inputArray = new ArrayList<IData>();
		inputArray.add(new LiteralStringBinding(inputString));
		inputs.put("LiteralInputData", inputArray);
		
		List<String> outputs = new ArrayList<String>();
		outputs.add("LiteralOutputData");

		results = (HashMap<String, IData>) wpsClient.executeProcess(processId, inputs, outputs);

		String resultString = (String) results.get("LiteralOutputData").getPayload();

		System.out.println("LiteralInputData: " + inputString);
		System.out.println("LiteralOutputData: " + resultString);
		
		assertEquals(inputString, resultString);
	}
	
	@Test
	public void testComplexExecuteRequest() throws Exception {
		processId = "org.n52.wps.server.algorithm.SimpleBufferAlgorithm";
		String inputData = "http://geoprocessing.demo.52north.org:8080/geoserver/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=topp:tasmania_roads&outputFormat=GML3";
		Double inputWidth = 0.05;
		
		HashMap<String, IData> results = new HashMap<String, IData>();
		
		Map<String, List<IData>> inputs = new HashMap<String, List<IData>>();
		ArrayList<IData> inputArrayData = new ArrayList<IData>();
		inputArrayData.add(new LiteralStringBinding(inputData));
		inputs.put("data", inputArrayData);
		ArrayList<IData> inputArrayWidth = new ArrayList<IData>();
		inputArrayWidth.add(new LiteralDoubleBinding(inputWidth));
		inputs.put("width", inputArrayWidth);
		
		List<String> outputs = new ArrayList<String>();
		outputs.add("result");

		// TODO: execute! 
		// execution is successful, but result parser fails because of mismatching schema definitions in process description
		//results = (HashMap<String, IData>) wpsClient.executeProcess(processId, inputs, outputs);
		
	}

	
}
