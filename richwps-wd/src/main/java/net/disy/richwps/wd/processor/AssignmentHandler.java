package net.disy.richwps.wd.processor;

import de.hsos.richwps.wd.elements.Assignment;
import de.hsos.richwps.wd.elements.IOperation;

public class AssignmentHandler implements IOperationHandler {
	
	@Override
	public boolean canHandle(IOperation operation)  {
		return operation instanceof Assignment;
	}

	@Override
	public void handleOperation(IOperation operation, ProcessingContext context) {
		Assignment assignmentOperation = (Assignment) operation;
		context.getAssignments().add(assignmentOperation);
	}

}
