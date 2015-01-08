package net.disy.wps.richwps.request;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import net.disy.wps.richwps.oe.processor.ProfilingOutputs;
import net.disy.wps.richwps.oe.processor.TimeMeasurement;
import net.disy.wps.richwps.response.ProfileProcessResponse;
import net.disy.wps.richwps.response.ProfileProcessResponseBuilder;
import net.opengis.ows.x11.ExceptionType;
import net.opengis.wps.x100.DeployProcessDocument;
import net.opengis.wps.x100.DeployProcessDocument.DeployProcess;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteDocument.Execute;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProfileProcessDocument;
import net.opengis.wps.x100.ProfileProcessDocument.ProfileProcess;
import net.opengis.wps.x100.StatusType;
import net.opengis.wps.x100.UndeployProcessDocument;
import net.opengis.wps.x100.UndeployProcessDocument.UndeployProcess;

import org.apache.commons.io.IOUtils;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlString;
import org.n52.wps.client.ExecuteRequestBuilder;
import org.n52.wps.commons.context.ExecutionContext;
import org.n52.wps.commons.context.ExecutionContextFactory;
import org.n52.wps.io.data.IComplexData;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.AbstractTransactionalAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.observerpattern.IObserver;
import org.n52.wps.server.observerpattern.ISubject;
import org.n52.wps.server.request.InputHandler;
import org.n52.wps.server.request.Request;
import org.n52.wps.server.response.Response;
import org.n52.wps.transactional.handler.TransactionalRequestHandler;
import org.n52.wps.transactional.request.DeployProcessRequest;
import org.n52.wps.transactional.request.UndeployProcessRequest;
import org.n52.wps.transactional.response.ITransactionalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 * This implementation represents information about a transferred
 * ProfileProcess-Request.
 * 
 * <p>
 * Furthermore it provides functionality for building the response. Therefore it
 * is callable by a caller where the invoked method handles the procedure to
 * calculate information und building the response with this information. <\p>
 * 
 * @author faltin
 *
 */
