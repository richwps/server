package net.disy.wps.richwps.oe.processor;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Stack;

import net.opengis.wps.x100.InputType;

import org.joda.time.DateTime;
import org.n52.wps.server.request.InputHandler;

import de.hsos.richwps.dsl.api.elements.Assignment;
import de.hsos.richwps.dsl.api.elements.Binding;
import de.hsos.richwps.dsl.api.elements.Execute;
import de.hsos.richwps.dsl.api.elements.IfStatement;

/**
 * This implementation holds information about the several examined parts of a
 * profiled process.
 * 
 * <p>
 * It provides functionality for measuring time and gathering information about
 * the examined parts of the profiled process. <\p>
 * 
 * @author faltin
 *
 */
public class TimeMeasurements implements Observer {
	private List<Measurement> measurements;
	private int rolaOperationCounter;
	private int nonRolaOperationCounter;
	private Stack<Measurement> operationStack;
	private final long MILLION = 1000000;
	private String deploymentProfileName;

	/**
	 * Constructs a new TimeMeasurement.
	 */
	public TimeMeasurements() {
		measurements = new ArrayList<TimeMeasurements.Measurement>();
		operationStack = new Stack<TimeMeasurements.Measurement>();
		rolaOperationCounter = 0;
		nonRolaOperationCounter = 1;
	}

	/**
	 * Returns the next Measurement of all Measurements.
	 * 
	 * @return the next Measurement
	 */
	public Iterator<Measurement> getIterator() {
		return measurements.iterator();
	}

	/**
	 * Starts time measurement and gathers information about kind of operation,
	 * identifier for this operation by delivered operation-object.
	 * 
	 * @param operation
	 */
	public void start(Object operation, boolean isRolaOperation) {
		Measurement currentMeasurement = new Measurement();
		operationStack.push(currentMeasurement);
		currentMeasurement.setStartTime(new DateTime());
		if (isRolaOperation) {
			String dslType = null;
			if (deploymentProfileName != null
					&& !deploymentProfileName.isEmpty()) {
				dslType = deploymentProfileName + "-Operation #";
			} else {
				dslType = "Operation #";
			}
			if (operation instanceof Assignment) {
				currentMeasurement.setMeasurementId(dslType
						+ rolaOperationCounter);
				currentMeasurement.setDescription("Assignment: "
						+ ((Assignment) operation).getLefthand().getId()
						+ " = " + ((Assignment) operation).getStringvalue());
			} else if (operation instanceof Binding) {
				currentMeasurement.setMeasurementId(dslType
						+ rolaOperationCounter);
				currentMeasurement.setDescription("Binding: "
						+ ((Binding) operation).getProcessId() + " to "
						+ ((Binding) operation).getHandle());
			} else if (operation instanceof Execute) {
				currentMeasurement.setMeasurementId(dslType
						+ rolaOperationCounter);
				currentMeasurement.setDescription("Execute: "
						+ ((Execute) operation).getHandle());
			} else if (operation instanceof IfStatement) {
				currentMeasurement.setMeasurementId(dslType
						+ rolaOperationCounter);
				currentMeasurement.setDescription("IfStatement: "
						+ ((IfStatement) operation).getLefthand().getId()
						+ ", "
						+ ((IfStatement) operation).getRighthand().getId());
			} else if (operation instanceof String) {
				currentMeasurement.setMeasurementId((String) operation);
			} else {
				currentMeasurement.setMeasurementId(dslType
						+ rolaOperationCounter);
			}
			rolaOperationCounter++;
		} else {
			if (operation instanceof InputType) {
				currentMeasurement.setMeasurementId("Parsing Input "
						+ ((InputType) operation).getIdentifier()
								.getStringValue());
			} else if (operation instanceof String) {
				currentMeasurement.setMeasurementId((String) operation);
			} else {
				currentMeasurement.setMeasurementId("Unknown Measurement #"
						+ nonRolaOperationCounter);
				currentMeasurement.setDescription(operation.getClass()
						.getName());
				nonRolaOperationCounter++;
			}
		}
		currentMeasurement.setElapseStart(System.nanoTime());
	}

	/**
	 * Stops recently started time measurement and saves gathered information.
	 */
	public void stop() {
		Measurement recentlyStartedMeasurement = null;
		try {
			recentlyStartedMeasurement = operationStack.pop();
			recentlyStartedMeasurement.setRuntime(calcRuntime(
					System.nanoTime(),
					recentlyStartedMeasurement.getElapseStart()));
			measurements.add(recentlyStartedMeasurement);
		} catch (EmptyStackException e) {
		}
	}

	/**
	 * Stops recently started time measurement and saves gathered information.
	 * Sets the last Measurement of overall Process at first position of the
	 * data structure.
	 */
	public void fullStop() {
		stop();
		measurements.add(0, measurements.get(measurements.size() - 1));
		measurements.remove(measurements.size() - 1);
	}

	private long calcRuntime(long elapseStop, long elapseStart) {
		return (elapseStop - elapseStart) / MILLION;
	}

	/**
	 * Holds the information about the measurements done by TimeMeasurement.
	 * 
	 * @author faltin
	 *
	 */
	public class Measurement {
		private DateTime starttime;
		private long elapseStart;
		private long runtime;
		private String measurementId;
		private String description;

		/**
		 * Sets the runtime
		 * 
		 * @param calcRuntime
		 *            the runtime
		 */
		public void setRuntime(long calcRuntime) {
			this.runtime = calcRuntime;
		}

		/**
		 * Returns the start time of measurement
		 * 
		 * @return the start time
		 */
		public DateTime getStartTime() {
			return starttime;
		}

		/**
		 * Sets the start time of measurement
		 * 
		 * @param startTime
		 *            the start time
		 */
		public void setStartTime(DateTime startTime) {
			this.starttime = startTime;
		}

		/**
		 * Gets the start time for relative time measurement
		 * 
		 * @return the start time for relative time measurement
		 */
		public long getElapseStart() {
			return elapseStart;
		}

		/**
		 * Sets the start time for relative time measurement
		 * 
		 * @param elapseStart
		 *            the start time for relative time measurement
		 */
		public void setElapseStart(long elapseStart) {
			this.elapseStart = elapseStart;
		}

		/**
		 * Returns the runtime
		 * 
		 * @return the runtime
		 */
		public long getRuntime() {
			return runtime;
		}

		/**
		 * Returns the measurement identifier
		 * 
		 * @return the measurement identifier
		 */
		public String getMeasurementId() {
			return measurementId;
		}

		/**
		 * Sets the measurement identifier
		 * 
		 * @param measurementId
		 *            the measurement identifier
		 */
		public void setMeasurementId(String measurementId) {
			this.measurementId = measurementId;
		}

		/**
		 * Returns the description of measurement
		 * 
		 * @return the description of measurement
		 */
		public String getDescription() {
			return description;
		}

		/**
		 * Sets the description of measurement
		 * 
		 * @param description
		 *            the description of measurement
		 */
		public void setDescription(String description) {
			this.description = description;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof InputHandler) {
			if (arg != null) {
				start(arg, false);
			} else {
				stop();
			}
		} else if (o instanceof WorkflowProcessor) {
			if (arg != null) {
				start(arg, true);
			} else {
				stop();
			}
		}
	}

	/**
	 * Sets the deployment profile name which emerges in the profile description
	 * of the response
	 * 
	 * @param deploymentProfileName
	 *            the deployment profile name
	 */
	public void setDeploymentProfileName(String deploymentProfileName) {
		this.deploymentProfileName = deploymentProfileName;

	}
}
