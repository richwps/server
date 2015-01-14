package net.disy.wps.richwps.response;

import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import net.disy.wps.richwps.oe.processor.TimeMeasurements;
import net.disy.wps.richwps.oe.processor.TimeMeasurements.Measurement;
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
import net.opengis.wps.x100.ProfileType;
import net.opengis.wps.x100.RuntimeInfoType;
import net.opengis.wps.x100.StatusType;

import org.apache.xmlbeans.GDurationBuilder;
import org.apache.xmlbeans.XmlCursor;
import org.joda.time.DateTime;
import org.joda.time.Duration;
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
		profileProcessResponse.addNewProfiles();
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

		ProfileProcessResponseDocument.ProfileProcessResponse profileProcessResponseElem = profileProcessResponseDocument
				.getProfileProcessResponse();
		ExecuteResponse executeResponseElem = executeResponseDocument
				.getExecuteResponse();

		if (profileProcessResponseElem.getStatus().isSetProcessSucceeded()) {
			dataInputs = request.getProfileProcess().getDataInputs();
			profileProcessResponseElem.setDataInputs(dataInputs);
			executeResponseElem.setDataInputs(dataInputs);
			profileProcessResponseElem.addNewProcessOutputs();
			executeResponseElem.addNewProcessOutputs();
			TimeMeasurements timeMeasurements = request.getTimeMeasurements();

			if (request.getProfileProcess().isSetResponseForm()) {
				OutputDescriptionType[] outputDescs = description
						.getProcessOutputs().getOutputArray();
				if (request.isRawData()) {
					OutputDefinitionType rawDataOutput = request
							.getProfileProcess().getResponseForm()
							.getRawDataOutput();
					String id = rawDataOutput.getIdentifier().getStringValue();
					OutputDescriptionType desc = XMLBeansHelper.findOutputByID(
							id, outputDescs);
					if (desc.isSetComplexOutput()) {
						String encoding = getEncoding(desc, rawDataOutput);
						String schema = getSchema(desc, rawDataOutput);
						String responseMimeType = getMimeType(rawDataOutput);
						generateComplexDataOutput(id, false, true, schema,
								responseMimeType, encoding, null);
					}

					else if (desc.isSetLiteralOutput()) {
						String mimeType = null;
						String schema = null;
						String encoding = null;
						DomainMetadataType dataType = desc.getLiteralOutput()
								.getDataType();
						String reference = dataType != null ? dataType
								.getReference() : null;
						generateLiteralDataOutput(id, true, reference, schema,
								mimeType, encoding, desc.getTitle());
					} else if (desc.isSetBoundingBoxOutput()) {
						generateBBOXOutput(id, true, desc.getTitle());
					}
					return;
				}
				for (int i = 0; i < request.getProfileProcess()
						.getResponseForm().getResponseDocument()
						.getOutputArray().length; i++) {
					OutputDefinitionType definition = request
							.getProfileProcess().getResponseForm()
							.getResponseDocument().getOutputArray(i);
					DocumentOutputDefinitionType documentDef = request
							.getProfileProcess().getResponseForm()
							.getResponseDocument().getOutputArray(i);
					String responseID = definition.getIdentifier()
							.getStringValue();
					OutputDescriptionType desc = XMLBeansHelper.findOutputByID(
							responseID, outputDescs);
					if (desc == null) {
						throw new ExceptionReport(
								"Could not find the output id " + responseID,
								ExceptionReport.INVALID_PARAMETER_VALUE);
					}
					if (desc.isSetComplexOutput()) {
						String mimeType = getMimeType(definition);
						String schema = getSchema(desc, definition);
						String encoding = getEncoding(desc, definition);
						generateComplexDataOutput(responseID,
								documentDef.getAsReference(), false, schema,
								mimeType, encoding, desc.getTitle());
					} else if (desc.isSetLiteralOutput()) {
						String mimeType = null;
						String schema = null;
						String encoding = null;
						DomainMetadataType dataType = desc.getLiteralOutput()
								.getDataType();
						String reference = dataType != null ? dataType
								.getReference() : null;
						generateLiteralDataOutput(responseID, false, reference,
								schema, mimeType, encoding, desc.getTitle());
					} else if (desc.isSetBoundingBoxOutput()) {
						generateBBOXOutput(responseID, false, desc.getTitle());
					} else {
						throw new ExceptionReport(
								"Requested type not supported: BBOX",
								ExceptionReport.INVALID_PARAMETER_VALUE);
					}
				}

			} else {
				LOGGER.info("OutputDefinitions are not stated explicitly in request");

				// THIS IS A WORKAROUND AND ACTUALLY NOT COMPLIANT TO THE SPEC.

				ProcessDescriptionType description = RepositoryManager
						.getInstance().getProcessDescription(
								request.getProfileProcess()
										.getProcessDescription()
										.getIdentifier().getStringValue());
				if (description == null) {
					throw new RuntimeException(
							"Error while accessing the process description for "
									+ request.getProfileProcess()
											.getProcessDescription()
											.getIdentifier().getStringValue());
				}

				OutputDescriptionType[] d = description.getProcessOutputs()
						.getOutputArray();
				for (int i = 0; i < d.length; i++) {
					if (d[i].isSetComplexOutput()) {
						String schema = d[i].getComplexOutput().getDefault()
								.getFormat().getSchema();
						String encoding = d[i].getComplexOutput().getDefault()
								.getFormat().getEncoding();
						String mimeType = d[i].getComplexOutput().getDefault()
								.getFormat().getMimeType();
						generateComplexDataOutput(d[i].getIdentifier()
								.getStringValue(), false, false, schema,
								mimeType, encoding, d[i].getTitle());
					} else if (d[i].isSetLiteralOutput()) {
						generateLiteralDataOutput(d[i].getIdentifier()
								.getStringValue(), false, d[i]
								.getLiteralOutput().getDataType()
								.getReference(), null, null, null,
								d[i].getTitle());
					}
				}
			}
			updateProfiles(timeMeasurements);
		} else if (request.isStoreResponse()) {
			profileProcessResponseElem.setStatusLocation(DatabaseFactory
					.getDatabase().generateRetrieveResultURL(
							(request.getUniqueId()).toString()));
		}
	}

	private void updateProfiles(TimeMeasurements timeMeasurements) {
		timeMeasurements.fullStop();
		Iterator<Measurement> measurementIterator = timeMeasurements
				.getIterator();
		while (measurementIterator.hasNext()) {
			Measurement measurement = measurementIterator.next();
			generateProfileProcess(measurement);
		}
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
		return getMimeType(null);
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

	public String getMimeType(OutputDefinitionType def) {

		String mimeType = "";
		OutputDescriptionType[] outputDescs = description.getProcessOutputs()
				.getOutputArray();

		boolean isResponseForm = request.getProfileProcess()
				.isSetResponseForm();

		String inputID = "";

		if (def != null) {
			inputID = def.getIdentifier().getStringValue();
		} else if (isResponseForm) {

			if (request.getProfileProcess().getResponseForm()
					.isSetRawDataOutput()) {
				inputID = request.getProfileProcess().getResponseForm()
						.getRawDataOutput().getIdentifier().getStringValue();
			} else if (request.getProfileProcess().getResponseForm()
					.isSetResponseDocument()) {
				inputID = request.getProfileProcess().getResponseForm()
						.getResponseDocument().getOutputArray(0)
						.getIdentifier().getStringValue();
			}
		}

		OutputDescriptionType outputDes = null;

		for (OutputDescriptionType tmpOutputDes : outputDescs) {
			if (inputID.equalsIgnoreCase(tmpOutputDes.getIdentifier()
					.getStringValue())) {
				outputDes = tmpOutputDes;
				break;
			}
		}

		if (isResponseForm) {
			// Get the outputdescriptions from the algorithm
			if (request.isRawData()) {
				mimeType = request.getProfileProcess().getResponseForm()
						.getRawDataOutput().getMimeType();
			} else {
				// mimeType = "text/xml";
				// MSS 03/02/2009 defaulting to text/xml doesn't work when the
				// data is a complex raster
				if (outputDes.isSetLiteralOutput()) {
					mimeType = "text/plain";
				} else if (outputDes.isSetBoundingBoxOutput()) {
					mimeType = "text/xml";
				} else {
					if (def != null) {
						mimeType = def.getMimeType();
					} else {
						if (outputDes.isSetComplexOutput()) {
							mimeType = outputDes.getComplexOutput()
									.getDefault().getFormat().getMimeType();
							LOGGER.warn("Using default mime type: " + mimeType
									+ " for input: " + inputID);
						}
					}
				}
			}
		}
		if (mimeType == null) {
			if (outputDes.isSetLiteralOutput()) {
				mimeType = "text/plain";
			} else if (outputDes.isSetBoundingBoxOutput()) {
				mimeType = "text/xml";
			} else if (outputDes.isSetComplexOutput()) {
				mimeType = outputDes.getComplexOutput().getDefault()
						.getFormat().getMimeType();
				LOGGER.warn("Using default mime type: " + mimeType
						+ " for input: " + inputID);
			}
		}

		return mimeType;
	}

	private void generateComplexDataOutput(String definedOutputId,
			boolean asReference, boolean rawData, String schema,
			String mimeType, String encoding, LanguageStringType title)
			throws ExceptionReport {
		request.getTimeMeasurements().start(
				"Generating Output " + definedOutputId, false);
		IData obj = request.getAttachedResult().get(definedOutputId);
		if (rawData) {
			rawDataElement = new RawData(obj, definedOutputId, schema,
					encoding, mimeType, this.identifier, description);
		} else {
			OutputDataItem outputDataItem = new OutputDataItem(obj,
					definedOutputId, schema, encoding, mimeType, title,
					this.identifier, description);
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
		request.getTimeMeasurements().stop();

	}

	private void generateProfileProcess(Measurement measurement) {
		ProfileType processProfile = profileProcessResponseDocument
				.getProfileProcessResponse().getProfiles().addNewProfile();
		processProfile.addNewIdentifier().setStringValue(
				measurement.getMeasurementId());
		if (measurement.getDescription() != null) {
			processProfile.addNewTitle().setStringValue(
					measurement.getDescription());
		}
		DateTime startTime = measurement.getStartTime();
		RuntimeInfoType runtimeInfo = processProfile.addNewRuntimeInfo();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(startTime.toDate());
		runtimeInfo.setStarttime(calendar);
		long runtime = measurement.getRuntime();
		Duration duration = new Duration(runtime);
		GDurationBuilder gdurationBuilder = new GDurationBuilder(
				duration.toString());
		runtimeInfo.setRuntime(gdurationBuilder.toGDuration());

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
		request.getTimeMeasurements().start(
				"Generating Output " + definedOutputId, false);
		IData obj = request.getAttachedResult().get(definedOutputId);
		if (rawData) {
			rawDataElement = new RawData(obj, definedOutputId, schema,
					encoding, mimeType, this.identifier, description);
		} else {
			OutputDataItem handler = new OutputDataItem(obj, definedOutputId,
					schema, encoding, mimeType, title, this.identifier,
					description);
			handler.updateResponseForLiteralData(executeResponseDocument,
					dataTypeReference);
			updateProfileProcessResponse(definedOutputId);
		}
		request.getTimeMeasurements().stop();
	}

	private void generateBBOXOutput(String definedOutputId, boolean rawData,
			LanguageStringType title) throws ExceptionReport {
		request.getTimeMeasurements().start(
				"Generating Output " + definedOutputId, false);
		IBBOXData obj = (IBBOXData) request.getAttachedResult().get(
				definedOutputId);
		if (rawData) {
			// TODO Not verified! Verify!
			rawDataElement = new RawData(obj, definedOutputId, null, null,
					null, this.identifier, description);
		} else {
			OutputDataItem handler = new OutputDataItem(obj, definedOutputId,
					null, null, null, title, this.identifier, description);
			// TODO Not verified! Verify!
			handler.updateResponseForBBOXData(executeResponseDocument, obj);
			updateProfileProcessResponse(definedOutputId);
		}
		request.getTimeMeasurements().stop();
	}
}
