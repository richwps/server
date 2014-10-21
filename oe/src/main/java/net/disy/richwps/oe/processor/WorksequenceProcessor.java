package net.disy.richwps.oe.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.InputType;

import org.apache.commons.lang.Validate;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.request.InputHandler;

import de.hsos.richwps.dsl.api.elements.IOperation;
import de.hsos.richwps.dsl.api.elements.Worksequence;

public class WorksequenceProcessor implements IWorksequenceProcessor {

    private List<IOperationHandler> operationHandlers = new ArrayList<IOperationHandler>();

    public WorksequenceProcessor() {
        initOperationHandlers();
    }

    private void initOperationHandlers() {
        operationHandlers.add(new AssignmentHandler());
        operationHandlers.add(new BindingHandler());
        operationHandlers.add(new ExecuteHandler());
    }

    @Override
    public Map<String, IData> process(ExecuteDocument executeDocument, Worksequence worksequence) {
        Validate.notNull(executeDocument);
        Validate.notNull(worksequence);
        ProcessingContext context = createProcessingContext(executeDocument);

        for (IOperation operation : worksequence) {
            IOperationHandler handler = getHandlerForOperation(operation);
            handler.handleOperation(operation, context);
        }
        return context.getOutputData();
    }

    private ProcessingContext createProcessingContext(ExecuteDocument executeDocument) {
        ProcessingContext context = new ProcessingContext(executeDocument);
        InputType[] outerProcessInputs = executeDocument.getExecute().getDataInputs().getInputArray();
        Map<String, List<IData>> outerProcessInputData = null;
        try {
            InputHandler outerProcessInputHandler = new InputHandler.Builder(
                    outerProcessInputs, executeDocument.getExecute()
                    .getIdentifier().getStringValue()).build();
            outerProcessInputData = outerProcessInputHandler.getParsedInputData();
            context.getInputData().putAll(outerProcessInputData);
        } catch (ExceptionReport e) {
            throw new RuntimeException("Could not parse input data", e);
        }
        return context;
    }

    private IOperationHandler getHandlerForOperation(IOperation operation) {
        for (IOperationHandler handler : operationHandlers) {
            if (handler.canHandle(operation)) {
                return handler;
            }
        }
        return null;
    }

}
