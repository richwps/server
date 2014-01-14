package net.disy.richwps.wd;

import java.util.Collection;
import java.util.Collections;

import net.opengis.wps.x100.ExecuteDocument;

import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.n52.wps.transactional.deploy.AbstractProcessManager;
import org.n52.wps.transactional.request.DeployProcessRequest;
import org.n52.wps.transactional.request.UndeployProcessRequest;
import org.w3c.dom.Document;

public class WdLocalProcessManager extends AbstractProcessManager {

	public WdLocalProcessManager(
			ITransactionalAlgorithmRepository parentRepository) {
		super(parentRepository);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean unDeployProcess(UndeployProcessRequest request)
			throws Exception {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean containsProcess(String processID) throws Exception {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<String> getAllProcesses() throws Exception {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	@Override
	public Document invoke(ExecuteDocument payload, String algorithmID)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deployProcess(DeployProcessRequest request) throws Exception {
		// TODO Auto-generated method stub
		return true;
	}

}
