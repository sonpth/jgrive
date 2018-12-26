package com.github.sonpth.jgrive.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class FileUtils {
	public static final String APP_PROPERTY_FILE = "application.properties";
	public static final String APP_STATES_FILE=".states";
	public static final String APP_LAST_SYNC = "lastsync";

	private static Properties APP_STATES;

	private static final Log logger = LogFactory.getLog(FileUtils.class);
	
	public static Properties getProperties(String filename) throws IOException{
		Properties properties = new Properties();
		FileInputStream instream = new FileInputStream(filename);
		properties.load(instream);
		instream.close();
		
		return properties;
	}
	
	public static synchronized Properties getAppStates() throws IOException{
		if (APP_STATES != null){
			return APP_STATES;
		}
		
		APP_STATES = new Properties();
		FileInputStream instream;
		try {
			instream = new FileInputStream(APP_STATES_FILE);
			APP_STATES.load(instream);
			instream.close();
		} catch (FileNotFoundException ex) {
			logger.debug(ex.getMessage());
		}
		
		return APP_STATES;
	}
	
	public static synchronized void saveAppStates() throws IOException {
		FileOutputStream outstream = new FileOutputStream(APP_STATES_FILE);
		APP_STATES.store(outstream, null);
		outstream.close();
	}
	
	/**
	 * http://www.mkyong.com/java/how-to-generate-a-file-checksum-value-in-java/
	 * @throws NoSuchAlgorithmException 
	 * @throws IOException 
	 */
	public static String getMd5Checksum(File file) throws NoSuchAlgorithmException, IOException{
	    MessageDigest md = MessageDigest.getInstance("MD5");
	    FileInputStream fis = new FileInputStream(file);
	    byte[] dataBytes = new byte[1024];
	 
	    int nread = 0; 
	 
	    while ((nread = fis.read(dataBytes)) != -1) {
	      md.update(dataBytes, 0, nread);
	    };
	 
	    byte[] mdbytes = md.digest();
	 
	    //convert the byte to hex format
	    StringBuffer sb = new StringBuffer("");
	    for (int i = 0; i < mdbytes.length; i++) {
	    	sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
	    }
	 
	    fis.close();
	    return sb.toString();
	}
}
