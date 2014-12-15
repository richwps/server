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


package net.disy.wps.richwps.wpsclient;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import net.disy.wps.richwps.dtm.DataTypeManager;
import net.opengis.wps.x100.OutputDataType;
import net.opengis.wps.x100.OutputDescriptionType;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.commons.XMLUtil;
import org.n52.wps.io.BasicXMLTypeFactory;
import org.n52.wps.io.IOHandler;
import org.n52.wps.io.IParser;
import org.n52.wps.io.ParserFactory;
import org.n52.wps.io.data.IData;
import org.n52.wps.server.ExceptionReport;
import org.w3c.dom.Node;


public class OutputHandler {
	
	/**
	 * Handles the ComplexValueReference
	 * @param class1 
	 * @param input The client input
	 * @throws ExceptionReport If the input (as url) is invalid, or there is an error while parsing the XML.
	 */
	public static String handleComplexValueReference(OutputDataType output) throws ExceptionReport{
		return output.getReference().getHref();
	}
	
	/**
	 * Handles the complexValue, which in this case should always include XML 
	 * which can be parsed into a FeatureCollection.
	 * @param class1 
	 * @param input The client input
	 * @throws ExceptionReport If error occured while parsing XML
	 */
	public static IData handleComplexValue(OutputDataType output, ProcessDescriptionType processDescription) throws ExceptionReport{
		DataTypeManager dtm = DataTypeManager.getInstance();
		String outputID = output.getIdentifier().getStringValue();
		
		String complexValue = getComplexValueNodeString(output.getData().getComplexData().getDomNode());

		OutputDescriptionType outputDesc = null;
		for(OutputDescriptionType tempDesc : processDescription.getProcessOutputs().getOutputArray()) {
			if((tempDesc.getIdentifier().getStringValue().startsWith(outputID))) {
				outputDesc = tempDesc;
				break;
			}
		}

		if(outputDesc == null) {
			throw new RuntimeException("output cannot be found in description for " + processDescription.getIdentifier().getStringValue() + "," + outputID);
		}
		
		// get data specification from request
		String schema = output.getData().getComplexData().getSchema();
		String encoding = output.getData().getComplexData().getEncoding();
		String mimeType = output.getData().getComplexData().getMimeType();
		
		// check for null elements in request and replace by defaults
		if(schema == null) {
			schema = outputDesc.getComplexOutput().getDefault().getFormat().getSchema();
		}
		if(mimeType == null) {
			mimeType = outputDesc.getComplexOutput().getDefault().getFormat().getMimeType();
		}
		if(encoding == null) {
			encoding = outputDesc.getComplexOutput().getDefault().getFormat().getEncoding();
		}
		
		Class<?> outputDataType = dtm.getBindingForOutputType(outputDesc);
		
		IParser parser = ParserFactory.getInstance().getParser(schema, mimeType, encoding, outputDataType);
		if(parser == null) {
			throw new ExceptionReport("Error. No applicable parser found for " + schema + "," + mimeType + "," + encoding, ExceptionReport.NO_APPLICABLE_CODE);
		}
		IData data = null;
		// encoding is UTF-8 (or nothing and we default to UTF-8)
		// everything that goes to this condition should be inline xml data
		if (encoding == null || encoding.equals("") || encoding.equalsIgnoreCase(IOHandler.DEFAULT_ENCODING)){
			try {
				InputStream stream = new ByteArrayInputStream(complexValue.getBytes());
				data = parser.parse(stream, mimeType, schema);
			}
			catch(RuntimeException e) {
				throw new ExceptionReport("Error occured, while XML parsing", 
						ExceptionReport.NO_APPLICABLE_CODE, e);
			}
		}
		else {
			throw new ExceptionReport("parser does not support operation: " + parser.getClass().getName(), ExceptionReport.INVALID_PARAMETER_VALUE);
		}
		return data;
	}

	public static IData handleLiteralValue(OutputDataType output) throws ExceptionReport {
		
		String parameter = output.getData().getLiteralData().getStringValue();
		String xmlDataType = output.getData().getLiteralData().getDataType();
		IData parameterObj = null;
		try {
			parameterObj = BasicXMLTypeFactory.getBasicJavaObject(xmlDataType, parameter);
		}
		catch(RuntimeException e) {
			throw new ExceptionReport("The passed parameterValue: " + parameter + ", but should be of type: " + xmlDataType, ExceptionReport.INVALID_PARAMETER_VALUE);
		}
		if(parameterObj == null) {
			throw new ExceptionReport("XML datatype as LiteralParameter is not supported by the server: dataType " + xmlDataType, 
					ExceptionReport.INVALID_PARAMETER_VALUE);
		}
		return parameterObj;
		
	}
	
	/**
	 * Handles BBoxValue
	 * @param input The client input
	 * @param class1 
	 */
	public static IData handleBBoxValue(OutputDataType input) throws ExceptionReport{
		//String inputID = input.getIdentifier().getStringValue();
		throw new ExceptionReport("BBox is not supported", ExceptionReport.OPERATION_NOT_SUPPORTED);
	}
	
	protected static String getComplexValueNodeString(Node complexValueNode) {
        String complexValue;
        try {
            complexValue = XMLUtil.nodeToString(complexValueNode);
            complexValue = complexValue.substring(complexValue.indexOf(">") + 1, complexValue.lastIndexOf("</"));
        } catch (TransformerFactoryConfigurationError e1) {
            throw new TransformerFactoryConfigurationError("Could not parse inline data. Reason " + e1);
        } catch (TransformerException e1) {
            throw new TransformerFactoryConfigurationError("Could not parse inline data. Reason " + e1);
        }
        return complexValue;
    }

}
