package net.disy.wps.richwps.response;

import net.opengis.wps.x100.OutputDescriptionType;
import de.hsos.richwps.dsl.api.elements.ReferenceOutputMapping;

/**
 * This implementation combines the mapping of output reference on output
 * identifier and the description of the corresponding output identifier.
 * 
 * @author faltin
 *
 */
public class OutputReferenceDescription {

	private ReferenceOutputMapping referenceOutputMapping;
	private OutputDescriptionType outputDescription;

	/**
	 * Creates new OutputReferenceDescription
	 * 
	 * @param referenceOutputMapping
	 *            the mapping of output reference on output identifier
	 * @param outputDescription
	 *            the description of the output identifier
	 */
	public OutputReferenceDescription(
			ReferenceOutputMapping referenceOutputMapping,
			OutputDescriptionType outputDescription) {
		this.referenceOutputMapping = referenceOutputMapping;
		this.outputDescription = outputDescription;
	}

	/**
	 * Returns the ReferenceOutputMapping
	 * 
	 * @return the ReferenceOutputMapping
	 */
	public ReferenceOutputMapping getReferenceOutputMapping() {
		return referenceOutputMapping;
	}

	/**
	 * Returns the OutputDescription
	 * 
	 * @return the OutputDescription
	 */
	public OutputDescriptionType getDescription() {
		return outputDescription;
	}

}
