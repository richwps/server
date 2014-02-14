package net.disy.richwps.wd.processor;

import de.hsos.richwps.wd.elements.IOperation;

public interface IOperationHandler {
	
	void handleOperation(IOperation operation, ProcessingContext context);
	
	boolean canHandle(IOperation operation);

}
