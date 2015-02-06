package net.disy.wps.richwps.oe.processor;

import de.hsos.richwps.dsl.api.elements.Reference;

public class OutputReferenceMap {

	private String identifier;
	private Reference reference;
	
	public OutputReferenceMap(String identifier, Reference reference) {
		this.identifier = identifier;
		this.reference = reference;
	}

	public String getIdentifier() {
		return this.identifier;
	}
	
	public Reference getReference () {
		return this.reference;
	}
	
}