public class ProfileProcessRequest extends Request implements IRichWPSRequest,
		IObserver {
	private static Logger LOGGER = LoggerFactory
			.getLogger(ProfileProcessRequest.class);
	protected ProfileProcessDocument profileDoc;
	protected String processId, schema, executionUnit;
	protected ProcessDescriptionType processDescription;
	private ProfileProcessResponseBuilder responseBuilder;
	private Map<String, IData> returnResults;
	private ExecuteDocument execDoc;
	private DeployProcessDocument deployProcessDocument;
	private UndeployProcessDocument undeployProcessDocument;
	private TimeMeasurement timeMeasurement;

	/**
	 * Constructs a new ProfileProcessRequest.
	 * 
	 * @param doc
	 *            the request-document
	 * @throws ExceptionReport
	 * @throws ParserConfigurationException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	public ProfileProcessRequest(Document doc) throws ExceptionReport,
			ParserConfigurationException, TransformerFactoryConfigurationError,
			TransformerException {
		super(doc);
		try {
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			this.profileDoc = ProfileProcessDocument.Factory.parse(doc, option);
			if (profileDoc == null) {
				LOGGER.error("ProfileProcessDocument is null");
				throw new ExceptionReport("Error while parsing post data",
						ExceptionReport.MISSING_PARAMETER_VALUE);
			}
			extractSubmittedExecuteDocument();

			processDescription = profileDoc.getProfileProcess()
					.getProcessDescription();
			processId = processDescription.getIdentifier().getStringValue()
					.trim();
			XmlObject execUnit = profileDoc.getProfileProcess()
					.getExecutionUnit();
			XmlCursor xcur = execUnit.newCursor();
			executionUnit = xcur.getTextValue();
		} catch (XmlException e) {
			throw new ExceptionReport("Error while parsing post data",
					ExceptionReport.MISSING_PARAMETER_VALUE, e);
		}

		responseBuilder = new ProfileProcessResponseBuilder(this);
		LOGGER.info("Profiling of process with processId: " + processId);

	}

	private ITransactionalResponse deployProcess() throws ExceptionReport {
		deployProcessDocument = extractDeployDocument();
		Document deploydocument = (Document) deployProcessDocument.getDomNode();
		DeployProcessRequest deployProcessRequest = new DeployProcessRequest(
				deploydocument);
		TransactionalRequestHandler transactionalRequestHandler = new TransactionalRequestHandler(
				deployProcessRequest);
		return transactionalRequestHandler.handle();
	}

	private ITransactionalResponse undeployProcess() throws ExceptionReport {
		undeployProcessDocument = extractUndeployDocument();
		Document undeploydocument = (Document) undeployProcessDocument
				.getDomNode();
		UndeployProcessRequest undeployProcessRequest = new UndeployProcessRequest(
				undeploydocument);
		TransactionalRequestHandler undeployRequestHandler = new TransactionalRequestHandler(
				undeployProcessRequest);
		return undeployRequestHandler.handle();
	}

	private UndeployProcessDocument extractUndeployDocument() {
		UndeployProcessDocument undeployProcessDocument = UndeployProcessDocument.Factory
				.newInstance();
		UndeployProcess undeployProcess = undeployProcessDocument
				.addNewUndeployProcess();
		ProfileProcess profileProcess = profileDoc.getProfileProcess();
		undeployProcess.setService(profileProcess.getService());
		undeployProcess.setVersion(profileProcess.getVersion());
		if (profileProcess.getLanguage() != null) {
			undeployProcess.setLanguage(profileProcess.getLanguage());
		}
		undeployProcess.addNewProcess();
		undeployProcess
				.getProcess()
				.addNewIdentifier()
				.setStringValue(
						processDescription.getIdentifier().getStringValue());
		undeployProcess.getProcess().setKeepExecutionUnit(false);
		return undeployProcessDocument;
	}

	private DeployProcessDocument extractDeployDocument() {
		DeployProcessDocument deployProcessDocument = DeployProcessDocument.Factory
				.newInstance();
		DeployProcess deployProcess = deployProcessDocument
				.addNewDeployProcess();
		ProfileProcess profileProcess = profileDoc.getProfileProcess();
		deployProcess.setService(profileProcess.getService());
		deployProcess.setVersion(profileProcess.getVersion());
		if (profileProcess.getLanguage() != null) {
			deployProcess.setLanguage(profileProcess.getLanguage());
		}
		deployProcess.setProcessDescription(processDescription);
		deployProcess.setExecutionUnit(XmlString.Factory
				.newValue(executionUnit));
		if (profileProcess.getDeploymentProfileName() != null) {
			deployProcess.setDeploymentProfileName(profileProcess
					.getDeploymentProfileName());
		}
		return deployProcessDocument;
	}

	private ExecuteDocument extractExecuteDocument() {
		ProfileProcess profileProcess = profileDoc.getProfileProcess();
		ExecuteRequestBuilder executeRequestBuilder = new ExecuteRequestBuilder(
				profileProcess.getProcessDescription());
		ExecuteDocument executeDocument = executeRequestBuilder.getExecute();
		Execute execute = executeDocument.getExecute();
		if (profileProcess.getDataInputs() != null) {
			execute.setDataInputs(profileProcess.getDataInputs());
		}
		if (profileProcess.getResponseForm() != null) {
			execute.setResponseForm(profileProcess.getResponseForm());
		}
		if (profileProcess.getLanguage() != null) {
			execute.setLanguage(profileProcess.getLanguage());
		}
		return executeDocument;
	}

	private Document extractSubmittedExecuteDocument()
			throws ParserConfigurationException,
			TransformerFactoryConfigurationError, TransformerException {
		execDoc = extractExecuteDocument();
		return (Document) execDoc.getDomNode();
	}

	/**
	 * Returns the ProfileProcessResponseBuilder.
	 * 
	 * @return the ProfileProcessResponseBuilder.
	 */
	public ProfileProcessResponseBuilder getProfileProcessResponseBuilder() {
		return responseBuilder;
	}

	/**
	 * Returns the ProfileProcess part of the ProfileProcess-Document.
	 * 
	 * @return the ProfileProcess-document-part.
	 */
	public ProfileProcess getProfileProcess() {
		return profileDoc.getProfileProcess();
	}

	/**
	 * Returns the ProfileProcess-Document
	 * 
	 * @return the ProfileProcess-Document.
	 */
	public ProfileProcessDocument getProfileDoc() {
		return profileDoc;
	}

	/**
	 * Returns the process-identifier.
	 * 
	 * @return the process-identifier.
	 */
	public String getProcessId() {
		return processId;
	}

	/**
	 * Returns the schema.
	 * 
	 * @return the schema.
	 */
	public String getSchema() {
		return schema;
	}

	/**
	 * Returns the execution-unit.
	 * 
	 * @return the execution-unit.
	 */
	public String getExecutionUnit() {
		return executionUnit;
	}

	/**
	 * Returns the process-description.
	 * 
	 * @return the process-description.
	 */
	public ProcessDescriptionType getProcessDescription() {
		return processDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.wps.server.request.Request#getAttachedResult()
	 */
	public Map<String, IData> getAttachedResult() {
		return returnResults;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.wps.server.request.Request#call()
	 */
	@Override
	public Response call() throws ExceptionReport {
		IAlgorithm algorithm = null;
		Map<String, List<IData>> inputMap = null;
		try {
			deployProcess();
			ExecutionContext context;
			ProfileProcess profileProcess = profileDoc.getProfileProcess();
			if (profileProcess.isSetResponseForm()) {
				context = profileProcess.getResponseForm().isSetRawDataOutput() ? new ExecutionContext(
						profileProcess.getResponseForm().getRawDataOutput())
						: new ExecutionContext(Arrays.asList(profileProcess
								.getResponseForm().getResponseDocument()
								.getOutputArray()));
			} else {
				context = new ExecutionContext();
			}

			// register so that any function that calls
			// ExecuteContextFactory.getContext() gets the instance registered
			// with this thread
			ExecutionContextFactory.registerContext(context);

			LOGGER.debug("started with execution");

			updateStatusStarted();

			// parse the input
			InputType[] inputs = new InputType[0];
			if (profileProcess.getDataInputs() != null) {
				inputs = profileProcess.getDataInputs().getInputArray();
			}
			InputHandler parser = new InputHandler.Builder(inputs,
					getAlgorithmIdentifier()).build();

			// we got so far:
			// get the algorithm, and run it with the clients input

			/*
			 * IAlgorithm algorithm =
			 * RepositoryManager.getInstance().getAlgorithm
			 * (getAlgorithmIdentifier()); returnResults =
			 * algorithm.run((Map)parser.getParsedInputLayers(),
			 * (Map)parser.getParsedInputParameters());
			 */
			algorithm = RepositoryManager.getInstance().getAlgorithm(
					getAlgorithmIdentifier());

			if (algorithm instanceof ISubject) {
				ISubject subject = (ISubject) algorithm;
				subject.addObserver(this);

			}

			if (algorithm instanceof AbstractTransactionalAlgorithm) {
				Object result = ((AbstractTransactionalAlgorithm) algorithm)
						.profileRun(execDoc);
				ProfilingOutputs profilingOutputs = (ProfilingOutputs) result;
				returnResults = profilingOutputs.getOutputData();
				timeMeasurement = profilingOutputs.getTimeMeasurement();
			} else {
				// TODO Not verified! Verify!
				inputMap = parser.getParsedInputData();
				returnResults = algorithm.run(inputMap);
			}

			List<String> errorList = algorithm.getErrors();
			if (errorList != null && !errorList.isEmpty()) {
				String errorMessage = errorList.get(0);
				LOGGER.error("Error reported while handling ExecuteRequest for "
						+ getAlgorithmIdentifier() + ": " + errorMessage);
				updateStatusError(errorMessage);
			} else {
				updateStatusSuccess();
			}
			undeployProcess();
		} catch (Throwable e) {
			String errorMessage = null;
			if (algorithm != null && algorithm.getErrors() != null
					&& !algorithm.getErrors().isEmpty()) {
				errorMessage = algorithm.getErrors().get(0);
			}
			if (errorMessage == null) {
				errorMessage = e.toString();
			}
			if (errorMessage == null) {
				errorMessage = "UNKNOWN ERROR";
			}
			LOGGER.error("Exception/Error while executing ProfileProcessRequest for "
					+ getAlgorithmIdentifier() + ": " + errorMessage);
			updateStatusError(errorMessage);
			if (e instanceof Error) {
				// This is required when catching Error
				throw (Error) e;
			}
			if (e instanceof ExceptionReport) {
				throw (ExceptionReport) e;
			} else {
				throw new ExceptionReport(
						"Error while executing the embedded process for: "
								+ getAlgorithmIdentifier(),
						ExceptionReport.NO_APPLICABLE_CODE, e);
			}
		} finally {
			// you ***MUST*** call this or else you will have a PermGen
			// ClassLoader memory leak due to ThreadLocal use
			ExecutionContextFactory.unregisterContext();
			if (algorithm instanceof ISubject) {
				((ISubject) algorithm).removeObserver(this);
			}
			if (inputMap != null) {
				for (List<IData> l : inputMap.values()) {
					for (IData d : l) {
						if (d instanceof IComplexData) {
							((IComplexData) d).dispose();
						}
					}
				}
			}
			if (returnResults != null) {
				for (IData d : returnResults.values()) {
					if (d instanceof IComplexData) {
						((IComplexData) d).dispose();
					}
				}
			}
		}
		ProfileProcessResponse profileProcessResponse = new ProfileProcessResponse(
				this);
		return profileProcessResponse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.wps.server.request.Request#validate()
	 */
	@Override
	public boolean validate() throws ExceptionReport {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.n52.wps.server.observerpattern.IObserver#update(org.n52.wps.server
	 * .observerpattern.ISubject)
	 */
	@Override
	public void update(ISubject subject) {
		Object state = subject.getState();
		LOGGER.info("Update received from Subject, state changed to : " + state);
		StatusType status = StatusType.Factory.newInstance();

		int percentage = 0;
		if (state instanceof Integer) {
			percentage = (Integer) state;
			status.addNewProcessStarted().setPercentCompleted(percentage);
		} else if (state instanceof String) {
			status.addNewProcessStarted().setStringValue((String) state);
		}
		updateStatus(status);

	}

	/**
	 * Updates status of process.
	 */
	public void updateStatusStarted() {
		StatusType status = StatusType.Factory.newInstance();
		status.addNewProcessStarted().setPercentCompleted(0);
		updateStatus(status);
	}

	private void updateStatus(StatusType status) {
		responseBuilder.setStatus(status);
		try {
			responseBuilder.update();
			if (isStoreResponse()) {
				ProfileProcessResponse profileProcessResponse = new ProfileProcessResponse(
						this);
				InputStream is = null;
				try {
					is = profileProcessResponse.getAsStream();
					DatabaseFactory.getDatabase().storeResponse(
							getUniqueId().toString(), is);
				} finally {
					IOUtils.closeQuietly(is);
				}
			}
		} catch (ExceptionReport e) {
			LOGGER.error("Update of process status failed.", e);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns <code>true<\code> if the Response has to be stored.
	 * 
	 * @return <code>true<\code> if the Response has to be stored;
	 *         <code>false<\code> otherwise.
	 */
	public boolean isStoreResponse() {
		ProfileProcess profileProcess = profileDoc.getProfileProcess();
		if (profileProcess.getResponseForm() == null) {
			return false;
		}
		if (profileProcess.getResponseForm().getRawDataOutput() != null) {
			return false;
		}
		return profileProcess.getResponseForm().getResponseDocument()
				.getStoreExecuteResponse();
	}

	/**
	 * Returns the identifier of the algorithm.
	 * 
	 * @return the identifier of the algorithm.
	 */
	public String getAlgorithmIdentifier() {
		ProcessDescriptionType processDescription = profileDoc
				.getProfileProcess().getProcessDescription();
		if (processDescription.getIdentifier() != null) {
			return processDescription.getIdentifier().getStringValue();
		}
		return null;
	}

	/**
	 * Updates the status to Error-status.
	 * 
	 * @param errorMessage
	 *            the error message
	 */
	public void updateStatusError(String errorMessage) {
		StatusType status = StatusType.Factory.newInstance();
		net.opengis.ows.x11.ExceptionReportDocument.ExceptionReport excRep = status
				.addNewProcessFailed().addNewExceptionReport();
		excRep.setVersion("1.0.0");
		ExceptionType excType = excRep.addNewException();
		excType.addNewExceptionText().setStringValue(errorMessage);
		excType.setExceptionCode(ExceptionReport.NO_APPLICABLE_CODE);
		updateStatus(status);
	}

	/**
	 * Updates status to Success-status.
	 */
	public void updateStatusSuccess() {
		StatusType status = StatusType.Factory.newInstance();
		status.setProcessSucceeded("Process successful");
		updateStatus(status);
	}

	/**
	 * Updates status to Accepted-status.
	 */
	public void updateStatusAccepted() {
		StatusType status = StatusType.Factory.newInstance();
		status.setProcessAccepted("Process Accepted");
		updateStatus(status);
	}

	/**
	 * Returns <code>true<\code> if the Response has to be returned as raw-data.
	 * 
	 * @return <code>true<\code> if the Response has to be returned as raw-data;
	 *         <code>false<\code> otherwise.
	 */

	public boolean isRawData() {
		ProfileProcess profileProcess = profileDoc.getProfileProcess();
		if (profileProcess.getResponseForm() == null) {
			return false;
		}
		if (profileProcess.getResponseForm().getRawDataOutput() != null) {
			return true;
		} else {
			return false;
		}
	}

}
