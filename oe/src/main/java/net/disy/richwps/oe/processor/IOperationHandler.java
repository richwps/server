package net.disy.richwps.oe.processor;

import de.hsos.richwps.dsl.api.elements.IOperation;


public interface IOperationHandler {
	
	void handleOperation(IOperation operation, ProcessingContext context);
	
	boolean canHandle(IOperation operation);

}
