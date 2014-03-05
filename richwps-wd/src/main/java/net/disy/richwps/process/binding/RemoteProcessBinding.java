package net.disy.richwps.process.binding;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.n52.wps.io.data.IData;

import de.hsos.richwps.dsl.api.elements.Binding;
import de.hsos.richwps.dsl.api.elements.Endpoint;

public class RemoteProcessBinding implements IProcessBinding {
	
	private final String processId;
	
	private final String wpsUrl;
	
	public RemoteProcessBinding(Binding binding) {
		Validate.notNull(binding);
		Validate.isTrue(!binding.isLocal() && binding.getEndpoint() != null,
				"Incorrect remote binding given.");
		this.processId = binding.getProcessId();
		this.wpsUrl = buildWpsUrlFromBinding(binding);
	}

	private String buildWpsUrlFromBinding(Binding binding) {
		Endpoint endpoint = binding.getEndpoint();
		URI uri;
		try {
			uri = new URI(endpoint.getProtocol(), null, endpoint.getHost(), endpoint.getPort(), endpoint.getPathToEndPoint(), null, null);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Could not build WPS url from endpoint " + endpoint, e);
		}
		return uri.toString();
	}

	@Override
	public Map<String, IData> executeProcess(Map<String, List<IData>> inputData) {
		// TODO impl
		System.out.println("Using remote WPS Service with url " + wpsUrl);
		System.out.println("Executing remote WPS process with id " + processId);
		return new HashMap<String, IData>();
	}

}
