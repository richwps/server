package net.disy.richwps.wd.processor;

import de.hsos.richwps.wd.elements.Execute;
import de.hsos.richwps.wd.elements.IOperation;

public class ExecuteHandler implements IOperationHandler {

	@Override
	public boolean canHandle(IOperation operation) {
		return operation instanceof Execute;
	}

	@Override
	public void handleOperation(IOperation operation, ProcessingContext context) {
		Execute executeOperation = (Execute) operation;
		context.getExecutes().add(executeOperation);
	}

}
