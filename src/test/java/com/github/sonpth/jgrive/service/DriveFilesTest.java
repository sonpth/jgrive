package com.github.sonpth.jgrive.service;

import java.io.IOException;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static com.github.sonpth.jgrive.model.GoogleDriveTreeNode.*;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

/**
 * @author Phan Son <https://github.com/sonpth>
 * 
 * This class is mainly responsible for checking if the API still supports what we are doing.
 * For example, a valid field `title` in V2 has been changed to `name` in V3.
 * [https://stackoverflow.com/questions/34735220/google-drive-api-query-by-name-returns-invalid/34776881]
 *
 */
public class DriveFilesTest {
	
	Drive.Files driveFiles;
	
	@BeforeEach
	public void setUp() throws IOException {
		Drive service = new DriveFactory(false).getInstance();
		driveFiles = service.files();
	}
	
	@Test
	public void testGetFilesInRoot() throws IOException {
    	Files.List request = driveFiles.list();
    	
    	request.setQ( ROOT_FILE_FILTER 
    				+ " and " 
    				+ FOLDER_TYPE_FILTER 
    			);
    	request.set("orderBy", "title");
		FileList files = request.execute();
		for (File file: files.getItems()) {
			System.out.println(String.format("Title: [%s], Id: [%s]",
					file.getTitle(), file.getId()));
			
			assertTrue(file.getParents().size() == 1);
			assertTrue(file.getParents().get(0).getIsRoot());
			assertEquals(FOLDER_MIME_TYPE, file.getMimeType());
		}
	}
	
	@Disabled
	@Test
	public void testGetFileByName() throws IOException {
    	Files.List request = driveFiles.list();
    	request.setQ("title = 'backup'"
    			+ " and " 
    			+ "mimeType = 'application/vnd.google-apps.folder'"
    			);
		FileList files = request.execute();
		assertFalse(files.isEmpty());
	}
}