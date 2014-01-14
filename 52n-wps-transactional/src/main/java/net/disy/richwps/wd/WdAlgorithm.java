package net.disy.richwps.wd;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.opengis.wps.x100.ExecuteDocument;
import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.io.data.IData;
import org.n52.wps.server.AbstractTransactionalAlgorithm;
import org.n52.wps.server.ExceptionReport;

public class WdAlgorithm extends AbstractTransactionalAlgorithm {

	public WdAlgorithm(String algorithmID) {
		super(algorithmID);
		// TODO Auto-generated constructor stub
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
	public ProcessDescriptionType getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWellKnownName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean processDescriptionIsValid() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Class<?> getInputDataType(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getOutputDataType(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, IData> run(ExecuteDocument document) {
		// TODO Auto-generated method stub
		return Collections.emptyMap();
	}

}
