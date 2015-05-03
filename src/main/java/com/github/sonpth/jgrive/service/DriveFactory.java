package com.github.sonpth.jgrive.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;

public class DriveFactory {
	private static final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
	private static final String APP_PROPERTIES = "application.properties";
	private static final String APP_STATES=".states";
	
	private volatile Drive instance;
	
	private boolean requestNewAuthToken = false;
	
	public DriveFactory(boolean requestNewAuthToken){
		this.requestNewAuthToken = requestNewAuthToken;
	}
	
	public Drive getInstanceWithNewToken() throws IOException {
		HttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();
				
		Properties appProperties = getProperties(APP_PROPERTIES);
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				httpTransport, jsonFactory, appProperties.getProperty("clientid"), appProperties.getProperty("clientsec"), Arrays.asList(DriveScopes.DRIVE))
					.setAccessType("offline")
					.setApprovalPrompt("auto")
					.build();

		String url = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();
		System.out.println("Please go to this URL and get an authentication code:");
		System.out.println("  " + url);
		System.out.println("Please input the authentication code here:");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String code = br.readLine();

		//TODO review
		GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(REDIRECT_URI).execute();
		//GoogleCredential credential = new GoogleCredential().setFromTokenResponse(response); //Only works with accessType == 'online' 
        GoogleCredential credential = new GoogleCredential.Builder().setTransport(httpTransport)
            .setJsonFactory(jsonFactory)
            .setClientSecrets(appProperties.getProperty("clientid"), appProperties.getProperty("clientsec"))
            .build()
            .setFromTokenResponse(response);
        String accessToken = credential.getAccessToken();
        String refreshToken = credential.getRefreshToken();

		Properties appStates = new Properties();
		appStates.put("accessToken", accessToken);
		appStates.put("refreshToken", refreshToken);
        FileOutputStream outstream = new FileOutputStream(APP_STATES);
        appStates.store(outstream, null);
        outstream.close();
		return new Drive.Builder(httpTransport, jsonFactory, credential).build();
	}
	
	private Drive getInstanceWithRefreshToken() throws IOException{
		HttpTransport httpTransport = new NetHttpTransport();
		JsonFactory jsonFactory = new JacksonFactory();
		
		Properties appProperties = getProperties(APP_PROPERTIES);
		Properties appStates = getProperties(APP_STATES);
		
		GoogleCredential credential = new GoogleCredential.Builder().setJsonFactory(jsonFactory)
				.setTransport(httpTransport).setClientSecrets(appProperties.getProperty("clientid"), appProperties.getProperty("clientsec")).build();
		credential.setAccessToken(appStates.getProperty("accessToken"));
		credential.setRefreshToken(appStates.getProperty("refreshToken"));

		return new Drive.Builder(httpTransport, jsonFactory, credential).build();
	}
	
	private Properties getProperties(String filename) throws IOException{
		Properties appProperties = new Properties();
		FileInputStream instream = new FileInputStream(filename);
		appProperties.load(instream);
		instream.close();
		
		return appProperties;
	}
	
	private Drive getGoolgeDriveInstance() throws IOException{
		if (requestNewAuthToken){
			return getInstanceWithNewToken();
		} else {
			return getInstanceWithRefreshToken();
		}
	}
	
	public Drive getInstance() throws IOException{
		if (instance == null){
			synchronized (this) {
				if (instance == null){
					instance = getGoolgeDriveInstance();
				}
			}
		}
		return instance;
	}
}
