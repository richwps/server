package net.disy.wps.richwps.response;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.n52.wps.io.data.IData;

import de.hsos.richwps.dsl.api.elements.ReferenceOutputMapping;

/**
 * This implementation provides a Map data structure containing processing
 * results associated with a List data structure containing meta information
 * about the results.
 * 
 * <p>
 * Meta information about a processing result refers to a mapping of output
 * reference on output identifier and associated process identifier.<\p>
 * 
 * @author faltin
 *
 */
public class TestingOutputs implements Map<String, IData> {

	private Map<String, IData> outputs;
	private List<ReferenceOutputMapping> referenceOutputMappings;

	/**
	 * Constructs a new TestingOutputs.
	 * 
	 * <p>
	 * Receives the outputs data and the mapping data which are associated.<\p>
	 * 
	 * @param outputs
	 *            the processing outputs
	 * @param referenceOutputMappings
	 *            the mapping of output reference on output identifier and
	 *            process identifier
	 */
	public TestingOutputs(Map<String, IData> outputs,
			List<ReferenceOutputMapping> referenceOutputMappings) {
		this.outputs = outputs;
		this.referenceOutputMappings = referenceOutputMappings;
	}

	/**
	 * Returns the processing outputs.
	 * 
	 * @return the processing outputs.
	 */
	public Map<String, IData> getOutputs() {
		return outputs;
	}

	/**
	 * Returns the output reference on output identifier and process identifier
	 * mappings.
	 * 
	 * @return the mappings.
	 */
	public List<ReferenceOutputMapping> getReferenceOutputMappings() {
		return referenceOutputMappings;
	}

	@Override
	public int size() {
		return outputs.size();
	}

	@Override
	public boolean isEmpty() {
		return outputs.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return outputs.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return outputs.containsValue(value);
	}

	@Override
	public IData get(Object key) {
		return outputs.get(key);
	}

	@Override
	public IData put(String key, IData value) {
		return outputs.put(key, value);
	}

	@Override
	public IData remove(Object key) {
		return outputs.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends IData> m) {
		outputs.putAll(m);
	}

	@Override
	public void clear() {
		outputs.clear();
	}

	@Override
	public Set<String> keySet() {
		return outputs.keySet();
	}

	@Override
	public Collection<IData> values() {
		return outputs.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, IData>> entrySet() {
		return outputs.entrySet();
	}

}
