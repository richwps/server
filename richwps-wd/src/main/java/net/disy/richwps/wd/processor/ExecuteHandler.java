package net.disy.richwps.wd.processor;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.RepositoryManager;

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
		
		// TODO consider previously declared bindings from context. If the execute binding id (=handle) isn't declared in binding list, throw an exception
		
		List<Reference> inputReferences = executeOperation.getInputreferences();
		List<String> inputNames = executeOperation.getInputnames();
		
		// TODO build inputs to fit the wps api for invoking the local wps process
		
		// TODO execute local wps process (get binding for handle)
		String processIdToExecute = getProcessIdToExecute(executeOperation, context);
		if (!RepositoryManager.getInstance().containsAlgorithm(processIdToExecute)) {
			throw new IllegalArgumentException(MessageFormat.format("Local process id {0} does not exist.", processIdToExecute));
		}
		IAlgorithm algorithm = RepositoryManager.getInstance().getAlgorithm(processIdToExecute);
		Map<String, List<IData>> inputData = new HashMap<String, List<IData>>();
		
		// TODO need of a sub-context per process invocation?
		
		// TODO make generic according to the input definitions
		inputData.put("LiteralInputData", Arrays.<IData>asList(new LiteralStringBinding("teststring")));
		
		Map<String, IData> localProcessResult = new HashMap<String, IData>();
		try {
			System.out.println("Executing local process with id " + processIdToExecute);
			localProcessResult = algorithm.run(inputData);
		} catch (ExceptionReport e) {
			throw new RuntimeException(e);
		}
		
		List<Reference> outputReferences = executeOperation.getOutputreferences();
		List<String> outputNames = executeOperation.getOutputnames();
		// TODO handle the case that the output gets saved in variables (var prefix) or a final output (out prefix)
		if (localProcessResult != null) {
			for (Reference outputReference : outputReferences) {
				context.getResults().put(outputReference.getId(), localProcessResult.values().iterator().next());	
			}
			
		}
        
	}

	private String getProcessIdToExecute(Execute executeOperation, ProcessingContext context) {
		Binding binding = context.getBindings().get(executeOperation.getHandle());
		if (binding == null) {
			throw new IllegalStateException("Could not find binding for handle " + executeOperation.getHandle() + ". Maybe the binding declaration is missing for this handle.");
		}
		return binding.getProcessId();
	}
	
	

}
