package net.disy.wps.richwps.oe.processor;

import java.util.Map;

import org.n52.wps.io.data.IData;

/**
 * This data structure holds information about a profiling of a process.
 * 
 * <p>
 * Besides the output data of the profiled process there is another element
 * which contains information of runtime information of the parts of the process
 * and the process itself. <\p>
 * 
 * @author faltin
 *
 */
public class ProfilingOutputs {
	private final TimeMeasurements timeMeasurement;
	private final Map<String, IData> outputData;

	/**
	 * Constructs an new ProfilingOutput with an TimeMeasurement object
	 * containing runtime information and a map containing process output data.
	 * 
	 * @param timeMeasurement
	 *            the Object containing runtime-information.
	 * @param outputData
	 *            the process output data.
	 */
	public ProfilingOutputs(TimeMeasurements timeMeasurement,
			Map<String, IData> outputData) {
		this.timeMeasurement = timeMeasurement;
		this.outputData = outputData;
	}

	/**
	 * Returns the runtime information object.
	 * 
	 * @return the runtime information object.
	 */
	public TimeMeasurements getTimeMeasurement() {
		return timeMeasurement;
	}

	/**
	 * Returns the process output data.
	 * 
	 * @return the process output data.
	 */
	public Map<String, IData> getOutputData() {
		return outputData;
	}

}
