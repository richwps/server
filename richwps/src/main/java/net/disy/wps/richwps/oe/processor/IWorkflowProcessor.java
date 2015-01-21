package net.disy.wps.richwps.oe.processor;

import java.util.Map;

import net.opengis.wps.x100.ExecuteDocument;

import org.n52.wps.io.data.IData;

import de.hsos.richwps.dsl.api.elements.Workflow;

public interface IWorkflowProcessor {

	Map<String, IData> process(ExecuteDocument document, Workflow worksequence);

	ProcessingContext getProcessingContext();

	/**
	 * Executes process and gathers runtime information.
	 * 
	 * @param executeDocument
	 *            the ExecuteDocument
	 * @param workflow
	 *            the Workflow
	 * @return the calculated results of the process
	 * @author faltin
	 */
	Map<String, IData> profileProcess(ExecuteDocument executeDocument,
			Workflow workflow);

}
