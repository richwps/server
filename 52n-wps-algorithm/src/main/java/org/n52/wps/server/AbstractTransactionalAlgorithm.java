/**
 * ﻿Copyright (C) 2007 - 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.n52.wps.server;

import java.util.List;
import java.util.Map;
import java.util.Observer;

import net.opengis.wps.x100.ExecuteDocument;

import org.n52.wps.io.data.IData;

public abstract class AbstractTransactionalAlgorithm implements IAlgorithm {

	protected String algorithmID;

	public AbstractTransactionalAlgorithm(String algorithmID) {
		this.algorithmID = algorithmID;

	}

	public String getAlgorithmID() {
		return algorithmID;
	}

	public abstract Map<String, IData> run(ExecuteDocument document);

	/**
	 * Starts testing of the given process.
	 * 
	 * @param document
	 *            the execution document.
	 * @return the results of the calculations.
	 * @author faltin
	 */
	public abstract Map<String, IData> runTest(ExecuteDocument document);

	/**
	 * Starts profiling of the given process.
	 * 
	 * @param execDoc
	 *            the execution document.
	 * @param observers
	 *            the observers observing the processing.
	 * @return the results of the calculations.
	 * @author faltin
	 */
	public abstract Map<String, IData> runProfiling(ExecuteDocument execDoc,
			List<Observer> observers);

}
