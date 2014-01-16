package net.disy.richwps.wd;

import java.io.BufferedWriter;
import java.io.File;
import java.util.Collection;
import java.util.Collections;

import net.opengis.wps.x100.ExecuteDocument;
import java.io.FileWriter;
import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.n52.wps.transactional.deploy.AbstractProcessManager;
import org.n52.wps.transactional.deploymentprofiles.DeploymentProfile;
import org.n52.wps.transactional.request.DeployProcessRequest;
import org.n52.wps.transactional.request.UndeployProcessRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import de.hsos.richwps.wd.Interpreter;
import de.hsos.richwps.wd.elements.Worksequence;

public class WdLocalProcessManager extends AbstractProcessManager {
	
	private static Logger LOGGER = LoggerFactory.getLogger(WdLocalProcessManager.class);

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
		final DeploymentProfile deploymentProfile = request.getDeploymentProfile();
		if (!(deploymentProfile instanceof WdDeploymentProfile)) {
			throw new IllegalArgumentException(WdLocalProcessManager.class.getSimpleName() + " only supports " + WdDeploymentProfile.class.getSimpleName());
		}
//		final WdDeploymentProfile wdDeploymentProfile = (WdDeploymentProfile) deploymentProfile;
//		String worksequenceDescription = wdDeploymentProfile.getWorksequenceDescription();
//		File wdFile = File.createTempFile("RichWPSWDTempFile", ".wd");
//		BufferedWriter writer = new BufferedWriter(new FileWriter(wdFile));
//		writer.write(worksequenceDescription);
//		writer.close();
//		Interpreter wdInterpreter = new Interpreter();
//		wdInterpreter.load(wdFile.getAbsolutePath());
//		Worksequence worksequence = wdInterpreter.getWorksequence();
//		LOGGER.warn("countExecutes: " + worksequence.countExecutes());
		return true;
	}

}
