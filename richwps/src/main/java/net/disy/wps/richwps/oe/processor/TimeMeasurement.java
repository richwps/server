package net.disy.wps.richwps.oe.processor;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralDateTimeBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

import de.hsos.richwps.dsl.api.elements.Assignment;
import de.hsos.richwps.dsl.api.elements.Binding;
import de.hsos.richwps.dsl.api.elements.Execute;
import de.hsos.richwps.dsl.api.elements.IOperation;
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
public class TimeMeasurement {
	private Measurement currentMeasurement;
	private List<Measurement> measurements;

	/**
	 * Constructs a new TimeMeasurement.
	 */
	public TimeMeasurement() {
		measurements = new ArrayList<TimeMeasurement.Measurement>();
	}

	/**
	 * Starts time measurement and gathers information about kind of operation,
	 * identifier for this operation by delivered operation-object.
	 * 
	 * @param operation
	 */
	public void start(IOperation operation) {
		if (currentMeasurement == null) {

			currentMeasurement = new Measurement();
			currentMeasurement.setStartTime(new DateTime());
			currentMeasurement.setMeasurementId("unknown");

			if (operation instanceof Assignment) {
				currentMeasurement.setAssignment((Assignment) operation);
				currentMeasurement.setMeasurementId("Assignment: "
						+ currentMeasurement.getAssignment().getStringvalue());
			}
			if (operation instanceof Binding) {
				currentMeasurement.setBinding((Binding) operation);
				currentMeasurement.setMeasurementId("Binding: "
						+ currentMeasurement.getBinding().getProcessId()
						+ " to " + currentMeasurement.getBinding().getHandle());
			}
			if (operation instanceof Execute) {
				currentMeasurement.setExecute((Execute) operation);
				currentMeasurement.setMeasurementId("Execute: "
						+ currentMeasurement.getExecute().getHandle());
			}
			if (operation instanceof IfStatement) {
				currentMeasurement.setIfStatement((IfStatement) operation);
				currentMeasurement.setMeasurementId("IfStatement: "
						+ currentMeasurement.getIfStatement().getLefthand()
								.getId()
						+ ", "
						+ currentMeasurement.getIfStatement().getRighthand()
								.getId());
			}
			currentMeasurement.setElapseStart(System.nanoTime());
		}
	}

	/**
	 * Stops recently started time measurement and saves gathered information.
	 */
	public void stop() {
		if (currentMeasurement != null) {
			long elapseStop = System.nanoTime();
			currentMeasurement.calcRuntime(elapseStop);
			measurements.add(currentMeasurement);
			currentMeasurement = null;
		}
	}

	/**
	 * Holds the information about the measurements done by TimeMeasurement.
	 * 
	 * @author faltin
	 *
	 */
	public class Measurement {
		private Assignment assignment;
		private Binding binding;
		private Execute execute;
		private IfStatement ifStatement;
		private DateTime starttime;
		private long elapseStart;
		private double runtime;
		private String description;
		List<IData> dataObjects;

		private final long THOUSAND = 1000;

		public Measurement() {
			dataObjects = new ArrayList<IData>();
		}

		private void buildDataObjects() {
			dataObjects.add(new LiteralDateTimeBinding(starttime.toDate()));
			dataObjects.add(new LiteralStringBinding(description));
			dataObjects.add(new LiteralDoubleBinding(runtime));
		}

		public Assignment getAssignment() {
			return assignment;
		}

		public void setAssignment(Assignment assignment) {
			this.assignment = assignment;
		}

		public Binding getBinding() {
			return binding;
		}

		public void setBinding(Binding binding) {
			this.binding = binding;
		}

		public Execute getExecute() {
			return execute;
		}

		public void setExecute(Execute execute) {
			this.execute = execute;
		}

		public IfStatement getIfStatement() {
			return ifStatement;
		}

		public void setIfStatement(IfStatement ifStatement) {
			this.ifStatement = ifStatement;
		}

		public DateTime getStartTime() {
			return starttime;
		}

		public void setStartTime(DateTime startTime) {
			this.starttime = startTime;
		}

		public long getElapseStart() {
			return elapseStart;
		}

		public void setElapseStart(long elapseStart) {
			this.elapseStart = elapseStart;
		}

		public double getRuntime() {
			return runtime;
		}

		public void calcRuntime(long elapseStop) {
			runtime = (elapseStop - elapseStart) / THOUSAND;
			buildDataObjects();
		}

		public String getMeasurementId() {
			return description;
		}

		public void setMeasurementId(String measurementId) {
			this.description = measurementId;
		}

		public List<Measurement> getMeasurements() {
			return measurements;
		}

	}
}
