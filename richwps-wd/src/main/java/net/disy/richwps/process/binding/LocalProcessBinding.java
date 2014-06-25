package net.disy.richwps.process.binding;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.RepositoryManager;

import de.hsos.richwps.dsl.api.elements.Binding;

public class LocalProcessBinding implements IProcessBinding {

    private final String processId;

    public LocalProcessBinding(Binding binding) {
        this.processId = binding.getProcessId();
    }

    @Override
    public Map<String, IData> executeProcess(
            Map<String, List<IData>> inputData, List<String> outputNames) {

        if (!RepositoryManager.getInstance().containsAlgorithm(processId)) {
            throw new IllegalArgumentException(MessageFormat.format("Local process id '{0}' does not exist.", processId));
        }
        IAlgorithm algorithm = RepositoryManager.getInstance().getAlgorithm(processId);
        try {
            return algorithm.run(inputData);
        } catch (ExceptionReport e) {
            throw new RuntimeException(e);
        }
    }

}
