package net.disy.richwps.wd.processor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ExecuteDocument;

import org.n52.wps.io.data.IData;

import de.hsos.richwps.dsl.api.elements.Assignment;
import de.hsos.richwps.dsl.api.elements.Binding;
import de.hsos.richwps.dsl.api.elements.Execute;


public class ProcessingContext {
	
	private Map<String, Binding> bindings = new HashMap<String, Binding>();
	
	private List<Execute> executes = new ArrayList<Execute>();
	
	private List<Assignment> assignments = new ArrayList<Assignment>();

	private final ExecuteDocument executeDocument;
	
	private final Map<String, IData> results = new HashMap<String, IData>();
	
	public ProcessingContext(ExecuteDocument executeDocument) {
		this.executeDocument = executeDocument;
	}
	
	public List<Execute> getExecutes() {
		return executes;
	}

	public Map<String, Binding> getBindings() {
		return bindings;
	}

	public List<Assignment> getAssignments() {
		return assignments;
	}
	
	public ExecuteDocument getExecuteDocument() {
		return executeDocument;
	}

	public Map<String, IData> getResults() {
		return results;
	}
	
}
