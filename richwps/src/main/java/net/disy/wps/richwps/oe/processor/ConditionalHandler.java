package net.disy.wps.richwps.oe.processor;

import de.hsos.richwps.dsl.api.elements.IOperation;

public class ConditionalHandler implements IOperationHandler{

	@Override
	public void handleOperation(IOperation operation, ProcessingContext context) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canHandle(IOperation operation) {
		//return operation instance of Condition
		return false;
	}

}
