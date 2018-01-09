package com.github.sonpth.jgrive;

import static com.github.sonpth.jgrive.service.FileUtils.APP_LAST_SYNC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.sonpth.jgrive.service.DriveFactory;
import com.github.sonpth.jgrive.service.FileUtils;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;


/**
 * TODO
 * - insert
 * - delete
 */
public class JGrive {
	private static final boolean DRY_RUN = false;
	private static final Log LOGGER = LogFactory.getLog(JGrive.class);
	
//	private static Properties APP_PROPERTIES;
	
    private static void usage() {
    	System.out.println("JGrive options:");
    	System.out.println("  -h [ --help ]         Produce help message");
    	System.out.println("  -v [ --version ]      Display Grive version");
    	System.out.println("  -a [ --auth ]         Request authorization token");
    	//System.out.println("  -V [ --verbose ]      Verbose mode. Enable more messages than normal.");
    	//System.out.println("  -d [ --debug ]        Enable debug level messages. Implies -v.");
    	//System.out.println("  -l [ --log ] arg      Set log output filename.");
    	System.out.println("  -f [ --force ]        Force grive to always download a file from Google Drive");
    	System.out.println("                        instead of uploading it.");
    	System.out.println("  --dry-run             Only detect which files need to be uploaded/downloaded,");
    	System.out.println("                        without actually performing them.");
    	
        System.exit(0);
    }
    
    private static boolean isLocallyIgnore(String filename){
    	if (localIgnorePatterns.length == 0){
    		return false;
    	}
    	
    	for (String pattern : localIgnorePatterns){
    		if (filename.matches(pattern)){
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    /**
     * Building a list of files which is modified since the last sync in a given folder.
     * 
     * .grive_state
     * { "last_sync": { "sec": 1429255944, "nsec": 743437000 }, "change_stamp": 10105 }
     * 
     * @param candidates a Map <file-name, file>
     * @param synFolder
     * @param lastSync
     */
    private static void readLocal(Map<String, java.io.File> candidates, java.io.File synFolder, long lastSync){
    	for(java.io.File file: synFolder.listFiles()){
    		if (!file.isFile()){
    			readLocal(candidates, file, lastSync);
    		} else if (file.lastModified() > lastSync
    				&& !isLocallyIgnore(file.getName())){
    			System.out.println("\tModified since last sync: [" + file.getName() + "]");
    			candidates.put(file.getName(), file);
    		}
    	}
    }
    
    /**
     * [https://developers.google.com/drive/v2/reference/files/list]
     * Retrieve a list of File resources.
     *
     * @param service Drive API service instance.
     * @return List of File resources.
     */
    private static List<File> retrieveAllFiles(Drive.Files driveFiles) throws IOException {
    	LOGGER.debug("Retrieving remote file's meta ...");
    	List<File> result = new ArrayList<File>(1000);
    	Files.List request = driveFiles.list();
    	request.setQ("");

    	do {
    		try {
    			FileList files = request.execute();

    			result.addAll(files.getItems());
    			request.setPageToken(files.getNextPageToken());
    		} catch (IOException e) {
    			LOGGER.error("An error occurred: " + e);
    			request.setPageToken(null);
    		}
    	} while (request.getPageToken() != null &&
    			request.getPageToken().length() > 0);

    	return result;
    }

    private static void syncFiles(Map<String, java.io.File> candidates) throws Exception {
    	Drive service = new DriveFactory(false).getInstance();
		Drive.Files driveFiles = service.files();
		List<File> remoteFiles = retrieveAllFiles(driveFiles);
		for(File file : remoteFiles) {
			System.out.println(String.format("Title: [%s], Id: [%s], Size: [%d], Parents: [%s]",
					file.getTitle(), file.getId(), file.getFileSize(), file.getParents().toString() ));
			String filename = file.getTitle();
			if (candidates.containsKey(filename)) {
				//If file was modified.
				//TODO ? java.io.File#length() can be slow. 
				//TODO check filesize b4 md5 will quicker ?
				if(!FileUtils.getMd5Checksum(candidates.get(filename)).equals(file.getMd5Checksum())) {
					if (DRY_RUN){
						System.out.println("\tChanged: [" + filename + "]");
					} else {
						//@See GoogleDriveFileSyncManager.updateFile
						System.out.println("\tUpdating: [" + filename + "]...");
						File body = new File();
						body.setTitle(filename);
						FileContent mediaContent = new FileContent("*/*", candidates.get(filename));
						driveFiles.update(file.getId(), body, mediaContent).execute();
					}
				}
				
				candidates.remove(filename);
			} else {
				//TODO check if it is directory 
				//TODO check if it is deleted/trash
				//FIXME
//				System.out.println("\tExisting in remote but not local: [" + filename + "]");
			}
		}
		
		for(Entry<String, java.io.File> entry: candidates.entrySet()){
			//TODO The whole list or only part of of the remote list ?
			System.out.println("\tUploading to remote: [" + entry.getKey() + "]...");
			File body = new File();
			body.setTitle(entry.getKey());
			FileContent mediaContent = new FileContent("*/*", entry.getValue());
			File file = service.files().insert(body, mediaContent).execute();
			System.out.println("File ID: " + file.getId());
		}
    }
    
    private static String[] localIgnorePatterns;
    
	public static void main(String[] args) throws Exception{
		//Load settings.
		Properties appStates = FileUtils.getAppStates();
		long lastSync = Long.valueOf(appStates.getProperty(APP_LAST_SYNC, "0"));
		Properties appProperties = FileUtils.getProperties(FileUtils.APP_PROPERTY_FILE);
		final String syncFoler = appProperties.getProperty("syncFolder");
		localIgnorePatterns = appProperties.getProperty("localIgnorePattern", "").split("\\|");
		
		LOGGER.info("Reading local directories ...");
		Map<String, java.io.File> candidates = new HashMap<>();
		readLocal(candidates, new java.io.File(syncFoler), lastSync);
		
		LOGGER.info("Synchronizing folders" + (DRY_RUN? " [dry-run]" : " ..."));
		syncFiles(candidates);
		
		appStates.setProperty(APP_LAST_SYNC, Long.toString(System.currentTimeMillis()));
		FileUtils.saveAppStates();
		LOGGER.info("Done!");
	}
}
