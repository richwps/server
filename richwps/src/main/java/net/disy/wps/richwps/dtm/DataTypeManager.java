package net.disy.wps.richwps.dtm;

import net.disy.wps.n52.binding.IntegerListBinding;
import net.disy.wps.n52.binding.IntersectionFeatureCollectionListBinding;
import net.disy.wps.n52.binding.MPBResultBinding;
import net.disy.wps.n52.binding.ObeservationFeatureCollectionListBinding;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;

import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.literal.LiteralAnyURIBinding;
import org.n52.wps.io.data.binding.literal.LiteralBase64BinaryBinding;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
import org.n52.wps.io.data.binding.literal.LiteralByteBinding;
import org.n52.wps.io.data.binding.literal.LiteralDateTimeBinding;
import org.n52.wps.io.data.binding.literal.LiteralDoubleBinding;
import org.n52.wps.io.data.binding.literal.LiteralFloatBinding;
import org.n52.wps.io.data.binding.literal.LiteralIntBinding;
import org.n52.wps.io.data.binding.literal.LiteralLongBinding;
import org.n52.wps.io.data.binding.literal.LiteralShortBinding;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;

import com.sun.media.jai.opimage.OrderedDitherRIF;

import de.baw.wps.binding.GMLCovBinding;
import de.baw.wps.binding.NetCDFBinding;
import de.baw.wps.binding.OMBinding;

/**
 * 
 * DataTypeManager to provide stable types support for the RichWPS Orchestration Engine
 * It is able to assign bindings to literal and complex DescriptionTypes
 * @author woessner
 *
 */
public class DataTypeManager {
	
	private static DataTypeManager instance;
	
	public DataTypeManager(){

	}
	
	/**
	 * Returns an instance of the DataTypeManager
	 * @return instance of the DataTypeManager
	 */
	public static DataTypeManager getInstance(){
		if(instance==null){
			instance = new DataTypeManager();
		}
		return instance;
	}
	
	/**
	 * Returns a matching binding for a given InputDescriptionType
	 * @param type InputDescriptionType of input
	 * @return binding class or null
	 */
	public Class<?> getBindingForInputType(InputDescriptionType type) {
		
		if (type.isSetLiteralData()) {
			return getBindingForLiteralType(type.getLiteralData().getDataType().getStringValue());
		}
		else if (type.isSetComplexData()) {
			return getBindingForComplexType(type.getComplexData().getDefault().getFormat());
		}
		return null;
	}
	
	/**
	 * Returns a matching binding for a given OutputDescriptionType
	 * @param type OutputDescriptionType of output
	 * @return binding class or null
	 */
	public Class<?> getBindingForOutputType(OutputDescriptionType type) {

		if (type.isSetLiteralOutput()) {
			 return getBindingForLiteralType(type.getLiteralOutput().getDataType().getStringValue());
		}
		else if (type.isSetComplexOutput()) {
			return getBindingForComplexType(type.getComplexOutput().getDefault().getFormat());
		}
		return null;
	}
	
	/**
	 * Checks if a given literal input type string is supported
	 * @param type string representing the literal data type
	 * @return boolean
	 */
	public boolean isSupportedLiteralInputType(String type) {
		if (getBindingForLiteralType(type) != null) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Checks if a given complex input description type is supported
	 * @param idt InputDescriptionType
	 * @return boolean
	 */
	public boolean isSupportedComplexInputType(InputDescriptionType idt) {
		if (getBindingForComplexType(idt.getComplexData().getDefault().getFormat()) != null) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Checks if a given literal output type string is supported
	 * @param type string representing the literal data type
	 * @return boolean
	 */
	public boolean isSupportedLiteralOutputType(String type) {
		if (getBindingForLiteralType(type) != null) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Checks if a given complex output description type is supported
	 * @param odt OutputDescriptionType
	 * @return boolean
	 */
	public boolean isSupportedComplexOutputType(OutputDescriptionType odt) {
		if (getBindingForComplexType(odt.getComplexOutput().getDefault().getFormat()) != null) {
			return true;
		}
		else {
			return false;
		}
	}
	
	private Class<?> getBindingForLiteralType(String type) {
		if (type.toLowerCase().contains("float")) {
            return LiteralFloatBinding.class;
        }
		if (type.toLowerCase().contains("double")) {
            return LiteralDoubleBinding.class;
        }
		if (type.toLowerCase().contains("long")) {
            return LiteralLongBinding.class;
        }
		if (type.toLowerCase().contains("int")) {
            return LiteralIntBinding.class;
        }
		if (type.toLowerCase().contains("short")) {
            return LiteralShortBinding.class;
        }
		if (type.toLowerCase().contains("byte")) {
            return LiteralByteBinding.class;
        }
		if (type.toLowerCase().contains("boolean")) {
            return LiteralBooleanBinding.class;
        }
		if (type.toLowerCase().contains("string")) {
            return LiteralStringBinding.class;
        }
		if (type.toLowerCase().contains("datetime")) {
            return LiteralDateTimeBinding.class;
        }
		if (type.toLowerCase().contains("base64")) {
            return LiteralBase64BinaryBinding.class;
        }
		if (type.toLowerCase().contains("anyuri")) {
            return LiteralAnyURIBinding.class;
        }
        return null;
	}
	
	private Class<?> getBindingForComplexType(ComplexDataDescriptionType type) {
		String schema = type.getSchema();
		String mimeType = type.getMimeType();
		String encoding = type.getEncoding();
		
		if (mimeType.toLowerCase().contains("xml")) {
		
			if (schema.toLowerCase().contains("mpbresult")) {
				return MPBResultBinding.class;
			}
			if (schema.toLowerCase().contains("integerlist")) {
				return IntegerListBinding.class;
			}
			if (schema.toLowerCase().contains("observationfeaturecollectionlist")) {
				return ObeservationFeatureCollectionListBinding.class;
			}
			if (schema.toLowerCase().contains("intersectionfeaturecollectionlist")) {
				return IntersectionFeatureCollectionListBinding.class;
			}
			if (schema.toLowerCase().contains("gmlcovall")) {
				return GMLCovBinding.class;
			}
			if (schema.toLowerCase().contains("observation")) {
				return OMBinding.class;
			}
			if (schema.toLowerCase().contains("gml")) {
				return GTVectorDataBinding.class;
			}
		}
		if (mimeType.toLowerCase().contains("netcdf")) {
			return NetCDFBinding.class;
		}
		return null;
	}
}
