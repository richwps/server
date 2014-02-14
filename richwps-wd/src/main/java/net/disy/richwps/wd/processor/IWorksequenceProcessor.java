package net.disy.richwps.wd.processor;

import net.opengis.wps.x100.ExecuteDocument;

import org.w3c.dom.Document;

import de.hsos.richwps.dsl.api.elements.Worksequence;


public interface IWorksequenceProcessor {

	Document process(ExecuteDocument document, Worksequence worksequence);
	
}
