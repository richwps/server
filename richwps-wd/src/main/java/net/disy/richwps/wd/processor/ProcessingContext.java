package net.disy.richwps.wd.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.disy.richwps.wpsclient.IProcessBinding;
import net.opengis.wps.x100.ExecuteDocument;

import org.apache.commons.lang.Validate;
import org.n52.wps.io.data.IData;

import de.hsos.richwps.dsl.api.elements.Assignment;
import de.hsos.richwps.dsl.api.elements.Binding;


public class ProcessingContext {
	
	private Map<String, Binding> bindings = new HashMap<String, Binding>();
	
	private Map<String, IProcessBinding> processBindings = new HashMap<String, IProcessBinding>();
	
	private List<Assignment> assignments = new ArrayList<Assignment>();

	private final ExecuteDocument executeDocument;
	
	private final Map<String, IData> variables = new HashMap<String, IData>();
	
	private final Map<String, List<IData>> inputData = new HashMap<String, List<IData>>();
	
	private final Map<String, IData> outputData = new HashMap<String, IData>();	
	
	
	public ProcessingContext(ExecuteDocument executeDocument) {
		Validate.notNull(executeDocument);
		this.executeDocument = executeDocument;
	}

	public Map<String, Binding> getBindings() {
		return bindings;
	}

	public Map<String, IProcessBinding> getProcessBindings() {
		return processBindings;
	}

	public List<Assignment> getAssignments() {
		return assignments;
	}
	
	public ExecuteDocument getExecuteDocument() {
		return executeDocument;
	}

	public Map<String, IData> getVariables() {
		return variables;
	}

	public Map<String, List<IData>> getInputData() {
		return inputData;
	}

	public Map<String, IData> getOutputData() {
		return outputData;
	}
	
}
