package net.disy.richwps.wd;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import net.opengis.wps.x100.ExecuteDocument;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
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
		// Iterate over all WD files and take their prefix as the processId (convention used in this implementation)
		return getAllProcessesFromWdDirectory();
	}
	
	private Collection<String> getAllProcessesFromWdDirectory() {
		final Collection<String> processIds = new ArrayList<String>();
		URI wdDirectory = getWorksequenceDescriptionDirectory();
		File directory = new File(wdDirectory);
		Collection<File> files = FileUtils.listFiles(directory, new String[]{"wd"}, false);
		for (File file : files) {
			processIds.add(FilenameUtils.getBaseName(file.getAbsolutePath()));
		}
		return processIds; 
	}

	@Override
	public Document invoke(ExecuteDocument payload, String algorithmID)
			throws Exception {
		// TODO Lade die WD Datei basierend auf der processID aus WEB-INF/WorksequenceDescriptions/ und f�hre interpreter aus
		return null;
	}

	@Override
	public boolean deployProcess(DeployProcessRequest request) throws Exception {
		final DeploymentProfile deploymentProfile = request.getDeploymentProfile();
		if (!(deploymentProfile instanceof WdDeploymentProfile)) {
			throw new IllegalArgumentException(WdLocalProcessManager.class.getSimpleName() + " only supports " + WdDeploymentProfile.class.getSimpleName());
		}
		final WdDeploymentProfile wdDeploymentProfile = (WdDeploymentProfile) deploymentProfile;
		File wdFile = saveWorksequenceDescription(wdDeploymentProfile.getProcessID(), wdDeploymentProfile.getWorksequenceDescription());
		Interpreter wdInterpreter = new Interpreter();
		wdInterpreter.load(wdFile.getAbsolutePath());
		Worksequence worksequence = wdInterpreter.getWorksequence();
		LOGGER.warn("countExecutes: " + worksequence.countExecutes());
		return true;
	}
	
	private File saveWorksequenceDescription(String processId, String worksequenceDescription) {
		File wdFile = null;
		try {
			URI fileUri = generateWorksequenceDescriptionFileUri(processId);
			wdFile = new File(fileUri);
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
		URI fileUri = generateWorksequenceDescriptionFileUri(processId);
		File file = new File(fileUri);
		if (file.exists()) {
			file.delete();
		}
	}
	
	private URI generateWorksequenceDescriptionFileUri(String processId) {
		try {
			return new URI(getWorksequenceDescriptionDirectory().toString() + processId + ".wd");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	private URI getWorksequenceDescriptionDirectory() {
		String fullPath = getClass().getProtectionDomain().getCodeSource()
				.getLocation().toString();
		int searchIndex = fullPath.indexOf("WEB-INF");
		String subPath = fullPath.substring(0, searchIndex);

		URI directoryUri;
		try {
			directoryUri = new URL(subPath
					+ "WEB-INF/WorksequenceDescriptions/").toURI();

			File directory = new File(directoryUri);
			if (!directory.exists()) {
				directory.mkdirs();
			}

			return directoryUri;

		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

	}

}