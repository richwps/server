package net.disy.richwps.wd;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTRasterDataBinding;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractTransactionalAlgorithm;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.transactional.algorithm.GenericTransactionalAlgorithm;
import org.n52.wps.transactional.deploy.IProcessManager;
import org.n52.wps.transactional.service.TransactionalHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WdAlgorithm extends AbstractTransactionalAlgorithm {

    private static Logger LOGGER = LoggerFactory.getLogger(WdAlgorithm.class);

    private ProcessDescriptionType processDescription;

    public WdAlgorithm(String algorithmID) {
        super(algorithmID);
        processDescription = initializeDescription();
    }

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData)
            throws ExceptionReport {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getErrors() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getWellKnownName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProcessDescriptionType getDescription() {
        return processDescription;
    }

    @Override
    public boolean processDescriptionIsValid() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Class<?> getInputDataType(String id) {
        InputDescriptionType[] inputs = processDescription.getDataInputs().getInputArray();
        for (InputDescriptionType input : inputs) {
            if (input.getIdentifier().getStringValue().equals(id)) {
                
                if (input.isSetLiteralData()) {
                    String datatype = input.getLiteralData().getDataType().getStringValue();
                    
                    if (datatype.contains("tring")) {
                        return LiteralStringBinding.class;
                    }
                    if (datatype.contains("ollean")) {
                        return LiteralBooleanBinding.class;
                    }
                    if (datatype.contains("loat") || datatype.contains("ouble")) {
                        return LiteralDoubleBinding.class;
                    }
                    if (datatype.contains("nt")) {
                        return LiteralIntBinding.class;
                    }
                }
                if (input.isSetComplexData()) {
                    //TODO: be careful here!
                    String mimeType = input.getComplexData().getDefault().getFormat().getMimeType();
                    if (mimeType.contains("xml") || (mimeType.contains("xml") || (mimeType.contains("json")))) {
                        return GTVectorDataBinding.class;
                    } else {
                        return GTRasterDataBinding.class;
                    }
                }
            }
        }
        throw new RuntimeException("Could not determie internal inputDataType");
    }

    @Override
    public Class<?> getOutputDataType(String id) {
        OutputDescriptionType[] outputs = processDescription.getProcessOutputs().getOutputArray();

        for (OutputDescriptionType output : outputs) {

            if (output.isSetLiteralOutput()) {
                String datatype = output.getLiteralOutput().getDataType().getStringValue();
                if (datatype.contains("tring")) {
                    return LiteralStringBinding.class;
                }
                if (datatype.contains("ollean")) {
                    return LiteralBooleanBinding.class;
                }
                if (datatype.contains("loat") || datatype.contains("ouble")) {
                    return LiteralDoubleBinding.class;
                }
                if (datatype.contains("nt")) {
                    return LiteralIntBinding.class;
                }
            }
            if (output.isSetComplexOutput()) {
                //TODO: be careful here!
            	String mimeType = output.getComplexOutput().getDefault().getFormat().getMimeType();
            	String schema = output.getComplexOutput().getDefault().getFormat().getSchema();
                if(mimeType.equals("text/xml")  && (schema.contains("gml") || schema.contains("GML"))) {
                	return GTVectorDataBinding.class;
                }
                else if (mimeType.contains("xml") || (mimeType.contains("XML")) || (mimeType.contains("json"))) {
                    return GenericFileDataBinding.class;
                }
                else {
                    return GenericFileDataBinding.class;
                }
            }
        }
        throw new RuntimeException("Could not determie internal inputDataType");
    }

    @Override
    public Map<String, IData> run(ExecuteDocument document) {
        try {
            IProcessManager deployManager = TransactionalHelper.getProcessManagerForSchema("RichWpsWd.xsd");
            return deployManager.invoke(document, getAlgorithmID());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ProcessDescriptionType initializeDescription() {
        // TODO use generate method from transactionalrequesthandler
        String fullPath = GenericTransactionalAlgorithm.class.getProtectionDomain().getCodeSource().getLocation().toString();
        int searchIndex = fullPath.indexOf("WEB-INF");
        String subPath = fullPath.substring(0, searchIndex);
        String processID = getAlgorithmID();
        // sanitize processID: strip version number and namespace if passed in
        if (processID.contains("-")) {
            processID = processID.split("-")[0];
        }
        if (processID.contains("}")) {
            processID = processID.split("}")[1];
        }
        try {
            URI fileUri = new URL(subPath + "WEB-INF" + File.separator + "ProcessDescriptions" + File.separator + processID + ".xml").toURI();
            File xmlDesc = new File(fileUri);
            XmlOptions option = new XmlOptions();
            option.setLoadTrimTextBuffer();
            ProcessDescriptionType doc = ProcessDescriptionType.Factory.parse(xmlDesc, option);
//            ProcessDescriptionsDocument doc = ProcessDescriptionsDocument.Factory.parse(xmlDesc, option);
//            if (doc.getProcessDescriptions().getProcessDescriptionArray().length == 0) {
//                LOGGER.warn("ProcessDescription does not contain any description");
//                return null;
//            }
//            doc.getIdentifier().setStringValue(processID);

            return doc;
            
        } catch (IOException e) {
            LOGGER.warn("Could not initialize algorithm, parsing error: " + getAlgorithmID(), e);
        } catch (XmlException e) {
            LOGGER.warn("Could not initialize algorithm, parsing error: " + getAlgorithmID(), e);
        } catch (URISyntaxException e) {
            LOGGER.warn("Could not initialize algorithm, parsing error: " + getAlgorithmID(), e);
        }
        return null;
    }

}
