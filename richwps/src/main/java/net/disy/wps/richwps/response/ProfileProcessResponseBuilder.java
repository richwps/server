package net.disy.wps.richwps.response;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import net.disy.wps.richwps.request.ProfileProcessRequest;
import net.opengis.ows.x11.DomainMetadataType;
import net.opengis.ows.x11.LanguageStringType;
import net.opengis.wps.x100.DataInputsType;
import net.opengis.wps.x100.DocumentOutputDefinitionType;
import net.opengis.wps.x100.ExecuteResponseDocument;
import net.opengis.wps.x100.ExecuteResponseDocument.ExecuteResponse;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputDefinitionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;
import net.opengis.wps.x100.ProfileProcessResponseDocument;
import net.opengis.wps.x100.ProfileProcessResponseDocument.ProfileProcessResponse;
import net.opengis.wps.x100.StatusType;

import org.apache.xmlbeans.XmlCursor;
import org.n52.wps.io.data.IBBOXData;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.CapabilitiesConfiguration;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.server.WebProcessingService;
import org.n52.wps.server.database.DatabaseFactory;
import org.n52.wps.server.request.Request;
import org.n52.wps.server.response.OutputDataItem;
import org.n52.wps.server.response.RawData;
import org.n52.wps.util.XMLBeansHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hsos.richwps.dsl.api.elements.OutputReferenceMapping;

/**
 * This implementation provides functionality for building the Response on a
 * ProfileProcessRequest. It holds, gathers and generates all necessary data to
 * create the Response.
 * 
 * 
 * @author faltin
 *
 */
public class ProfileProcessResponseBuilder {
	private static Logger LOGGER = LoggerFactory
			.getLogger(ProfileProcessResponseBuilder.class);

	private String identifier;
	private DataInputsType dataInputs;
	protected ProfileProcessResponseDocument profileProcessResponseDocument;
	private ExecuteResponseDocument executeResponseDocument;
	private ProfileProcessRequest request;
	private RawData rawDataElement;
	private ProcessDescriptionType description;
	private Calendar creationTime;
	private List<OutputReferenceDescription> varOutDescs;

	/**
	 * Constructs a new ProfileProcessResponseBuilder
	 * 
	 * @param request
	 *            the ProfileProcessRequest
	 */
	public ProfileProcessResponseBuilder(ProfileProcessRequest request) {
		this.request = request;
		description = this.request.getProcessDescription();
		identifier = description.getIdentifier().getStringValue().trim();
		if (description == null) {
			throw new RuntimeException(
					"Error while accessing the process description for "
							+ identifier);
		}
		profileProcessResponseDocument = ProfileProcessResponseDocument.Factory
				.newInstance();
		profileProcessResponseDocument.addNewProfileProcessResponse();
		XmlCursor c = profileProcessResponseDocument.newCursor();
		c.toFirstChild();
		c.toLastAttribute();
		c.setAttributeText(new QName(
				XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "schemaLocation"),
				"./wpsProfileProcess_response.xsd");
		ProfileProcessResponse profileProcessResponse = profileProcessResponseDocument
				.getProfileProcessResponse();
		profileProcessResponse
				.setServiceInstance(CapabilitiesConfiguration.WPS_ENDPOINT_URL
						+ "?REQUEST=GetCapabilities&SERVICE=WPS");
		profileProcessResponse.setLang(WebProcessingService.DEFAULT_LANGUAGE);
		profileProcessResponse.setService("WPS");
		profileProcessResponse.setVersion(Request.SUPPORTED_VERSION);

		profileProcessResponse.addNewProcess();
		profileProcessResponse.getProcess().addNewIdentifier()
				.setStringValue(identifier);
		profileProcessResponse.getProcess().setProcessVersion(
				description.getProcessVersion());
		profileProcessResponse.getProcess().setTitle(description.getTitle());
		initializeExecuteResponseDocument();
		creationTime = Calendar.getInstance();
	}

