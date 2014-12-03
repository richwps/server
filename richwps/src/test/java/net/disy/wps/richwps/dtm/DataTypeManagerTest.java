package net.disy.wps.richwps.dtm;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.disy.wps.n52.binding.IntegerListBinding;
import net.disy.wps.n52.binding.IntersectionFeatureCollectionListBinding;
import net.disy.wps.n52.binding.MPBResultBinding;
import net.disy.wps.n52.binding.ObeservationFeatureCollectionListBinding;
import net.opengis.wps.x100.ComplexDataDescriptionType;
import net.opengis.wps.x100.InputDescriptionType;
import net.opengis.wps.x100.OutputDescriptionType;

import org.eclipse.xtext.junit4.serializer.AssertNodeModelAcceptor;
import org.junit.Test;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
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

import de.baw.wps.binding.GMLCovBinding;
import de.baw.wps.binding.NetCDFBinding;
import de.baw.wps.binding.OMBinding;

public class DataTypeManagerTest {
	
	DataTypeManager dtm = DataTypeManager.getInstance();
	
	private HashMap<String, Class<?>> literalTypes;
	private HashMap<FormatTriplet<String, String, String>, Class<?>> complexOutputTypes;
	
	
	public DataTypeManagerTest() {
		literalTypes = new HashMap<String, Class<?>>();
		literalTypes.put("xs:float", LiteralFloatBinding.class);
		literalTypes.put("xs:double", LiteralDoubleBinding.class);
		literalTypes.put("xs:long", LiteralLongBinding.class);
		literalTypes.put("xs:int", LiteralIntBinding.class);
		literalTypes.put("xs:short", LiteralShortBinding.class);
		literalTypes.put("xs:byte", LiteralByteBinding.class);
		literalTypes.put("xs:boolean", LiteralBooleanBinding.class);
		literalTypes.put("xs:string", LiteralStringBinding.class);
		literalTypes.put("xs:dateTime", LiteralDateTimeBinding.class);
		literalTypes.put("xs:base64Binary", LiteralBase64BinaryBinding.class);
		literalTypes.put("xs:anyURI", LiteralAnyURIBinding.class);
		
		
		complexOutputTypes = new HashMap<FormatTriplet<String, String, String>, Class<?>>();
		
		// GML
		complexOutputTypes.put(new FormatTriplet<String, String, String>("http://schemas.opengis.net/gml/3.1.1/base/feature.xsd", "text/xml", "UTF-8"), GTVectorDataBinding.class);
		complexOutputTypes.put(new FormatTriplet<String, String, String>("http://schemas.opengis.net/gml/3.2.1/base/feature.xsd", "text/xml; subtype=gml/3.2.1", "UTF-8"), GTVectorDataBinding.class);
		complexOutputTypes.put(new FormatTriplet<String, String, String>("http://geoserver.itc.nl:8080/wps/schemas/gml/2.1.2/gmlpacket.xsd", "text/xml", "UTF-8"), GTVectorDataBinding.class);
		
		// RichWPS LKN types
		complexOutputTypes.put(new FormatTriplet<String, String, String>("http://richwps.github.io/schemas/MPBResult", "application/xml", "UTF-8"), MPBResultBinding.class);
		complexOutputTypes.put(new FormatTriplet<String, String, String>("http://richwps.github.io/schemas/IntegerList", "application/xml", "UTF-8"), IntegerListBinding.class);
		complexOutputTypes.put(new FormatTriplet<String, String, String>("http://richwps.github.io/schemas/ObservationFeatureCollectionList", "application/xml", "UTF-8"), ObeservationFeatureCollectionListBinding.class);
		complexOutputTypes.put(new FormatTriplet<String, String, String>("http://richwps.github.io/schemas/IntersectionFeatureCollectionList", "application/xml", "UTF-8"), IntersectionFeatureCollectionListBinding.class);
		complexOutputTypes.put(new FormatTriplet<String, String, String>("", "application/json", ""), GTVectorDataBinding.class);
		complexOutputTypes.put(new FormatTriplet<String, String, String>("", "application/json", "UTF-8"), GTVectorDataBinding.class);
		
		// RichWPS BAW types
		complexOutputTypes.put(new FormatTriplet<String, String, String>("", "application/x-netcdf", ""), NetCDFBinding.class);
		complexOutputTypes.put(new FormatTriplet<String, String, String>("http://schemas.opengis.net/gmlcov/1.0/gmlcovAll.xsd", "text/xml", ""), GMLCovBinding.class);
		complexOutputTypes.put(new FormatTriplet<String, String, String>("http://schemas.opengis.net/om/2.0/observation.xsd", "text/xml", "UTF-8"), OMBinding.class);
		
	}

