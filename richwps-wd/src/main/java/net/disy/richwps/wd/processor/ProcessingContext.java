package net.disy.richwps.wd.processor;

import java.util.ArrayList;
import java.util.List;

import de.hsos.richwps.wd.elements.Assignment;
import de.hsos.richwps.wd.elements.Binding;
import de.hsos.richwps.wd.elements.Execute;

public class ProcessingContext {
	
	private List<Binding> bindings = new ArrayList<Binding>();
	
	private List<Execute> executes = new ArrayList<Execute>();
	
	private List<Assignment> assignments = new ArrayList<Assignment>();

	public List<Binding> getBindings() {
		return bindings;
	}

	public List<Execute> getExecutes() {
		return executes;
	}

	public List<Assignment> getAssignments() {
		return assignments;
	}
	
}
