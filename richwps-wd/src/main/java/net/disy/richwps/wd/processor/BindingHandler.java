package net.disy.richwps.wd.processor;

import de.hsos.richwps.wd.elements.Binding;
import de.hsos.richwps.wd.elements.IOperation;

public class BindingHandler implements IOperationHandler {

	@Override
	public boolean canHandle(IOperation operation) {
		return operation instanceof Binding;
	}

	@Override
	public void handleOperation(IOperation operation, ProcessingContext context) {
		Binding binding = (Binding) operation;
		context.getBindings().add(binding);
	}

}
