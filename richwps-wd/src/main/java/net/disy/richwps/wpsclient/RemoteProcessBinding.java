package net.disy.richwps.wpsclient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;

import de.hsos.richwps.dsl.api.elements.Binding;

public class RemoteProcessBinding implements IProcessBinding {
	
	private final String processId;
	
	public RemoteProcessBinding(Binding binding) {
		this.processId = binding.getProcessId();
		// TODO build remote wps client logic that uses the binding argument for the needed values
	}

	@Override
	public Map<String, IData> executeProcess(Map<String, List<IData>> inputData) {
		// TODO impl
		return new HashMap<String, IData>();
	}

}
