package net.disy.wps.richwps.oe.processor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.disy.wps.richwps.process.binding.IProcessBinding;

import org.n52.wps.io.data.IData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsos.richwps.dsl.api.elements.Binding;
import de.hsos.richwps.dsl.api.elements.Execute;
import de.hsos.richwps.dsl.api.elements.IOperation;
import de.hsos.richwps.dsl.api.elements.InReference;
import de.hsos.richwps.dsl.api.elements.OutReference;
import de.hsos.richwps.dsl.api.elements.Reference;
import de.hsos.richwps.dsl.api.elements.VarReference;

public class ExecuteHandler implements IOperationHandler {

    private static Logger LOGGER = LoggerFactory.getLogger(ExecuteHandler.class);

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

        Map<String, Reference> inputReferenceMapping = createInputReferenceMapping(executeOperation.getInputnames(), executeOperation.getInputreferences());
        Map<String, List<IData>> innerProcessInputData = new HashMap<String, List<IData>>();

        for (Map.Entry<String, Reference> inputReferenceMappingEntry : inputReferenceMapping.entrySet()) {
            List<IData> dataForInputReference = this.getInputReferenceValueFromContext(inputReferenceMappingEntry.getValue(), context);
            innerProcessInputData.put(inputReferenceMappingEntry.getKey(), dataForInputReference);
        }

        IProcessBinding processBinding = getProcessBindingForHandle(executeOperation.getHandle(), context);

        // TODO validate algorithm.getProcessDescription() if the bindings have valid data types or if they're incompatible
        Map<String, IData> innerProcessResultData = new HashMap<String, IData>();
        LOGGER.debug("Executing local process with id " + processIdToExecute);
        innerProcessResultData = processBinding.executeProcess(innerProcessInputData, executeOperation.getOutputnames());

        if (innerProcessResultData == null) {
            throw new RuntimeException("The inner process " + processIdToExecute + " returned no result.");
        }

        ArrayList<OutputReferenceMap> outputReferenceMapping = createOutputReferenceMapping(executeOperation.getOutputnames(), executeOperation.getOutputreferences());
        for (OutputReferenceMap outputReferenceMap : outputReferenceMapping) {
            if (!innerProcessResultData.containsKey(outputReferenceMap.getIdentifier())) {
                throw new RuntimeException("No result data found for output identifier " + outputReferenceMap.getIdentifier());
            }
            IData dataForOutputReference = innerProcessResultData.get(outputReferenceMap.getIdentifier());
            addOutputReferenceValueToContext(outputReferenceMap.getReference(), dataForOutputReference, context);
        }

    }

    private IProcessBinding getProcessBindingForHandle(String handle, ProcessingContext context) {
        return context.getProcessBindings().get(handle);
    }

    private List<IData> getInputReferenceValueFromContext(Reference reference,
            ProcessingContext context) {

        if (reference instanceof InReference) {
            if (!context.getInputData().containsKey(reference.getId())) {
                throw new IllegalArgumentException("InReference does not exist: " + reference.getId());
            }
            List<IData> value = context.getInputData().get(reference.getId());
            System.out.println("Resolved " + reference.getId() + " as" + value);
            return value;
        }

        if (reference instanceof VarReference) {
            if (!context.getVariables().containsKey(reference.getId())) {
                throw new IllegalArgumentException("VarReference does not exist: " + reference.getId());
            }
            IData variableData = context.getVariables().get(reference.getId());
            System.out.println("Resolved " + reference.getId() + " as" + variableData);

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

    private Map<String, Reference> createInputReferenceMapping(List<String> names,
            List<Reference> references) {
        Map<String, Reference> referenceMapping = new HashMap<String, Reference>();
        for (int i = 0; i < names.size(); i++) {
            referenceMapping.put(names.get(i), references.get(i));
        }
        return referenceMapping;
    }
    
    private ArrayList<OutputReferenceMap> createOutputReferenceMapping(List<String> names,
            List<Reference> references) {
    	ArrayList<OutputReferenceMap> referenceMapping = new ArrayList<OutputReferenceMap>();
        for (int i = 0; i < names.size(); i++) {
            referenceMapping.add(new OutputReferenceMap(names.get(i), references.get(i)));
        }
        return referenceMapping;
    }

    private String getProcessIdToExecute(Execute executeOperation, ProcessingContext context) {
        return getBinding(executeOperation, context).getProcessId();
    }

    private Binding getBinding(Execute executeOperation,
            ProcessingContext context) {
        Binding binding = context.getBindings().get(executeOperation.getHandle());
        if (binding == null) {
            throw new IllegalStateException("Could not find binding for handle " + executeOperation.getHandle() + ". Maybe the binding declaration is missing for this handle.");
        }
        return binding;
    }

}
