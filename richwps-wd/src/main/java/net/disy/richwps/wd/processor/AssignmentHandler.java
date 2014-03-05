package net.disy.richwps.wd.processor;

import de.hsos.richwps.dsl.api.elements.Assignment;
import de.hsos.richwps.dsl.api.elements.IOperation;


public class AssignmentHandler implements IOperationHandler {
	
	@Override
	public boolean canHandle(IOperation operation)  {
		return operation instanceof Assignment;
	}

	@Override
	public void handleOperation(IOperation operation, ProcessingContext context) {
		if (!canHandle(operation)) {
			throw new IllegalArgumentException("Could not handle operation as it is not of type " + Assignment.class.getName());
		}
		Assignment assignmentOperation = (Assignment) operation;
		context.getAssignments().add(assignmentOperation);
		// TODO assignments are not yet evaluated
	}

}
