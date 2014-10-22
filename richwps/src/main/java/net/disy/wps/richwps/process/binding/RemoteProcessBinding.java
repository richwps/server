package net.disy.wps.richwps.process.binding;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import net.disy.wps.richwps.wpsclient.WpsClient;

import org.apache.commons.lang.Validate;
import org.n52.wps.io.data.IData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsos.richwps.dsl.api.elements.Binding;
import de.hsos.richwps.dsl.api.elements.Endpoint;

public class RemoteProcessBinding implements IProcessBinding {

    private static Logger LOGGER = LoggerFactory.getLogger(RemoteProcessBinding.class);

    private final String processId;

    private WpsClient wpsClient;

    public RemoteProcessBinding(Binding binding) {
        Validate.notNull(binding);
        Validate.isTrue(!binding.isLocal() && binding.getEndpoint() != null,
                "Incorrect remote binding given.");
        this.processId = binding.getProcessId();
        final String wpsUrl = buildWpsUrlFromBinding(binding);
        this.wpsClient = new WpsClient(wpsUrl);
    }

    private String buildWpsUrlFromBinding(Binding binding) {
        Endpoint endpoint = binding.getEndpoint();
        URI uri;
        try {
            uri = new URI(endpoint.getProtocol(), null, endpoint.getHost(), endpoint.getPort(), endpoint.getPath(), null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Could not build WPS url from endpoint " + endpoint, e);
        }
        return uri.toString();
    }

    @Override
    public Map<String, IData> executeProcess(Map<String, List<IData>> inputData, List<String> outputNames) {
        LOGGER.debug("Executing remote WPS process with id " + processId);
        return wpsClient.executeProcess(processId, inputData, outputNames);
    }

}