	private void initializeExecuteResponseDocument() {

		executeResponseDocument = ExecuteResponseDocument.Factory.newInstance();
		executeResponseDocument.addNewExecuteResponse();
		XmlCursor c = executeResponseDocument.newCursor();
		c.toFirstChild();
		c.toLastAttribute();
		c.setAttributeText(
				new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
						"schemaLocation"),
				"http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_response.xsd");
		executeResponseDocument.getExecuteResponse().setServiceInstance(
				CapabilitiesConfiguration.WPS_ENDPOINT_URL
						+ "?REQUEST=GetCapabilities&SERVICE=WPS");
		executeResponseDocument.getExecuteResponse().setLang(
				WebProcessingService.DEFAULT_LANGUAGE);
		executeResponseDocument.getExecuteResponse().setService("WPS");
		executeResponseDocument.getExecuteResponse().setVersion(
				Request.SUPPORTED_VERSION);

		ExecuteResponse responseElem = executeResponseDocument
				.getExecuteResponse();
		responseElem.addNewProcess().addNewIdentifier()
				.setStringValue(identifier);
		responseElem.getProcess().setTitle(description.getTitle());
		responseElem.getProcess().setProcessVersion(
				description.getProcessVersion());
	}

	/**
	 * Returns the Response.
	 * 
	 * @return the Response
	 * @throws ExceptionReport
	 */
	public InputStream getAsStream() throws ExceptionReport {
		if (request.isRawData() && rawDataElement != null) {
			return rawDataElement.getAsStream();
		}
		if (request.isStoreResponse()) {
			String id = request.getUniqueId().toString();
			String statusLocation = DatabaseFactory.getDatabase()
					.generateRetrieveResultURL(id);
			profileProcessResponseDocument.getProfileProcessResponse()
					.setStatusLocation(statusLocation);
		}
		try {
			return profileProcessResponseDocument.newInputStream(XMLBeansHelper
					.getXmlOptions());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Updates the status of the processing. If status is success the outputs
	 * are parsed. If Response has to be stored the location of the response is
	 * set.
	 * 
	 * @throws ExceptionReport
	 */

	public void update() throws ExceptionReport {

		net.opengis.wps.x100.ProfileProcessResponseDocument.ProfileProcessResponse profileProcessResponseElem = profileProcessResponseDocument
				.getProfileProcessResponse();
		ExecuteResponse executeResponseElem = executeResponseDocument
				.getExecuteResponse();

		// if status succeeded, update reponse with result
		if (profileProcessResponseElem.getStatus().isSetProcessSucceeded()) {
			// FIXME Importing WPSConfiguration produces weird behaviour. (No
			// autocompletion available)

			// the response only include dataInputs, if the property is set to
			// true;
			// if(Boolean.getBoolean(WPSConfiguration.getInstance().getProperty(WebProcessingService.PROPERTY_NAME_INCLUDE_DATAINPUTS_IN_RESPONSE)))
			// {
			// if (new
			// Boolean(WPSConfig.getInstance().getWPSConfig().getServer()
			// .getIncludeDataInputsInResponse())) {
			dataInputs = request.getProfileProcess().getDataInputs();
			profileProcessResponseElem.setDataInputs(dataInputs);
			executeResponseElem.setDataInputs(dataInputs);
			// }
			profileProcessResponseElem.addNewProcessOutputs();
			executeResponseElem.addNewProcessOutputs();
			// has the client specified the outputs?
			varOutDescs = new ArrayList<OutputReferenceDescription>();
			List<OutputReferenceMapping> processOutputsOnVariablesMapping = null;
			OutputDescriptionType[] outputDescs = description
					.getProcessOutputs().getOutputArray();
			if (request.getProfileProcess().isSetResponseForm()) {
				// Get the outputdescriptions from the algorithm

				for (int i = 0; i < processOutputsOnVariablesMapping.size(); i++) {
					String outputIdentifier = processOutputsOnVariablesMapping
							.get(i).getOutputIdentifier();
					// Get OutputDescriptions of current Process
					OutputDescriptionType[] descs = RepositoryManager
							.getInstance()
							.getProcessDescription(
									processOutputsOnVariablesMapping.get(i)
											.getProcessId())
							.getProcessOutputs().getOutputArray();
					for (OutputDescriptionType desc : descs) {
						if (desc.getIdentifier().getStringValue()
								.equals(outputIdentifier)) {
							varOutDescs.add(new OutputReferenceDescription(
									processOutputsOnVariablesMapping.get(i),
									desc));
						}
					}
				}

				if (request.isRawData()) {
					// TODO Not verified! Verify!
					OutputDefinitionType rawDataOutput = request
							.getProfileProcess().getResponseForm()
							.getRawDataOutput();
					String definedOutputId = rawDataOutput.getIdentifier()
							.getStringValue();
					OutputDescriptionType desc = XMLBeansHelper.findOutputByID(
							definedOutputId, outputDescs);
					if (desc.isSetComplexOutput()) {
						String encoding = ProfileProcessResponseBuilder
								.getEncoding(desc, rawDataOutput);
						String schema = ProfileProcessResponseBuilder
								.getSchema(desc, rawDataOutput);
						String responseMimeType = getMimeType(rawDataOutput,
								null);
						generateComplexDataOutput(definedOutputId, false, true,
								schema, responseMimeType, encoding, null);
					}

					else if (desc.isSetLiteralOutput()) {
						String mimeType = null;
						String schema = null;
						String encoding = null;
						DomainMetadataType dataType = desc.getLiteralOutput()
								.getDataType();
						String reference = dataType != null ? dataType
								.getReference() : null;
						generateLiteralDataOutput(definedOutputId, true,
								reference, schema, mimeType, encoding,
								desc.getTitle());
					} else if (desc.isSetBoundingBoxOutput()) {
						generateBBOXOutput(definedOutputId, true,
								desc.getTitle());
					}
					return;
				}
				// Get the outputdefinitions from the clients request
				// For each request of output
				for (int i = 0; i < request.getProfileProcess()
						.getResponseForm().getResponseDocument()
						.getOutputArray().length; i++) {
					OutputDefinitionType definition = request
							.getProfileProcess().getResponseForm()
							.getResponseDocument().getOutputArray(i);
					String definedOutputId = definition.getIdentifier()
							.getStringValue();
					OutputReferenceDescription varOutDescription = getVarOutDescriptionOfDefinedOutput(definedOutputId);
					OutputDescriptionType desc = null;
					if (varOutDescription != null) {
						desc = getDescOfVariable(definedOutputId);
					} else {
						desc = XMLBeansHelper.findOutputByID(definedOutputId,
								outputDescs);
					}
					if (desc == null) {
						throw new ExceptionReport(
								"Could not find the output id "
										+ definedOutputId,
								ExceptionReport.INVALID_PARAMETER_VALUE);
					}
					if (desc.isSetComplexOutput()) {

						String mimeType = getMimeType(definition,
								varOutDescription);
						String schema = ProfileProcessResponseBuilder
								.getSchema(desc, definition);
						String encoding = ProfileProcessResponseBuilder
								.getEncoding(desc, definition);

						generateComplexDataOutput(definedOutputId,
								((DocumentOutputDefinitionType) definition)
										.getAsReference(), false, schema,
								mimeType, encoding, desc.getTitle());
					} else if (desc.isSetLiteralOutput()) {
						String mimeType = null;
						String schema = null;
						String encoding = null;
						DomainMetadataType dataType = desc.getLiteralOutput()
								.getDataType();
						String reference = dataType != null ? dataType
								.getReference() : null;
						generateLiteralDataOutput(definedOutputId, false,
								reference, schema, mimeType, encoding,
								desc.getTitle());
					} else if (desc.isSetBoundingBoxOutput()) {
						generateBBOXOutput(definedOutputId, false,
								desc.getTitle());
					} else {
						throw new ExceptionReport(
								"Requested type not supported: BBOX",
								ExceptionReport.INVALID_PARAMETER_VALUE);
					}
				}
			} else {
				LOGGER.info("OutputDefinitions are not stated explicitly in request");
				// THIS IS A WORKAROUND AND ACTUALLY NOT COMPLIANT TO THE
				// SPEC.
				for (int i = 0; i < processOutputsOnVariablesMapping.size(); i++) {
					String currentProcessId = processOutputsOnVariablesMapping
							.get(i).getProcessId();
					OutputDescriptionType[] descs = RepositoryManager
							.getInstance()
							.getProcessDescription(currentProcessId)
							.getProcessOutputs().getOutputArray();
					for (OutputDescriptionType desc : descs) {
						if (desc.getIdentifier()
								.getStringValue()
								.equals(processOutputsOnVariablesMapping.get(i)
										.getOutputIdentifier())) {
							OutputReferenceDescription varOutDesc = new OutputReferenceDescription(
									processOutputsOnVariablesMapping.get(i),
									desc);
							if (!varOutDescsContains(varOutDesc)) {
								varOutDescs.add(varOutDesc);
							}
						}
					}
				}

				if (description == null) {
					throw new RuntimeException(
							"Error while accessing the process description for "
									+ request.getProfileProcess()
											.getProcessDescription()
											.getIdentifier().getStringValue());
				}
				for (int i = 0; i < outputDescs.length; i++) {
					if (outputDescs[i].isSetComplexOutput()) {
						String schema = outputDescs[i].getComplexOutput()
								.getDefault().getFormat().getSchema();
						String encoding = outputDescs[i].getComplexOutput()
								.getDefault().getFormat().getEncoding();
						String mimeType = outputDescs[i].getComplexOutput()
								.getDefault().getFormat().getMimeType();
						generateComplexDataOutput(outputDescs[i]
								.getIdentifier().getStringValue(), false,
								false, schema, mimeType, encoding,
								outputDescs[i].getTitle());
					} else if (outputDescs[i].isSetLiteralOutput()) {
						generateLiteralDataOutput(outputDescs[i]
								.getIdentifier().getStringValue(), false,
								outputDescs[i].getLiteralOutput().getDataType()
										.getReference(), null, null, null,
								outputDescs[i].getTitle());
					}
				}
				for (int i = 0; i < varOutDescs.size(); i++) {
					if (varOutDescs.get(i).getDescription()
							.isSetComplexOutput()) {
						String schema = varOutDescs.get(i).getDescription()
								.getComplexOutput().getDefault().getFormat()
								.getSchema();
						String encoding = varOutDescs.get(i).getDescription()
								.getComplexOutput().getDefault().getFormat()
								.getEncoding();
						String mimeType = varOutDescs.get(i).getDescription()
								.getComplexOutput().getDefault().getFormat()
								.getMimeType();
						generateComplexDataOutput(varOutDescs.get(i)
								.getProcessOutputOnVariableMapping()
								.getOutputReference(), false, false, schema,
								mimeType, encoding, varOutDescs.get(i)
										.getDescription().getTitle());
					} else if (varOutDescs.get(i).getDescription()
							.isSetLiteralOutput()) {
						generateLiteralDataOutput(varOutDescs.get(i)
								.getProcessOutputOnVariableMapping()
								.getOutputReference(), false, varOutDescs
								.get(i).getDescription().getLiteralOutput()
								.getDataType().getReference(), null, null,
								null, varOutDescs.get(i).getDescription()
										.getTitle());
					}

				}
			}
			// }
		} else if (request.isStoreResponse()) {
			profileProcessResponseElem.setStatusLocation(DatabaseFactory
					.getDatabase().generateRetrieveResultURL(
							(request.getUniqueId()).toString()));
		}

	}

	private boolean varOutDescsContains(OutputReferenceDescription varOutDesc) {
		for (int i = 0; i < varOutDescs.size(); i++) {
			if (varOutDescs
					.get(i)
					.getProcessOutputOnVariableMapping()
					.getOutputReference()
					.equals(varOutDesc.getProcessOutputOnVariableMapping()
							.getOutputReference())) {
				return true;
			}
		}
		return false;
	}

	private OutputDescriptionType getDescOfVariable(String definedOutputId) {
		OutputDescriptionType outputDescription = null;
		for (OutputReferenceDescription varOutDescription : varOutDescs) {
			if (varOutDescription.getProcessOutputOnVariableMapping()
					.getOutputReference().equals(definedOutputId)) {
				outputDescription = varOutDescription.getDescription();
			}
		}
		return outputDescription;
	}

	private OutputReferenceDescription getVarOutDescriptionOfDefinedOutput(
			String varReferenceId) {
		OutputReferenceDescription varOutDescription = null;
		for (int i = 0; i < varOutDescs.size(); i++) {
			if (varOutDescs.get(i).getProcessOutputOnVariableMapping()
					.getOutputReference().equals(varReferenceId)) {
				varOutDescription = varOutDescs.get(i);
			}
		}
		return varOutDescription;
	}

	/**
	 * Sets the status.
	 * 
	 * @param status
	 *            status to be set.
	 */

	public void setStatus(StatusType status) {
		// workaround, should be generated either at the creation of the
		// document or when the process has been finished.
		status.setCreationTime(creationTime);
		profileProcessResponseDocument.getProfileProcessResponse().setStatus(
				status);
	}

	private static String getSchema(OutputDescriptionType desc,
			OutputDefinitionType def) {
		String schema = null;
		if (def != null) {
			schema = def.getSchema();
		}

		return schema;
	}

	private static String getEncoding(OutputDescriptionType desc,
			OutputDefinitionType def) {
		String encoding = null;
		if (def != null) {
			encoding = def.getEncoding();
		}
		return encoding;
	}

	/**
	 * Returns the mime-type.
	 * 
	 * @return the mime-type.
	 */

	public String getMimeType() {
		return getMimeType(null, null);
	}

	/**
	 * Returns the mime-type.
	 * 
	 * @param def
	 *            the definition of the output.
	 * @param outputReferenceDescription
	 *            the description of
	 * @return the mime-type
	 */

	public String getMimeType(OutputDefinitionType def,
			OutputReferenceDescription outputReferenceDescription) {

		String mimeType = "";
		OutputDescriptionType[] outputDescs = description.getProcessOutputs()
				.getOutputArray();
		boolean isSetResponseForm = request.getProfileProcess()
				.isSetResponseForm();

		String definedOutputId = "";

		if (def != null) {
			definedOutputId = def.getIdentifier().getStringValue();
		} else if (isSetResponseForm) {

			if (request.getProfileProcess().getResponseForm()
					.isSetRawDataOutput()) {
				definedOutputId = request.getProfileProcess().getResponseForm()
						.getRawDataOutput().getIdentifier().getStringValue();
			} else if (request.getProfileProcess().getResponseForm()
					.isSetResponseDocument()) {
				definedOutputId = request.getProfileProcess().getResponseForm()
						.getResponseDocument().getOutputArray(0)
						.getIdentifier().getStringValue();
			}
		}

		OutputDescriptionType outputDesc = null;
		if (outputReferenceDescription != null) {
			outputDesc = outputReferenceDescription.getDescription();
		} else {
			for (OutputDescriptionType tmpOutputDes : outputDescs) {
				if (definedOutputId.equalsIgnoreCase(tmpOutputDes
						.getIdentifier().getStringValue())) {
					outputDesc = tmpOutputDes;
					break;
				}
			}
		}

		if (isSetResponseForm) {
			// Get the outputdescriptions from the algorithm
			if (request.isRawData()) {
				// TODO Not verified! Verify!
				mimeType = request.getProfileProcess().getResponseForm()
						.getRawDataOutput().getMimeType();
			} else {
				// mimeType = "text/xml";
				// MSS 03/02/2009 defaulting to text/xml doesn't work when the
				// data is a complex raster
				if (outputDesc.isSetLiteralOutput()) {
					mimeType = "text/plain";
				} else if (outputDesc.isSetBoundingBoxOutput()) {
					mimeType = "text/xml";
				} else {
					if (def != null) {
						mimeType = def.getMimeType();
					} else {
						if (outputDesc.isSetComplexOutput()) {
							mimeType = outputDesc.getComplexOutput()
									.getDefault().getFormat().getMimeType();
							LOGGER.warn("Using default mime type: " + mimeType
									+ " for input: " + definedOutputId);
						}
					}
				}
			}
		}
		if (mimeType == null) {
			if (outputDesc.isSetLiteralOutput()) {
				mimeType = "text/plain";
			} else if (outputDesc.isSetBoundingBoxOutput()) {
				mimeType = "text/xml";
			} else if (outputDesc.isSetComplexOutput()) {
				mimeType = outputDesc.getComplexOutput().getDefault()
						.getFormat().getMimeType();
				LOGGER.warn("Using default mime type: " + mimeType
						+ " for input: " + definedOutputId);
			}
		}

		return mimeType;
	}

	private void generateComplexDataOutput(String definedOutputId,
			boolean asReference, boolean rawData, String schema,
			String mimeType, String encoding, LanguageStringType title)
			throws ExceptionReport {
		IData obj = request.getAttachedResult().get(definedOutputId);
		if (rawData) {
			rawDataElement = new RawData(obj, definedOutputId, schema,
					encoding, mimeType, this.identifier, description);
		} else {
			OutputDataItem outputDataItem = new OutputDataItem(obj,
					getOutputIdOfRelatedOutput(definedOutputId),
					definedOutputId, schema, encoding, mimeType, title,
					getProcessIdOfRelatedOutput(definedOutputId),
					getProcessDescriptionOfRelatedOutput(definedOutputId));
			if (asReference) {
				outputDataItem.updateResponseAsReference(
						executeResponseDocument,
						(request.getUniqueId()).toString(), mimeType);
				updateProfileProcessResponse(definedOutputId);
			} else {
				outputDataItem
						.updateResponseForInlineComplexData(executeResponseDocument);
				updateProfileProcessResponse(definedOutputId);
			}
		}

	}

	private String getOutputIdOfRelatedOutput(String definedOutputId) {
		String outputIdentifier = null;
		for (OutputReferenceDescription varOutDescription : varOutDescs) {
			if (varOutDescription.getProcessOutputOnVariableMapping()
					.getOutputReference().equals(definedOutputId)) {
				outputIdentifier = varOutDescription
						.getProcessOutputOnVariableMapping()
						.getOutputIdentifier();
			}
		}
		if (outputIdentifier == null) {
			outputIdentifier = definedOutputId;
		}
		return outputIdentifier;
	}

	private String getProcessIdOfRelatedOutput(String definedOutputId) {
		String processId = null;
		for (OutputReferenceDescription varOutDescription : varOutDescs) {
			if (varOutDescription.getProcessOutputOnVariableMapping()
					.getOutputReference().equals(definedOutputId)) {
				processId = varOutDescription
						.getProcessOutputOnVariableMapping().getProcessId();
			}
		}
		if (processId == null) {
			processId = identifier;
		}
		return processId;
	}

	private ProcessDescriptionType getProcessDescriptionOfRelatedOutput(
			String definedOutputId) {
		ProcessDescriptionType processDescription = null;
		for (OutputReferenceDescription varOutDescription : varOutDescs) {
			if (varOutDescription.getProcessOutputOnVariableMapping()
					.getOutputReference().equals(definedOutputId)) {
				String processId = varOutDescription
						.getProcessOutputOnVariableMapping().getProcessId();
				processDescription = RepositoryManager.getInstance()
						.getProcessDescription(processId);
			}
		}
		if (processDescription == null) {
			processDescription = description;
		}
		return processDescription;
	}

	private void updateProfileProcessResponse(String responseID) {
		OutputDataType output = profileProcessResponseDocument
				.getProfileProcessResponse().getProcessOutputs().addNewOutput();
		OutputDataType[] outputs = executeResponseDocument.getExecuteResponse()
				.getProcessOutputs().getOutputArray();
		for (OutputDataType currentOutput : outputs) {
			if (currentOutput.getIdentifier().getStringValue()
					.equals(responseID)) {
				output.set(currentOutput);
			}
		}
	}

	private void generateLiteralDataOutput(String definedOutputId,
			boolean rawData, String dataTypeReference, String schema,
			String mimeType, String encoding, LanguageStringType title)
			throws ExceptionReport {
		IData obj = request.getAttachedResult().get(definedOutputId);
		if (rawData) {
			rawDataElement = new RawData(obj, definedOutputId, schema,
					encoding, mimeType, this.identifier, description);
		} else {
			OutputDataItem handler = new OutputDataItem(obj,
					getOutputIdOfRelatedOutput(definedOutputId),
					definedOutputId, schema, encoding, mimeType, title,
					getProcessIdOfRelatedOutput(definedOutputId),
					getProcessDescriptionOfRelatedOutput(definedOutputId));
			handler.updateResponseForLiteralData(executeResponseDocument,
					dataTypeReference);
			updateProfileProcessResponse(definedOutputId);
		}
	}

	private void generateBBOXOutput(String definedOutputId, boolean rawData,
			LanguageStringType title) throws ExceptionReport {
		IBBOXData obj = (IBBOXData) request.getAttachedResult().get(
				definedOutputId);
		if (rawData) {
			// TODO Not verified! Verify!
			rawDataElement = new RawData(obj, definedOutputId, null, null,
					null, this.identifier, description);
		} else {
			OutputDataItem handler = new OutputDataItem(obj,
					getOutputIdOfRelatedOutput(definedOutputId),
					definedOutputId, null, null, null, title,
					getProcessIdOfRelatedOutput(definedOutputId),
					getProcessDescriptionOfRelatedOutput(definedOutputId));
			// TODO Not verified! Verify!
			handler.updateResponseForBBOXData(executeResponseDocument, obj);
			updateProfileProcessResponse(definedOutputId);
		}

	}
}