	@Test
	public void testLiteralOutputDescriptionTypeToBindingAssignment() {
		OutputDescriptionType odt;
		Class<?> binding;
		
		Iterator<Entry<String, Class<?>>> it = literalTypes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Class<?>> pairs = (Map.Entry<String, Class<?>>) it.next();
			odt = OutputDescriptionType.Factory.newInstance();
			odt.addNewLiteralOutput().addNewDataType().setStringValue(pairs.getKey());
			binding = dtm.getBindingForOutputType(odt);
			assertTrue(binding.equals(pairs.getValue()));
		}
	}
	
	@Test
	public void testLiteralInputDescriptionTypeToBindingAssignment() {
		InputDescriptionType idt;
		Class<?> binding;
		
		Iterator<Entry<String, Class<?>>> it = literalTypes.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Class<?>> pairs = (Map.Entry<String, Class<?>>) it.next();
			idt = InputDescriptionType.Factory.newInstance();
			idt.addNewLiteralData().addNewDataType().setStringValue(pairs.getKey());
			binding = dtm.getBindingForInputType(idt);
			assertTrue(binding.equals(pairs.getValue()));
		}
	}
	
	@Test
	public void testComplexOutputDescriptiontypeToBindingAssignment() {
		OutputDescriptionType odt;
		
		Iterator<Entry<FormatTriplet<String, String, String>, Class<?>>> it = complexOutputTypes.entrySet().iterator();
		for (int i=0;i<complexOutputTypes.size();i++) {
			Map.Entry<FormatTriplet<String, String, String>, Class<?>> pairs = (Map.Entry<FormatTriplet<String, String, String>, Class<?>>) it.next();
			Class<?> binding = getBindingForFormatTriplet(pairs.getKey()); 
			assertTrue(binding.equals(pairs.getValue()));
		}
	}
	
	
	@Test
	public void testValidFormats() {
		Class<?> binding;
		
		binding = getBindingForFormatTriplet(new FormatTriplet<String, String, String>("", "xml", ""));
		assertTrue(binding.equals(GenericFileDataBinding.class));
		
		binding = getBindingForFormatTriplet(new FormatTriplet<String, String, String>("xsd", "", ""));
		assertTrue(binding.equals(GenericFileDataBinding.class));
		
		binding = getBindingForFormatTriplet(new FormatTriplet<String, String, String>("xsd", "", ""));
		assertTrue(binding.equals(GenericFileDataBinding.class));
	}
	
	@Test
	public void testInvalidFormats() {
		Class<?> binding;
		
		binding = getBindingForFormatTriplet(new FormatTriplet<String, String, String>(null, null, null));
		assertNull(binding);
		
		binding = getBindingForFormatTriplet(new FormatTriplet<String, String, String>("", "", ""));
		assertNull(binding);
		
		binding = getBindingForFormatTriplet(new FormatTriplet<String, String, String>("foo", "bar", "buzz"));
		assertNull(binding);
		
		binding = getBindingForFormatTriplet(new FormatTriplet<String, String, String>("", "gml", ""));
		assertNull(binding);
		
		binding = getBindingForFormatTriplet(new FormatTriplet<String, String, String>("", "", "gml"));
		assertNull(binding);
		
	}
	
	private Class<?> getBindingForFormatTriplet(FormatTriplet<String, String, String> formatTriplet) {
		OutputDescriptionType odt = OutputDescriptionType.Factory.newInstance();
		ComplexDataDescriptionType cddt = odt.addNewComplexOutput().addNewDefault().addNewFormat();
		
		cddt.setSchema(formatTriplet.getSchema());
		cddt.setMimeType(formatTriplet.getMimeType());
		cddt.setEncoding(formatTriplet.getEncoding());
	
		return dtm.getBindingForOutputType(odt);
	}	
}
