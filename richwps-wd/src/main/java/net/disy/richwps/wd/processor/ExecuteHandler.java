package net.disy.richwps.wd.processor;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.RepositoryManager;

import de.hsos.richwps.dsl.api.elements.Binding;
import de.hsos.richwps.dsl.api.elements.Execute;
import de.hsos.richwps.dsl.api.elements.IOperation;
import de.hsos.richwps.dsl.api.elements.InReference;
import de.hsos.richwps.dsl.api.elements.OutReference;
import de.hsos.richwps.dsl.api.elements.Reference;
import de.hsos.richwps.dsl.api.elements.VarReference;


public class ExecuteHandler implements IOperationHandler {

	@Override
	public boolean canHandle(IOperation operation) {
		return operation instanceof Execute;
	}

	@Override
	public void handleOperation(IOperation operation, ProcessingContext context) {
		if (!canHandle(operation)) {
			throw new IllegalArgumentException("Could not handle operation as it is not of type " + Execute.class.getName());
		}
		Execute executeOperation = (Execute) operation;
		
		String processIdToExecute = getProcessIdToExecute(executeOperation, context);
		if (!RepositoryManager.getInstance().containsAlgorithm(processIdToExecute)) {
			throw new IllegalArgumentException(MessageFormat.format("Local process id {0} does not exist.", processIdToExecute));
		}
		
		Map<String, Reference> inputReferenceMapping = createReferenceMapping(executeOperation.getInputnames(), executeOperation.getInputreferences());
		Map<String, List<IData>> innerProcessInputData = new HashMap<String, List<IData>>();
		for (Map.Entry<String, Reference> inputReferenceMappingEntry : inputReferenceMapping.entrySet()) {			
			List<IData> dataForInputReference = getInputReferenceValueFromContext(inputReferenceMappingEntry.getValue(), context);
			innerProcessInputData.put(inputReferenceMappingEntry.getKey(), dataForInputReference);
		}
		
		IAlgorithm algorithm = RepositoryManager.getInstance().getAlgorithm(processIdToExecute);
		
		// TODO validate algorithm.getProcessDescription() if the bindings have valid data types or if they're incompatible
		
		Map<String, IData> innerProcessResultData = new HashMap<String, IData>();
		try {
			System.out.println("Executing local process with id " + processIdToExecute);
			innerProcessResultData = algorithm.run(innerProcessInputData);
		} catch (ExceptionReport e) {
			throw new RuntimeException(e);
		}
		
		if (innerProcessResultData == null) {
			throw new RuntimeException("The inner process " + processIdToExecute + " returned no result.");
		}
		
		Map<String, Reference> outputReferenceMapping = createReferenceMapping(executeOperation.getOutputnames(), executeOperation.getOutputreferences());
		for (Map.Entry<String, Reference> outputReferenceMappingEntry : outputReferenceMapping.entrySet()) {
			if (!innerProcessResultData.containsKey(outputReferenceMappingEntry.getKey())) {
				throw new RuntimeException("No result data found for output identifier " + outputReferenceMappingEntry.getKey());
			}
			IData dataForOutputReference = innerProcessResultData.get(outputReferenceMappingEntry.getKey());
			addOutputReferenceValueToContext(outputReferenceMappingEntry.getValue(), dataForOutputReference, context);
		}
        
	}

	private List<IData> getInputReferenceValueFromContext(Reference reference,
			ProcessingContext context) {
		if (reference instanceof InReference) {
			if (!context.getInputData().containsKey(reference.getId())) {
				throw new IllegalArgumentException("InReference does not exist: " + reference.getId());
			}
			return context.getInputData().get(reference.getId()); 
		}
		if (reference instanceof VarReference) {
			if (!context.getVariables().containsKey(reference.getId())) {
				throw new IllegalArgumentException("VarReference does not exist: " + reference.getId());
			}
			IData variableData = context.getVariables().get(reference.getId());
			if (variableData == null) {
				return null;
			}
			return Collections.singletonList(variableData);
		}
		throw new IllegalArgumentException("Unsupported input reference type");
	}

	private void addOutputReferenceValueToContext(Reference reference, IData outputData,
			ProcessingContext context) {
		if (reference instanceof OutReference) {
			context.getOutputData().put(reference.getId(), outputData);
			return;
		}
		if (reference instanceof VarReference) {
			context.getVariables().put(reference.getId(), outputData);
			return;
		}
		throw new IllegalArgumentException("Unsupported output reference type");
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
