package com.sense.dbproxy.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBProperties {
	 private static Logger logger = LoggerFactory.getLogger(DBProperties.class);
	private final static String CONF_FILENAME = "dbproxy.properties";
	private static Properties props = new Properties();
	
    private static InetAddress listenAddress;
    private static Integer listenPort;

    
    static URL getStorageConfigURI() throws FileNotFoundException  {
        String confdir = System.getProperty("dd.server.conf.dir");
        if (confdir != null) {
            String scp = confdir + File.separator + CONF_FILENAME;
            File scpf = new File(scp);
            if (scpf.exists()) {
                try {
                    return scpf.toURI().toURL();
                } catch (MalformedURLException e) {
                }
            }
        }

        ClassLoader loader = DBProperties.class.getClassLoader();
        URL scpurl = loader.getResource(CONF_FILENAME);
        if (scpurl != null)
            return scpurl;

        throw new FileNotFoundException("Cannot locate " + CONF_FILENAME );
    }
    static {
        try {
            URL configFileURI = getStorageConfigURI();
            if (logger.isDebugEnabled())
                    logger.debug("Loading settings from " + configFileURI);
            
            props.load(configFileURI.openStream());
        } catch (FileNotFoundException e) {
            logger.warn("Can't get configuration file.");
        } catch (IOException e) {
            logger.error("Fatal error: " + e.getMessage());
            System.err.println("Bad configuration; unable to start server");
            System.exit(1);
        }
    }
    public static String getProperty(String key, String defaultValue) {
        if (props.containsKey(key))
            return props.getProperty(key, defaultValue);
        return System.getProperty(key, defaultValue);
    }

	public static void getXml() throws IOException {
	}

	public static InetAddress getListenAddress() throws IOException {
		 if (listenAddress == null) {
	            try {
	            /* Local IP or hostname to bind thrift server to */
	            String ssAddr = getProperty("dd.server.listen.address", "0.0.0.0");
	            if ( ssAddr != null )
	                listenAddress = InetAddress.getByName(ssAddr);
	            } catch (Exception e) {
	                logger.warn("Can't get dd.server.listen.address: " + e.getMessage());
	                try {
	                    listenAddress = InetAddress.getByName("0.0.0.0");
	                } catch (UnknownHostException e1) {
	                }
	            }
	        }
	        return listenAddress;
	}


    public static int getListenPort() {
        if (listenPort == null) {
            try {
            String port = getProperty("dbproxy.server.listen.port", "9161");
            if (port != null)
                listenPort = Integer.parseInt(port);
            } catch (Exception e) {
                logger.warn("Can't get dd.server.listen.port: " + e.getMessage());
                listenPort = 9161;
            }
        }
        return listenPort;
    }

}
