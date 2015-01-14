/***************************************************************
 This implementation provides a framework to publish processes to the
web through the  OGC Web Processing Service interface. The framework 
is extensible in terms of processes and data handlers. 

 Copyright (C) 2006 by con terra GmbH

 Authors: 
	Bastian Schaeffer, Institute for Geoinformatics, Muenster, Germany

 Contact: Albert Remke, con terra GmbH, Martin-Luther-King-Weg 24,
 48155 Muenster, Germany, 52n@conterra.de

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt); if not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA or visit the web page of the Free
 Software Foundation, http://www.fsf.org.

 ***************************************************************/

package org.n52.wps.transactional.deploy;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import net.opengis.wps.x100.ExecuteDocument;

import org.n52.wps.io.data.IData;
import org.n52.wps.transactional.request.DeployProcessRequest;
import org.n52.wps.transactional.request.UndeployProcessRequest;

public interface IProcessManager {

	boolean unDeployProcess(UndeployProcessRequest request) throws Exception;

	boolean containsProcess(String processID) throws Exception;

	Collection<String> getAllProcesses() throws Exception;

	Map<String, IData> invoke(ExecuteDocument payload, String algorithmID)
			throws Exception;

	boolean deployProcess(DeployProcessRequest request) throws Exception;

	/**
	 * Performs the test of the given process.
	 * 
	 * @param payload
	 *            the execution document.
	 * @param algorithmID
	 *            the process identifier.
	 * @return the results of the calculation.
	 * @throws Exception
	 */
	Map<String, IData> invokeTest(ExecuteDocument document, String algorithmID)
			throws Exception;

	/**
	 * Returns the output reference on output identifier mappings.
	 * 
	 * @return the output reference on output identifier mappings.
	 */
	Object getReferenceOutputMappings();

	/**
	 * Performs the profiling of the given process.
	 * 
	 * @param document
	 *            the execution document.
	 * @param algorithmID
	 *            the process identifier.
	 * @param observers
	 *            the observers of the calculation process.
	 * @return the results of the calculation.
	 * @throws Exception
	 */
	Map<String, IData> invokeProfiling(ExecuteDocument document,
			String algorithmID, List<Observer> observers) throws Exception;

}
