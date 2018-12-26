package com.github.sonpth.jgrive.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

public abstract class DriveUtils {
	private static final Log LOGGER = LogFactory.getLog(DriveUtils.class);

	public static void uploadFile(java.io.File localFile, Drive service) throws IOException {
		File body = new File();
		body.setTitle(localFile.getName());
		FileContent mediaContent = new FileContent("*/*", localFile);
		File file = service.files().insert(body, mediaContent).execute();

		LOGGER.info(String.format("Uploaded file [%s] to remote with Id [%s]",
				localFile.getName(), file.getId()));
	}

	public static void updateRemoteFile(java.io.File localFile, String fileId, Drive service) throws IOException {
		File body = new File();
		body.setTitle(localFile.getName());
		FileContent mediaContent = new FileContent("*/*", localFile);
		File file = service.files().update(fileId, body, mediaContent).execute();

		LOGGER.info(String.format("Updated file [%s] to remote with Id [%s]",
				localFile.getName(), file.getId()));
	}

	public static void updateLocalFile(java.io.File localFile, String fileId, Drive service) throws IOException {
		OutputStream outputStream = new FileOutputStream(localFile);
		service.files().get(fileId).executeMediaAndDownloadTo(outputStream);

		LOGGER.info(String.format("Got file [%s] from remote with Id [%s]",
				localFile.getName(), fileId));
	}

}
