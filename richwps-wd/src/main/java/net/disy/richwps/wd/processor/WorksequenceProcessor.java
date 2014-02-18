package net.disy.richwps.wd.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ExecuteDocument;

import org.n52.wps.io.data.IData;

import de.hsos.richwps.dsl.api.elements.IOperation;
import de.hsos.richwps.dsl.api.elements.Worksequence;


public class WorksequenceProcessor implements IWorksequenceProcessor {
	
	private List<IOperationHandler> operationHandlers = new ArrayList<IOperationHandler>();
	
	public WorksequenceProcessor() {
		initOperationHandlers();
	}

	private void initOperationHandlers() {
		operationHandlers.add(new AssignmentHandler());
		operationHandlers.add(new BindingHandler());
		operationHandlers.add(new ExecuteHandler());
	}
	
	@Override
	public Map<String, IData> process(ExecuteDocument executeDocument, Worksequence worksequence) {
		ProcessingContext context = new ProcessingContext(executeDocument);
		for (IOperation operation : worksequence) {
			IOperationHandler handler = getHandlerForOperation(operation);
			handler.handleOperation(operation, context);
		}
		return createResultsFromProcessingContext(context);
	}
	
	private Map<String, IData> createResultsFromProcessingContext(
			ProcessingContext context) { 
		return context.getResults();
	}

	private IOperationHandler getHandlerForOperation(IOperation operation) {
		for (IOperationHandler handler : operationHandlers) {
			if (handler.canHandle(operation)) {
				return handler;
			}
		}
		return null;
	}

}
