package net.disy.richwps.wd.processor;

import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

import de.hsos.richwps.dsl.api.elements.Assignment;
import de.hsos.richwps.dsl.api.elements.IOperation;
import de.hsos.richwps.dsl.api.elements.InReference;
import de.hsos.richwps.dsl.api.elements.OutReference;
import de.hsos.richwps.dsl.api.elements.Reference;
import de.hsos.richwps.dsl.api.elements.VarReference;


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
		
		Reference lefthand = assignmentOperation.getLefthand();
		Reference righthand = assignmentOperation.getRighthand();
		String stringvalue = assignmentOperation.getStringvalue();
		Integer intvalue = assignmentOperation.getIntvalue();
		
		if (lefthand != null && righthand != null) {
			if (lefthand instanceof OutReference && righthand instanceof VarReference) {
				context.getOutputData().put(lefthand.getId(), context.getVariables().get(righthand.getId()));
			}
			if (lefthand instanceof VarReference && righthand instanceof InReference) {
				// TODO: It is possible to have a list of IData elements assigned to one input key
				context.getVariables().put(lefthand.getId(), context.getInputData().get(righthand.getId()).get(0));
			}
		}
		if (lefthand != null && stringvalue != null) {
			context.getVariables().put(lefthand.getId(), new LiteralStringBinding(stringvalue));
		}
		if (lefthand != null && intvalue != null) {
			context.getVariables().put(lefthand.getId(), new LiteralIntBinding(intvalue));
		}
	}
}
