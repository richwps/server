package net.disy.wps.richwps.oe.processor;

import net.disy.wps.richwps.process.binding.IProcessBinding;
import net.disy.wps.richwps.process.binding.LocalProcessBinding;
import net.disy.wps.richwps.process.binding.RemoteProcessBinding;
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
		Binding binding = (Binding) operation;
		context.getBindings().put(binding.getHandle(), binding);
		IProcessBinding processBinding = createProcessBinding(binding);
		context.getProcessBindings().put(binding.getHandle(), processBinding);
	}
	
	private IProcessBinding createProcessBinding(Binding binding) {
		if (binding.isLocal()) {
			return new LocalProcessBinding(binding);
		}
		return new RemoteProcessBinding(binding);
	}

}
