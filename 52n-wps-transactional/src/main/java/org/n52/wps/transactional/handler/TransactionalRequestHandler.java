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

package org.n52.wps.transactional.handler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.n52.wps.server.ExceptionReport;
import org.n52.wps.server.IAlgorithmRepository;
import org.n52.wps.server.ITransactionalAlgorithmRepository;
import org.n52.wps.server.RepositoryManager;
import org.n52.wps.server.handler.IHandler;
import org.n52.wps.transactional.algorithm.GenericTransactionalAlgorithm;
import org.n52.wps.transactional.request.DeployProcessRequest;
import org.n52.wps.transactional.request.ITransactionalRequest;
import org.n52.wps.transactional.request.UndeployProcessRequest;
import org.n52.wps.transactional.response.DeployProcessResponse;
import org.n52.wps.transactional.response.ITransactionalResponse;
import org.n52.wps.transactional.response.UndeployProcessResponse;
import org.n52.wps.transactional.service.TransactionalHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class TransactionalRequestHandler implements IHandler {

	private static Logger LOGGER = LoggerFactory.getLogger(TransactionalRequestHandler.class);

	protected OutputStream os;

	protected ITransactionalRequest req;

	public TransactionalRequestHandler(ITransactionalRequest request) throws ExceptionReport {
		if (request == null) {
			throw new ExceptionReport("Request not valid", ExceptionReport.OPERATION_NOT_SUPPORTED);
		} else if (request instanceof DeployProcessRequest
				|| request instanceof UndeployProcessRequest) {
			this.req = request;
		} else {
			throw new ExceptionReport("Request type unknown (" + request.getClass().toString()
					+ ") Must be DeployProcess or UnDeployProcess",
					ExceptionReport.OPERATION_NOT_SUPPORTED);
		}

		req = request;
	}

	public TransactionalRequestHandler(InputStream is, OutputStream os) throws ExceptionReport {

		Document doc;
		this.os = os;

		try {
			DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
			fac.setNamespaceAware(true);// this prevents "xmlns="""
			fac.setIgnoringElementContentWhitespace(true);

			DocumentBuilder documentBuilder = fac.newDocumentBuilder();
			doc = documentBuilder.parse(is);

			Node child = doc.getFirstChild();

			while (child.getNodeName().compareTo("#comment") == 0) {
				child = child.getNextSibling();
			}

			// TODO: check version
			Node versionNode = child.getAttributes().getNamedItem("version");

			String requestType = getRequestType(doc.getFirstChild());

			LOGGER.info("Request type: " + requestType);

			if (requestType == null) {
				throw new ExceptionReport("Request not valid",
						ExceptionReport.OPERATION_NOT_SUPPORTED);
			} else if (requestType.equals("DeployProcess")) {
				this.req = new DeployProcessRequest(doc);
			} else if (requestType.equals("UnDeployProcess")) {
				this.req = new UndeployProcessRequest(doc);
			} else {
				throw new ExceptionReport("Request type unknown (" + requestType
						+ ") Must be DeployProcess or UnDeployProcess",
						ExceptionReport.OPERATION_NOT_SUPPORTED);
			}

		} catch (SAXException e) {
			throw new ExceptionReport("There went something wrong with parsing the POST data: "
					+ e.getMessage(), ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (IOException e) {
			throw new ExceptionReport("There went something wrong with the network connection.",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		} catch (ParserConfigurationException e) {
			throw new ExceptionReport("There is a internal parser configuration error",
					ExceptionReport.NO_APPLICABLE_CODE, e);
		}

	}

	/**
	 * Handles the request and returns a transactional response (if succeeded)
	 * or throws an exception (otherwise)
	 * 
	 * @param request
	 *            the request to handle
	 * @return a response if the process has succeeded. <code>null</code> is
	 *         never returned
	 * @throws Exception
	 *             if an error occurs handling the request
	 */
	public ITransactionalResponse handle() throws ExceptionReport {
		if (this.req == null)
			throw new ExceptionReport("Internal Error", "");
		if (req instanceof DeployProcessRequest) {
			return handleDeploy((DeployProcessRequest) req);
		} else if (req instanceof UndeployProcessRequest) {
			return handleUnDeploy((UndeployProcessRequest) req);
		} else {
			throw new ExceptionReport("Error. Could not handle request",
					ExceptionReport.OPERATION_NOT_SUPPORTED);
		}
	}

	private DeployProcessResponse handleDeploy(DeployProcessRequest request) throws ExceptionReport {

		DeployProcessResponse response;
		saveProcessDescription(request);

		try {
			ITransactionalAlgorithmRepository repository = TransactionalHelper
					.getMatchingTransactionalRepository(request.getDeploymentProfileName());

			if (repository == null) {
				throw new ExceptionReport("Could not find matching repository",
						ExceptionReport.NO_APPLICABLE_CODE);
			}

			if (!repository.addAlgorithm(request)) {
				throw new ExceptionReport("Could not deploy process",
						ExceptionReport.NO_APPLICABLE_CODE);
			} else {
				request.updateResponseProcessDescriptions(repository);
				response = new DeployProcessResponse(request);
				return response;
			}

		} catch (RuntimeException e) {
			throw new ExceptionReport("Could not deploy process",
					ExceptionReport.NO_APPLICABLE_CODE);
		}

	}

	private void saveProcessDescription(DeployProcessRequest request) {
		String processId = request.getProcessId();
		ProcessDescriptionType pDescr = request.getProcessDescription();

		URI fileUri = generateProcessDescriptionFileUri(processId);

		try {
			writeXmlFile(pDescr, fileUri);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// TODO
	private static ITransactionalResponse handleUnDeploy(UndeployProcessRequest request)
			throws ExceptionReport {
		UndeployProcessResponse response;

		try {
			if (RepositoryManager.getInstance().getAlgorithm(request.getProcessID()) == null) {
				throw new ExceptionReport("The process does not exist",
						ExceptionReport.INVALID_PARAMETER_VALUE);
			}
			IAlgorithmRepository repository = RepositoryManager.getInstance()
					.getRepositoryForAlgorithm(request.getProcessID());
			if (repository instanceof ITransactionalAlgorithmRepository) {
				ITransactionalAlgorithmRepository transactionalRepository = (ITransactionalAlgorithmRepository) repository;
				if (!transactionalRepository.removeAlgorithm(request)) {
					throw new ExceptionReport("Could not undeploy process",
							ExceptionReport.NO_APPLICABLE_CODE);
				} else {
					deleteProcessDescription(request.getProcessID());
					request.updateResponseProcessDescriptions(transactionalRepository);
					response = new UndeployProcessResponse(request);
					return response;
				}
			} else {
				throw new ExceptionReport("The process is not in a transactional "
						+ "repository and cannot be undeployed",
						ExceptionReport.INVALID_PARAMETER_VALUE);
			}
		} catch (RuntimeException e) {
			throw new ExceptionReport("Could not undeploy process",
					ExceptionReport.NO_APPLICABLE_CODE);
		}
	}

	public static URI generateProcessDescriptionFileUri(String processId) {
		String fullPath = GenericTransactionalAlgorithm.class.getProtectionDomain().getCodeSource()
				.getLocation().toString();
		int searchIndex = fullPath.indexOf("WEB-INF");
		String subPath = fullPath.substring(0, searchIndex);

		URI directoryUri;
		try {
			directoryUri = new URL(subPath + "WEB-INF/ProcessDescriptions/").toURI();
			File directory = new File(directoryUri);
			if (!directory.exists()) {
				directory.mkdirs();
			}

			return new URI(directoryUri.toString() + "/" + processId + ".xml");
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}

	}

	private static void deleteProcessDescription(String processId) {
		URI fileUri = generateProcessDescriptionFileUri(processId);
		File file = new File(fileUri);
		if (file.exists()) {
			file.delete();
		}
	}

	protected void writeXmlFile(XmlObject doc, URI fileUri) throws IOException {
		File file = new File(fileUri);
		String parent = file.getParent();
		File directory = new File(parent);
		directory.mkdirs();

		XmlOptions xmlOptions = new XmlOptions();
		xmlOptions.setSavePrettyPrint();

		doc.save(file, xmlOptions);

	}

	@Deprecated
	protected void writeXmlFile(Node node, URI fileUri) throws IOException,
			TransformerFactoryConfigurationError, TransformerException,
			ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		Document tempDocument = documentBuilder.newDocument();
		Node importedNode = tempDocument.importNode(node, true);
		tempDocument.appendChild(importedNode);
		tempDocument.getDocumentElement().setAttribute("xmlns:wps",
				"http://www.opengis.net/wps/1.0.0");
		tempDocument.getDocumentElement().setAttribute("xmlns:ows",
				"http://www.opengis.net/ows/1.1");
		// Prepare the DOM document for writing
		Source source = new DOMSource(tempDocument);

		// Prepare the output file
		File file = new File(fileUri);
		String parent = file.getParent();
		File directory = new File(parent);
		directory.mkdirs();
		// file.createNewFile();
		OutputStream fileOutput = new FileOutputStream(file);
		Result result = new StreamResult(fileOutput);

		// Write the DOM document to the file
		Transformer xformer = TransformerFactory.newInstance().newTransformer();
		xformer.transform(source, result);
		fileOutput.close();
	}

	private String getRequestType(Node node) {
		String localName = node.getLocalName();
		if (localName.equalsIgnoreCase("undeployprocess")) {
			return "UnDeployProcess";
		} else if (localName.equalsIgnoreCase("deployprocess")) {
			return "DeployProcess";
		} else {
			return null;
		}
	}

	public static IHandler newInstance(ByteArrayInputStream is, OutputStream os)
			throws ExceptionReport {
		return new TransactionalRequestHandler(is, os);
	}

}
