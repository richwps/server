package net.disy.wps.richwps.response;

import net.opengis.wps.x100.OutputDescriptionType;
import de.hsos.richwps.dsl.api.elements.OutputReferenceMapping;

public class OutputReferenceDescription {

	private OutputReferenceMapping processOutputOnVariableMapping;
	private OutputDescriptionType outputDescription;

	public OutputReferenceDescription() {

	}

	public OutputReferenceDescription(
			OutputReferenceMapping processOutputOnVariableMapping,
			OutputDescriptionType outputDescription) {
		this.processOutputOnVariableMapping = processOutputOnVariableMapping;
		this.outputDescription = outputDescription;
	}

	public OutputReferenceMapping getProcessOutputOnVariableMapping() {
		return processOutputOnVariableMapping;
	}

	public void setProcessOutputOnVariableMapping(
			OutputReferenceMapping processOutputOnVariableMapping) {
		this.processOutputOnVariableMapping = processOutputOnVariableMapping;
	}

	public OutputDescriptionType getDescription() {
		return outputDescription;
	}

	public void setDescription(OutputDescriptionType description) {
		this.outputDescription = description;
	}

}
