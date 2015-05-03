package com.github.sonpth.jgrive.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class FileUtils {
	public static final String APP_PROPERTIES = "application.properties";
	
	public static final String APP_STATES=".states";
	public static final String APP_LAST_SYNC = "lastsync";

	public static Properties getProperties(String filename) throws IOException{
		Properties properties = new Properties();
		FileInputStream instream = new FileInputStream(filename);
		properties.load(instream);
		instream.close();
		
		return properties;
	}
	
	/**
	 * TODO should not pass appStates around ?
	 */
	public static void saveStates(Properties appStates) throws IOException {
		FileOutputStream outstream = new FileOutputStream(APP_STATES);
		appStates.store(outstream, null);
		outstream.close();
	}
	
	/**
	 * http://www.mkyong.com/java/how-to-generate-a-file-checksum-value-in-java/
	 * @throws NoSuchAlgorithmException 
	 * @throws IOException 
	 */
	public static String getMd5Checksum(String filename) throws NoSuchAlgorithmException, IOException{
	    MessageDigest md = MessageDigest.getInstance("MD5");
	    FileInputStream fis = new FileInputStream(filename);
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
