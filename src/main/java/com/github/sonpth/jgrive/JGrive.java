package com.github.sonpth.jgrive;

import static com.github.sonpth.jgrive.service.FileUtils.APP_LAST_SYNC;
import static com.github.sonpth.jgrive.service.FileUtils.APP_STATES;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
	//FIXME current working directory OR application.properties.
	private static final String SYN_FOLDER = "/home/pson/cloud";
	
	private static final boolean dryRun = true;
	
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
    
    /**
     * .grive_state
     * { "last_sync": { "sec": 1429255944, "nsec": 743437000 }, "change_stamp": 10105 }
     * @param lastSync
     */
    private static Map<String, java.io.File> readLocal(long lastSync){
    	System.out.println("Reading local directories");
    	java.io.File synFolder = new java.io.File(SYN_FOLDER);
    	
    	Map<String, java.io.File> candidates = new HashMap<>();
    	
    	for(java.io.File file: synFolder.listFiles()){
    		if (file.isFile() && file.lastModified() > lastSync){
    			System.out.println("\t[" + file.getName() + "]");
    			candidates.put(file.getName(), file);
    		}
    	}
    	
    	return candidates;
    }
    
    private static void syncFiles(Map<String, java.io.File> candidates) throws Exception {
		System.out.println("Synchronizing folders" + (dryRun? " [dry-run]" : ""));
		Drive service = new DriveFactory(false).getInstance();

		Drive.Files driveFiles = service.files();
		Files.List request = driveFiles.list();
		FileList files = request.execute();
		for(File file : files.getItems()) {
			//System.out.println(String.format("Title: [%s], [%s] [%d]", file.getTitle(), file.getId(), file.getFileSize()));
			String filename = file.getTitle();
			//TODO ? java.io.File#length() can be slow. 
			if (candidates.containsKey(filename)
					//TODO check filesize b4 md5 will quicker ?
					&& !FileUtils.getMd5Checksum(SYN_FOLDER+ "/" + filename).equals(file.getMd5Checksum())) {
				if (dryRun){
					System.out.println("\tChanged: [" + filename + "]");
				} else {
					//@See GoogleDriveFileSyncManager.updateFile
					System.out.println("\tUdpating: [" + filename + "]...");
					File body = new File();
					body.setTitle(filename);
					FileContent mediaContent = new FileContent("*/*", candidates.get(filename));
					driveFiles.update(file.getId(), body, mediaContent).execute();
				}
			}
		}
    }
    
	public static void main(String[] args) throws Exception{
		Properties appStates = FileUtils.getProperties(APP_STATES);
		long lastSync = Long.valueOf(appStates.getProperty(APP_LAST_SYNC, "0"));
		syncFiles(readLocal(lastSync));
		
		appStates.setProperty(APP_LAST_SYNC, Long.toString(System.currentTimeMillis()));
		FileUtils.saveStates(appStates);
		System.out.println("Done!");
	}
}
