package net.disy.richwps.wd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import net.opengis.wps.x100.ExecuteDocument;

import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.n52.wps.transactional.deploy.AbstractProcessManager;
import org.n52.wps.transactional.deploymentprofiles.DeploymentProfile;
import org.n52.wps.transactional.request.DeployProcessRequest;
import org.n52.wps.transactional.request.UndeployProcessRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class WdLocalProcessManager extends AbstractProcessManager {
	
	private static Logger LOGGER = LoggerFactory.getLogger(WdLocalProcessManager.class);

	public WdLocalProcessManager(
			ITransactionalAlgorithmRepository parentRepository) {
		super(parentRepository);
	}

	@Override
	public boolean unDeployProcess(UndeployProcessRequest request)
			throws Exception {
		deleteWorksequenceDescription(request.getProcessID());
		return true;
	}

	@Override
	public boolean containsProcess(String processID) throws Exception {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Collection<String> getAllProcesses() throws Exception {
		// TODO wie ist das drüben implementiert? woher weiß er an dieser stelle, welche seine eigenen prozesse sind? oder gehts dort nur weil die info über den bpelclient kommt?
		return Arrays.asList("sampleWdProcess");
	}

	@Override
	public Document invoke(ExecuteDocument payload, String algorithmID)
			throws Exception {
		// TODO Lade die WD Datei basierend auf der processID aus WEB-INF/WorksequenceDescriptions/ und führe interpreter aus
		return null;
	}

	@Override
	public boolean deployProcess(DeployProcessRequest request) throws Exception {
		final DeploymentProfile deploymentProfile = request.getDeploymentProfile();
		if (!(deploymentProfile instanceof WdDeploymentProfile)) {
			throw new IllegalArgumentException(WdLocalProcessManager.class.getSimpleName() + " only supports " + WdDeploymentProfile.class.getSimpleName());
		}
		final WdDeploymentProfile wdDeploymentProfile = (WdDeploymentProfile) deploymentProfile;
		saveWorksequenceDescription(wdDeploymentProfile.getProcessID(), wdDeploymentProfile.getWorksequenceDescription());
		
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
	
	private File saveWorksequenceDescription(String processId, String worksequenceDescription) {
		File wdFile = null;
		try {
			String path = generateWorksequenceDescriptionFilePath(processId);
			wdFile = new File(path);
			BufferedWriter writer = new BufferedWriter(new FileWriter(wdFile));
			writer.write(worksequenceDescription);
			writer.close();
			return wdFile;
		} catch (IOException e) {
			throw new RuntimeException("Could not save WD file "
					+ (wdFile != null ? wdFile.getAbsolutePath() : ""));
		}
	}
	
	private void deleteWorksequenceDescription(String processId) {
		String path = generateWorksequenceDescriptionFilePath(processId);
		File file = new File(path);
		if (file.exists()) {
			file.delete();
		}
	}
	
	private String generateWorksequenceDescriptionFilePath(String processId) {
		String fullPath = getClass().getProtectionDomain().getCodeSource().getLocation().toString();
		int searchIndex= fullPath.indexOf("WEB-INF");
		String subPath = fullPath.substring(0, searchIndex);
		subPath = subPath.replaceFirst("file:", "");
		if(subPath.startsWith("/")){
			subPath = subPath.substring(1);
		}
		
		File directory = new File(subPath+"WEB-INF/WorksequenceDescriptions/");
		if(!directory.exists()){
			directory.mkdirs();
		}

		return subPath+"WEB-INF/WorksequenceDescriptions/"+processId+".wd";		
		
	}

}
