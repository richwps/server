package org.n52.wps.client;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;

import org.apache.xmlbeans.XmlException;
import org.n52.wps.FormatDocument.Format;
import org.n52.wps.GeneratorDocument.Generator;
import org.n52.wps.ParserDocument.Parser;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.WPSClientConfigurationDocument;
import org.n52.wps.impl.WPSClientConfigurationDocumentImpl.WPSClientConfigurationImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * 
 * WPSClientConfig singelton which holds the wps client configuration with its datahandlers
 * @author 52north
 * @author woessner
 *
 */
public class WPSClientConfig implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = -5034849968372875707L;
    private static transient WPSClientConfig wpsClientConfig;
    private static transient WPSClientConfigurationImpl wpsClientConfigXMLBeans;

    private static transient Logger LOGGER = LoggerFactory.getLogger(WPSClientConfig.class);

    // FvK: added Property Change support
    protected final PropertyChangeSupport propertyChangeSupport;
    // constants for the Property change event names
    public static final String WPSCONFIG_PROPERTY_EVENT_NAME = "WPSConfigUpdate";
    public static final String WPSCAPABILITIES_SKELETON_PROPERTY_EVENT_NAME = "WPSCapabilitiesUpdate";

    public static final String CONFIG_FILE_NAME = "wps_client_config.xml";
    private static final String CONFIG_FILE_DIR = "config";
    private static final String URL_DECODE_ENCODING = "UTF-8";

    private WPSClientConfig(String wpsConfigPath) throws XmlException, IOException {
        wpsClientConfigXMLBeans = (WPSClientConfigurationImpl) WPSClientConfigurationDocument.Factory.parse(new File(wpsConfigPath)).getWPSClientConfiguration();

        // FvK: added Property Change support
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    private WPSClientConfig(InputStream resourceAsStream) throws XmlException, IOException {
        wpsClientConfigXMLBeans = (WPSClientConfigurationImpl) WPSClientConfigurationDocument.Factory.parse(resourceAsStream).getWPSClientConfiguration();

        // FvK: added Property Change support
        this.propertyChangeSupport = new PropertyChangeSupport(this);
    }

    /**
     * Add an Listener to the wpsConfig
     * 
     * @param propertyName
     * @param listener
     */
    public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
        this.propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * remove a listener from the wpsConfig
     * 
     * @param propertyName
     * @param listener
     */
    public void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
        this.propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    // For Testing purpose only
    public void notifyListeners() {
        this.propertyChangeSupport.firePropertyChange(WPSCONFIG_PROPERTY_EVENT_NAME, null, null);
    }

    public void firePropertyChange(String event) {
    	propertyChangeSupport.firePropertyChange(event, null, null);
    }

    /**
     * WPSClientConfig is a singleton. If there is a need for reinitialization, use this path.
     * 
     * @param configPathp
     *        path to the wps_config.xml
     * @throws XmlException
     * @throws IOException
     */
    public static void forceInitialization(String configPath) throws XmlException, IOException {
        // temporary save all registered listeners
        PropertyChangeListener[] listeners = {};
        if (wpsClientConfig != null) {
            listeners = wpsClientConfig.propertyChangeSupport.getPropertyChangeListeners();
        }
        wpsClientConfig = new WPSClientConfig(configPath);

        // register all saved listeners to new wpsConfig Instance
        // reversed order to keep original order of the registration!!!
        for (int i = listeners.length - 1; i >= 0; i--) {
            wpsClientConfig.propertyChangeSupport.addPropertyChangeListener(listeners[i]);
        }

        // fire event
        wpsClientConfig.propertyChangeSupport.firePropertyChange(WPSCONFIG_PROPERTY_EVENT_NAME, null, wpsClientConfig);
        LOGGER.info("Configuration Reloaded, Listeners informed");
    }

    /**
     * WPSClientConfig is a singleton. If there is a need for reinitialization, use this path.
     * 
     * @param stream
     *        stream containing the wps_config.xml
     * @throws XmlException
     * @throws IOException
     */
    public static void forceInitialization(InputStream stream) throws XmlException, IOException {
        // temporary save all registered listeners
        PropertyChangeListener[] listeners = {};
        if (wpsClientConfig != null) {
            listeners = wpsClientConfig.propertyChangeSupport.getPropertyChangeListeners();
        }

        wpsClientConfig = new WPSClientConfig(stream);

        // register all saved listeners to new wpsConfig Instance
        // reversed order to keep original order of the registration!!!
        for (int i = listeners.length - 1; i >= 0; i--) {
            wpsClientConfig.propertyChangeSupport.addPropertyChangeListener(listeners[i]);
        }

        // fire event
        wpsClientConfig.propertyChangeSupport.firePropertyChange(WPSCONFIG_PROPERTY_EVENT_NAME, null, wpsClientConfig);
        LOGGER.info("Configuration Reloaded, Listeners informed");
    }

    /**
     * returns an instance of the WPSClientConfig class. WPSClientConfig is a single. If there is need for
     * reinstantitation, use forceInitialization().
     * 
     * @return WPSClientConfig object representing the wps_config.xml from the classpath or webapps folder
     */
    public static WPSClientConfig getInstance() {
        if (wpsClientConfig == null) {
            String path = getConfigPath();
            WPSClientConfig config = getInstance(path);
            wpsClientConfig = config;
        }

        return wpsClientConfig;
    }

    /**
     * returns an instance of the WPSClientConfig class. WPSCofnig is a single. If there is need for
     * reinstantitation, use forceInitialization().
     * 
     * @param path
     *        path to the wps_config.xml
     * @return WPSClientConfig object representing the wps_config.xml from the given path
     */
    public static WPSClientConfig getInstance(String path) {
        if (LOGGER.isDebugEnabled())
            LOGGER.debug("Getting WPSClientConfig instance... from path: " + path);

        if (wpsClientConfig == null) {
            try {
                wpsClientConfig = new WPSClientConfig(path);
            }
            catch (XmlException e) {
                LOGGER.error("Failed to initialize WPS. Reason: " + e.getMessage());
                throw new RuntimeException("Failed to initialize WPS. Reason: " + e.getMessage());
            }
            catch (IOException e) {
                LOGGER.error("Failed to initialize WPS. Reason: " + e.getMessage());
                throw new RuntimeException("Failed to initialize WPS. Reason: " + e.getMessage());
            }
        }
        return wpsClientConfig;
    }
    
    /**
     * This method retrieves the full path for the file (wps_config.xml), searching in WEB-INF/config. This is
     * only applicable for webapp applications. To customize this, please use directly
     * {@link WPSClientConfig#forceInitialization(String)} and then getInstance().
     * 
     * @return
     * @throws IOException
     */
    public static String getConfigPath() {
        String configPath = tryToGetPathFromClassPath();
        File file = null;
        if (configPath != null) {
            file = new File(configPath);
            if (file.exists()) {
                return configPath;
            }
        }

        configPath = tryToGetPathLastResort();
        if (configPath != null) {
            file = new File(configPath);
            if (configPath != null && file.exists()) {
                return configPath;
            }
        }

        throw new RuntimeException("Could not find and load wps_client_config.xml");
    }

    public static String tryToGetPathFromClassPath() {
        URL configPathURL = WPSClientConfig.class.getClassLoader().getResource(CONFIG_FILE_NAME);
        if (configPathURL != null) {
            String config = configPathURL.getFile();
            try {
                config = URLDecoder.decode(config, URL_DECODE_ENCODING);
            }
            catch (UnsupportedEncodingException e) {
                LOGGER.error("Could not devode URL to get config from class path.", e);
                return null;
            }
            return config;
        }
        return null;
    }


    public static String tryToGetPathLastResort() {
        String domain = WPSClientConfig.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        
        try {
			domain = URLDecoder.decode(domain, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.warn("Could not decode URL of WPSClientConfig class, continuing.");
		}
        
        /*
         * domain should always be 52n-wps-commons/target/classes so we just go three directories up
         */
        File classDir = new File(domain);

        File projectRoot = classDir.getParentFile().getParentFile().getParentFile();

        String path = projectRoot.getAbsolutePath();

        String[] dirs = projectRoot.getAbsoluteFile().list();
        for (String dir : dirs) {
            if (dir.startsWith("52n-wps-webapp") && !dir.endsWith(".war")) {
                path = path + File.separator + dir + File.separator + "src" + File.separator + "main" + File.separator
                        + "webapp" + File.separator + CONFIG_FILE_DIR + File.separator + CONFIG_FILE_NAME;
            }
        }
        LOGGER.info(path);
        return path;
    }

    public WPSClientConfigurationImpl getWPSConfig() {
        return wpsClientConfigXMLBeans;
    }

    public Parser[] getRegisteredParser() {
        return wpsClientConfigXMLBeans.getDatahandlers().getParserList().getParserArray();
    }

    public Parser[] getActiveRegisteredParser() {
        Parser[] parsers = getRegisteredParser();
        ArrayList<Parser> activeParsers = new ArrayList<Parser>();
        for (int i = 0; i < parsers.length; i++) {
            if (parsers[i].getActive()) {
                activeParsers.add(parsers[i]);
            }
        }
        Parser[] parArr = {};
        return activeParsers.toArray(parArr);
    }

    public Generator[] getRegisteredGenerator() {
        return wpsClientConfigXMLBeans.getDatahandlers().getGeneratorList().getGeneratorArray();
    }

    public Generator[] getActiveRegisteredGenerator() {
        Generator[] generators = getRegisteredGenerator();
        ArrayList<Generator> activeGenerators = new ArrayList<Generator>();
        for (int i = 0; i < generators.length; i++) {
            if (generators[i].getActive()) {
                activeGenerators.add(generators[i]);
            }
        }
        Generator[] genArr = {};
        return activeGenerators.toArray(genArr);
    }

    public Property[] getPropertiesForGeneratorClass(String className) {
        Generator[] generators = wpsClientConfigXMLBeans.getDatahandlers().getGeneratorList().getGeneratorArray();
        for (int i = 0; i < generators.length; i++) {
            Generator generator = generators[i];
            if (generator.getClassName().equals(className)) {
                return generator.getPropertyArray();
            }
        }
        return (Property[]) Array.newInstance(Property.class, 0);

    }

    public Format[] getFormatsForGeneratorClass(String className) {
        Generator[] generators = wpsClientConfigXMLBeans.getDatahandlers().getGeneratorList().getGeneratorArray();
        for (int i = 0; i < generators.length; i++) {
            Generator generator = generators[i];
            if (generator.getClassName().equals(className)) {
                return generator.getFormatArray();
            }
        }
        return (Format[]) Array.newInstance(Format.class, 0);

    }

    public Property[] getPropertiesForParserClass(String className) {
        Parser[] parsers = wpsClientConfigXMLBeans.getDatahandlers().getParserList().getParserArray();
        for (int i = 0; i < parsers.length; i++) {
            Parser parser = parsers[i];
            if (parser.getClassName().equals(className)) {
                return parser.getPropertyArray();
            }
        }
        return (Property[]) Array.newInstance(Property.class, 0);

    }

    public Format[] getFormatsForParserClass(String className) {
        Parser[] parsers = wpsClientConfigXMLBeans.getDatahandlers().getParserList().getParserArray();
        for (int i = 0; i < parsers.length; i++) {
            Parser parser = parsers[i];
            if (parser.getClassName().equals(className)) {
                return parser.getFormatArray();
            }
        }
        return (Format[]) Array.newInstance(Format.class, 0);

    }

    public boolean isParserActive(String className) {
        Parser[] activeParser = getActiveRegisteredParser();
        for (int i = 0; i < activeParser.length; i++) {
            Parser parser = activeParser[i];
            if (parser.getClassName().equals(className)) {
                return parser.getActive();
            }
        }
        return false;
    }

    public boolean isGeneratorActive(String className) {
        Generator[] generators = getActiveRegisteredGenerator();
        for (int i = 0; i < generators.length; i++) {
            Generator generator = generators[i];
            if (generator.getClassName().equals(className)) {
                return generator.getActive();
            }
        }
        return false;
    }

    public Property getPropertyForKey(Property[] properties, String key) {
        for (Property property : properties) {
            if (property.getName().equalsIgnoreCase(key)) {
                return property;
            }
        }
        return null;
    }

    /**
     * 
     * @return directory of the configuration folder
     */
    public static final String getConfigDir() {
        String dir = getConfigPath();
        return dir.substring(0, dir.lastIndexOf(CONFIG_FILE_NAME));
    }

}
