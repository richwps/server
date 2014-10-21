package net.disy.richwps.oe;

import javax.xml.transform.TransformerException;

import org.apache.commons.lang.StringUtils;
import org.apache.xpath.XPathAPI;
import org.n52.wps.transactional.deploymentprofiles.DeploymentProfile;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;

public class WdDeploymentProfile extends DeploymentProfile {

    public WdDeploymentProfile(Node payload, String processID) {
        super(payload, processID);
    }

    public String getWorksequenceDescription() {
        return getWorksequenceDescriptionFromPayload((Node) getPayload());
    }

    private String getWorksequenceDescriptionFromPayload(Node payload) {
        try {
            String worksequenceDescription = XPathAPI.selectSingleNode(payload, "/DeployProcess/DeploymentProfile/WorksequenceDescription/text()").getNodeValue();
            return StringUtils.trimToEmpty(worksequenceDescription);
        } catch (DOMException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

}
