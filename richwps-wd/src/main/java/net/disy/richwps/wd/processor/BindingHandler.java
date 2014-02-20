package net.disy.richwps.wd.processor;

import de.hsos.richwps.dsl.api.elements.Binding;
import de.hsos.richwps.dsl.api.elements.IOperation;


public class BindingHandler implements IOperationHandler {

	@Override
	public boolean canHandle(IOperation operation) {
		return operation instanceof Binding;
	}

	@Override
	public void handleOperation(IOperation operation, ProcessingContext context) {
		if (!canHandle(operation)) {
			throw new IllegalArgumentException("Could not handle operation as it is not of type " + Binding.class.getName());
		}
		Binding binding = (Binding) operation; // TODO check if binding already exists
		context.getBindings().put(binding.getHandle(), binding);
	}

}
