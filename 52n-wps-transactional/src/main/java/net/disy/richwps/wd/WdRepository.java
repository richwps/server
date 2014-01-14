package net.disy.richwps.wd;

import java.util.Collection;
import java.util.Collections;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.ITransactionalAlgorithmRepository;

public class WdRepository implements ITransactionalAlgorithmRepository {

	public WdRepository() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public Collection<String> getAlgorithmNames() {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	@Override
	public IAlgorithm getAlgorithm(String processID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ProcessDescriptionType getProcessDescription(String processID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsAlgorithm(String processID) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean addAlgorithm(Object className) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean removeAlgorithm(Object className) {
		// TODO Auto-generated method stub
		return true;
	}

}
