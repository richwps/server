package net.disy.richwps.wd.processor;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.InputType;

import org.n52.wps.io.data.IData;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.server.request.InputHandler;

import de.hsos.richwps.dsl.api.elements.Binding;
import de.hsos.richwps.dsl.api.elements.Execute;
import de.hsos.richwps.dsl.api.elements.IOperation;
import de.hsos.richwps.dsl.api.elements.Reference;


public class ExecuteHandler implements IOperationHandler {

	@Override
	public boolean canHandle(IOperation operation) {
		return operation instanceof Execute;
	}

	@Override
	public void handleOperation(IOperation operation, ProcessingContext context) {
		Execute executeOperation = (Execute) operation;
		
		String processIdToExecute = getProcessIdToExecute(executeOperation, context);
		if (!RepositoryManager.getInstance().containsAlgorithm(processIdToExecute)) {
			throw new IllegalArgumentException(MessageFormat.format("Local process id {0} does not exist.", processIdToExecute));
		}
		
		// Get the input data for the WD WPS process and change the input names according to the target process later on
		InputType[] outerProcessInputs = context.getExecuteDocument().getExecute().getDataInputs().getInputArray();
		Map<String, List<IData>> outerProcessInputData = null;
		try {
			InputHandler outerProcessInputHandler = new InputHandler.Builder(outerProcessInputs, context.getExecuteDocument().getExecute().getIdentifier().getStringValue()).build();
			outerProcessInputData = outerProcessInputHandler.getParsedInputData();
		} catch (ExceptionReport e1) {
			throw new RuntimeException("Could not parse inputs", e1);
		}

		Map<String, Reference> inputReferenceMapping = createReferenceMapping(executeOperation.getInputnames(), executeOperation.getInputreferences());
		Map<String, List<IData>> innerProcessInputData = new HashMap<String, List<IData>>();
		for (Map.Entry<String, Reference> inputReferenceMappingEntry : inputReferenceMapping.entrySet()) {
			List<IData> dataForInputReference = outerProcessInputData.get(inputReferenceMappingEntry.getValue().getId());
			if (dataForInputReference == null) {
				throw new RuntimeException("No input data found for input reference " + inputReferenceMappingEntry.getKey());
			}
			innerProcessInputData.put(inputReferenceMappingEntry.getKey(), dataForInputReference);
		}
		
		IAlgorithm algorithm = RepositoryManager.getInstance().getAlgorithm(processIdToExecute);
		
		Map<String, IData> innerProcessResultData = new HashMap<String, IData>();
		try {
			System.out.println("Executing local process with id " + processIdToExecute);
			innerProcessResultData = algorithm.run(innerProcessInputData);
		} catch (ExceptionReport e) {
			throw new RuntimeException(e);
		}
		
		// TODO handle the case that the output gets saved in variables (var prefix) or a final output (out prefix)
		
		if (innerProcessResultData == null) {
			throw new RuntimeException("The inner process " + processIdToExecute + " returned no result.");
		}
		
		Map<String, Reference> outputReferenceMapping = createReferenceMapping(executeOperation.getOutputnames(), executeOperation.getOutputreferences());
		for (Map.Entry<String, Reference> outputReferenceMappingEntry : outputReferenceMapping.entrySet()) {
			if (!innerProcessResultData.containsKey(outputReferenceMappingEntry.getKey())) {
				throw new RuntimeException("No result data found for output identifier " + outputReferenceMappingEntry.getKey());
			}
			IData dataForOutputReference = innerProcessResultData.get(outputReferenceMappingEntry.getKey());
			context.getResults().put(outputReferenceMappingEntry.getValue().getId(), dataForOutputReference);
		}
        
	}

	private Map<String, Reference> createReferenceMapping(List<String> inputnames,
			List<Reference> inputreferences) {
		Map<String, Reference> references = new HashMap<String, Reference>();
		for (int i = 0; i < inputnames.size(); i++) {
			references.put(inputnames.get(i), inputreferences.get(i));
		}
		return references;
	}

	private String getProcessIdToExecute(Execute executeOperation, ProcessingContext context) {
		Binding binding = context.getBindings().get(executeOperation.getHandle());
		if (binding == null) {
			throw new IllegalStateException("Could not find binding for handle " + executeOperation.getHandle() + ". Maybe the binding declaration is missing for this handle.");
		}
		return binding.getProcessId();
	}
	
	

}
