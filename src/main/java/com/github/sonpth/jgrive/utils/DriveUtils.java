package com.github.sonpth.jgrive.utils;

import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.ParentReference;
import static com.github.sonpth.jgrive.model.GoogleDriveTreeNode.FOLDER_MIME_TYPE;

public abstract class DriveUtils {
	private static final Log LOGGER = LogFactory.getLog(DriveUtils.class);

	/**
	 * @param localFile
	 * @param fileFilters
	 * @param fileId Google Drive's FileId
	 * @param service 
	 * @throws IOException
	 */
	public static void uploadFiles(java.io.File localFile, List<FileFilter> fileFilters,
			String fileId, Drive service) throws IOException {
		for(FileFilter filter: fileFilters) {
			if (!filter.accept(localFile)) {
				LOGGER.debug(String.format("File [%s] failed to pass a FileFilter and will be ignored!",
						localFile.getAbsoluteFile()));
				return;
			}
		}

		File body = new File();
		body.setTitle(localFile.getName());

		if (localFile.isFile()) {
			body.setParents(Collections.singletonList(
				    new ParentReference().setId(fileId)));
			FileContent mediaContent = new FileContent("*/*", localFile);
			File file = service.files().insert(body, mediaContent)
					.setFields("id, parents")
					.execute();
	
			LOGGER.info(String.format("Uploaded file [%s] to remote with Id [%s]",
					localFile.getName(), file.getId()));
		} else {
//			System.out.println(localFile.getName());
			body.setMimeType(FOLDER_MIME_TYPE);
			body.setParents(Collections.singletonList(
				    new ParentReference().setId(fileId)));
			File folder = service.files().insert(body)
				    .setFields("id")
				    .execute();
			LOGGER.debug(String.format("Created a folder [%s] under [%s]",
					localFile.getName(), folder.getId()));
			for (java.io.File file: localFile.listFiles()) {
				uploadFiles(file, fileFilters, folder.getId(), service);
			}
		}
	}

	/**
	 * Update a *remote* file with given `fileId` by the `localFile`
	 * 
	 * @param localFile
	 * @param fileId
	 * @param service
	 * @throws IOException
	 */
	public static void updateRemoteFile(java.io.File localFile, String fileId, Drive service) throws IOException {
		if (!localFile.isFile()) {
			throw new IllegalArgumentException();
		}
		
		File body = new File();
		body.setTitle(localFile.getName());
		FileContent mediaContent = new FileContent("*/*", localFile);
		File file = service.files().update(fileId, body, mediaContent).execute();

		LOGGER.info(String.format("Updated file [%s] to remote with Id [%s]",
				localFile.getName(), file.getId()));
	}

	/**
	 * Update a *local* file with a remote file identified by `fileId`.
	 * 
	 * @param localFile
	 * @param fileId
	 * @param service
	 * @throws IOException
	 */
	public static void updateLocalFile(java.io.File localFile, String fileId, Drive service) throws IOException {
		if (!localFile.isFile()) {
			throw new IllegalArgumentException();
		}

		OutputStream outputStream = new FileOutputStream(localFile);
		service.files().get(fileId).executeMediaAndDownloadTo(outputStream);

		LOGGER.info(String.format("Got file [%s] from remote with Id [%s]",
				localFile.getName(), fileId));
	}

}
