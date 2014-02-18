package net.disy.richwps.wd.processor;

import java.util.Map;

import net.opengis.wps.x100.ExecuteDocument;

import org.n52.wps.io.data.IData;

import de.hsos.richwps.dsl.api.elements.Worksequence;


public interface IWorksequenceProcessor {

	Map<String, IData> process(ExecuteDocument document, Worksequence worksequence);
	
}
