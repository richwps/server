package net.disy.wps.richwps.request;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import net.disy.wps.richwps.response.TestProcessResponse;
import net.disy.wps.richwps.response.TestProcessResponseBuilder;
import net.opengis.ows.x11.ExceptionType;
import net.opengis.wps.x100.DeployProcessDocument;
import net.opengis.wps.x100.DeployProcessDocument.DeployProcess;
import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ExecuteDocument.Execute;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.InputType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.StatusType;
import net.opengis.wps.x100.TestProcessDocument;
import net.opengis.wps.x100.TestProcessDocument.TestProcess;
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
import org.xml.sax.SAXException;

import de.hsos.richwps.dsl.api.elements.ReferenceOutputMapping;

/**
 * This implementation represents information about a transferred
 * TestProcessRequest.
 * 
 * <p>
 * Furthermore it provides functionality for building the response. Therefore it
 * is callable by a caller where the invoked method handles the procedure to
 * calculate information und building the response with this information. <\p>
 * 
 * @author faltin
 *
 */
public class TestProcessRequest extends Request implements IRichWPSRequest,
		IObserver {
	private static Logger LOGGER = LoggerFactory
			.getLogger(TestProcessRequest.class);
	protected TestProcessDocument testDoc;
	protected String processId, schema, executionUnit;
	protected ProcessDescriptionType processDescription;
	private TestProcessResponseBuilder responseBuilder;
	private Map<String, IData> returnResults;
	private ExecuteDocument execDoc;
	private DeployProcessDocument deployProcessDocument;
	private UndeployProcessDocument undeployProcessDocument;
	private List<ReferenceOutputMapping> referenceOutputMappings;

	/**
	 * Constructs a new TestProcessRequest.
	 * 
	 * @param doc
	 *            the request document
	 * @throws ExceptionReport
	 * @throws ParserConfigurationException
	 * @throws TransformerFactoryConfigurationError
	 * @throws TransformerException
	 */
	public TestProcessRequest(Document doc) throws ExceptionReport,
			ParserConfigurationException, TransformerFactoryConfigurationError,
			TransformerException, SAXException, IOException {
		super(doc);
		try {
			XmlOptions option = new XmlOptions();
			option.setLoadTrimTextBuffer();
			this.testDoc = TestProcessDocument.Factory.parse(doc, option);
			if (testDoc == null) {
				LOGGER.error("TestProcessDocument is null");
				throw new ExceptionReport("Error while parsing post data",
						ExceptionReport.MISSING_PARAMETER_VALUE);
			}
			extractSubmittedExecuteDocument();

			processDescription = testDoc.getTestProcess()
					.getProcessDescription();
			validate();
			processId = processDescription.getIdentifier().getStringValue()
					.trim();
			XmlObject execUnit = testDoc.getTestProcess().getExecutionUnit();
			XmlCursor xcur = execUnit.newCursor();
			executionUnit = xcur.getTextValue();
		} catch (XmlException e) {
			throw new ExceptionReport("Error while parsing post data",
					ExceptionReport.MISSING_PARAMETER_VALUE, e);
		}

		responseBuilder = new TestProcessResponseBuilder(this);
		LOGGER.info("Test of process with processId: " + processId);
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
		TestProcess testProcess = testDoc.getTestProcess();
		undeployProcess.setService(testProcess.getService());
		undeployProcess.setVersion(testProcess.getVersion());
		if (testProcess.getLanguage() != null) {
			undeployProcess.setLanguage(testProcess.getLanguage());
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
		TestProcess testProcess = testDoc.getTestProcess();
		deployProcess.setService(testProcess.getService());
		deployProcess.setVersion(testProcess.getVersion());
		if (testProcess.getLanguage() != null) {
			deployProcess.setLanguage(testProcess.getLanguage());
		}
		deployProcess.setProcessDescription(processDescription);
		deployProcess.setExecutionUnit(XmlString.Factory
				.newValue(executionUnit));
		if (testProcess.getDeploymentProfileName() != null) {
			deployProcess.setDeploymentProfileName(testProcess
					.getDeploymentProfileName());
		}
		return deployProcessDocument;
	}

	private ExecuteDocument extractExecuteDocument() {
		TestProcess testProcess = testDoc.getTestProcess();
		ExecuteRequestBuilder executeRequestBuilder = new ExecuteRequestBuilder(
				testProcess.getProcessDescription());
		ExecuteDocument executeDocument = executeRequestBuilder.getExecute();
		Execute execute = executeDocument.getExecute();
		if (testProcess.getDataInputs() != null) {
			execute.setDataInputs(testProcess.getDataInputs());
		}
		if (testProcess.getResponseForm() != null) {
			execute.setResponseForm(testProcess.getResponseForm());
		}
		if (testProcess.getLanguage() != null) {
			execute.setLanguage(testProcess.getLanguage());
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
	 * Returns the TestProcessResponseBuilder
	 * 
	 * @return the TestProcessResponseBuilder
	 * 
	 */
	public TestProcessResponseBuilder getTestProcessResponseBuilder() {
		return responseBuilder;
	}

	/**
	 * Returns the TestProcess part of the document
	 * 
	 * @return the TestProcess part
	 */
	public TestProcess getTestProcess() {
		return testDoc.getTestProcess();
	}

	/**
	 * Returns the TestProcessDocument
	 * 
	 * @return the TestProcessDocument
	 */
	public TestProcessDocument getTestDoc() {
		return testDoc;
	}

	/**
	 * Returns the process identifier
	 * 
	 * @return the process identifier
	 */
	public String getProcessId() {
		return processId;
	}

	/**
	 * Returns the schema
	 * 
	 * @return the schema
	 */
	public String getSchema() {
		return schema;
	}

	/**
	 * Returns the execution unit
	 * 
	 * @return the execution unit
	 */
	public String getExecutionUnit() {
		return executionUnit;
	}

	/**
	 * Returns the ProcessDescription part of the document
	 * 
	 * @return the ProcessDescription part
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

	/**
	 * Returns the mappings of output reference on output identifier
	 * 
	 * @return the mappings
	 */
	public List<ReferenceOutputMapping> getOutputReferenceMappings() {
		return referenceOutputMappings;
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
			TestProcess testProcess = testDoc.getTestProcess();
			if (testProcess.isSetResponseForm()) {
				context = testProcess.getResponseForm().isSetRawDataOutput() ? new ExecutionContext(
						testProcess.getResponseForm().getRawDataOutput())
						: new ExecutionContext(Arrays.asList(testProcess
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
			if (testProcess.getDataInputs() != null) {
				inputs = testProcess.getDataInputs().getInputArray();
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
				returnResults = ((AbstractTransactionalAlgorithm) algorithm)
						.runTest(execDoc);
				// FIXME Don't do this cast
				referenceOutputMappings = (List<ReferenceOutputMapping>) ((AbstractTransactionalAlgorithm) algorithm)
						.getReferenceOutputMappings();
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
			LOGGER.error("Exception/Error while executing TestProcessRequest for "
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
		TestProcessResponse testProcessResponse = new TestProcessResponse(this);
		return testProcessResponse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.wps.server.request.Request#validate()
	 */
	@Override
	public boolean validate() throws ExceptionReport {
		TestProcessDocument.TestProcess testProcess = testDoc.getTestProcess();
		if (!testProcess.getVersion().equals(SUPPORTED_VERSION)) {
			throw new ExceptionReport("Specified version is not supported.",
					ExceptionReport.INVALID_PARAMETER_VALUE, "version="
							+ testProcess.getVersion());
		}

		if (processDescription == null) {
			throw new ExceptionReport("No process description supplied.",
					ExceptionReport.MISSING_PARAMETER_VALUE,
					"process description");
		}

		// Get the inputdescriptions of the algorithm
		if (processDescription.getDataInputs() != null) {
			InputDescriptionType[] inputDescs = processDescription
					.getDataInputs().getInputArray();

			// prevent NullPointerException for zero input values in execute
			// request (if only default values are used)
			InputType[] inputs;
			if (testProcess.getDataInputs() == null)
				inputs = new InputType[0];
			else
				inputs = testProcess.getDataInputs().getInputArray();

			// For each input supplied by the client
			for (InputType input : inputs) {
				boolean identifierMatched = false;
				// Try to match the input with one of the descriptions
				for (InputDescriptionType inputDesc : inputDescs) {
					// If found, then process:
					if (inputDesc.getIdentifier().getStringValue()
							.equals(input.getIdentifier().getStringValue())) {
						identifierMatched = true;
						// If it is a literal value,
						if (input.getData() != null
								&& input.getData().getLiteralData() != null) {
							// then check if the desription is also of type
							// literal
							if (inputDesc.getLiteralData() == null) {
								throw new ExceptionReport(
										"Inputtype LiteralData is not supported",
										ExceptionReport.INVALID_PARAMETER_VALUE);
							}
							// literalValue.getDataType ist optional
							if (input.getData().getLiteralData().getDataType() != null) {
								if (inputDesc.getLiteralData() != null)
									if (inputDesc.getLiteralData()
											.getDataType() != null)
										if (inputDesc.getLiteralData()
												.getDataType().getReference() != null)
											if (!input
													.getData()
													.getLiteralData()
													.getDataType()
													.equals(inputDesc
															.getLiteralData()
															.getDataType()
															.getReference())) {
												throw new ExceptionReport(
														"Specified dataType is not supported "
																+ input.getData()
																		.getLiteralData()
																		.getDataType()
																+ " for input "
																+ input.getIdentifier()
																		.getStringValue(),
														ExceptionReport.INVALID_PARAMETER_VALUE);
											}
							}
						}
						break;
					}
				}
				// if the identifier did not match one of the descriptions, it
				// is
				// invalid
				if (!identifierMatched) {
					throw new ExceptionReport("Input Identifier is not valid: "
							+ input.getIdentifier().getStringValue(),
							ExceptionReport.INVALID_PARAMETER_VALUE,
							"input identifier");
				}
			}
		}
		return true;
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
	 * Updates status to started.
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
				TestProcessResponse testProcessResponse = new TestProcessResponse(
						this);
				InputStream is = null;
				try {
					is = testProcessResponse.getAsStream();
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
		TestProcess testProcess = testDoc.getTestProcess();
		if (testProcess.getResponseForm() == null) {
			return false;
		}
		if (testProcess.getResponseForm().getRawDataOutput() != null) {
			return false;
		}
		return testProcess.getResponseForm().getResponseDocument()
				.getStoreExecuteResponse();
	}

	/**
	 * Returns the identifier of the algorithm.
	 * 
	 * @return the identifier of the algorithm.
	 */

	public String getAlgorithmIdentifier() {
		ProcessDescriptionType processDescription = testDoc.getTestProcess()
				.getProcessDescription();
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
		TestProcess testProcess = testDoc.getTestProcess();
		if (testProcess.getResponseForm() == null) {
			return false;
		}
		if (testProcess.getResponseForm().getRawDataOutput() != null) {
			return true;
		} else {
			return false;
		}
	}
}
