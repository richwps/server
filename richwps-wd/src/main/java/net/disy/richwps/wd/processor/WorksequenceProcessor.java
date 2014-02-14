package net.disy.richwps.wd.processor;

import java.util.ArrayList;
import java.util.List;

import net.opengis.wps.x100.ExecuteDocument;

import org.w3c.dom.Document;

import de.hsos.richwps.wd.elements.IOperation;
import de.hsos.richwps.wd.elements.Worksequence;

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
	public Document process(ExecuteDocument document, Worksequence worksequence) {
		ProcessingContext context = new ProcessingContext();
		for (IOperation operation : worksequence) {
			IOperationHandler handler = getHandlerForOperation(operation);
			handler.handleOperation(operation, context);
		}
		// TODO the last execute output determines the response document
		return createDocumentFromProcessingContext(context);
	}
	
	private Document createDocumentFromProcessingContext(
			ProcessingContext context) {
		// TODO Auto-generated method stub
		return null;
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
