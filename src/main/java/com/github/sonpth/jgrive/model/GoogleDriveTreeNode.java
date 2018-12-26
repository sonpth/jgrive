package com.github.sonpth.jgrive.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

/**
 * @author Phan Son <https://github.com/sonpth>
 *
 */
public class GoogleDriveTreeNode implements TreeNode {
	private final Log logger = LogFactory.getLog(this.getClass());

	public GoogleDriveTreeNode(File file, Drive drive) {
		this.file = file;
		this.drive = drive;
	}

	public static final String FOLDER_MIME_TYPE = "application/vnd.google-apps.folder";
	public static final String FOLDER_TYPE_FILTER = "mimeType = '" + FOLDER_MIME_TYPE + "'";
	public static final String ROOT_FILE_FILTER = "'root' in parents";

	private File file;
	private Drive drive;
	private List<GoogleDriveTreeNode> children;

	public static GoogleDriveTreeNode getInstance(Drive drive) {
		GoogleDriveTreeNode result = new GoogleDriveTreeNode(null, drive);
		result.drive = drive;
		return result;
	}

	@Override
	public String getName() {
		if (file == null) {
			return "root";
		} else {
			return file.getTitle();
		}
	}

	@Override
	public TreeNode getParent() {
		if (file == null) {
			return null;
		} else {
			// TODO Auto-generated method stub
			return null;
//			return file.getParents().get(0).geti
		}
	}

	@Override
	public List<GoogleDriveTreeNode> getChildren() {
		if (!isFolder() || children != null) {
			return children;
		}

		children = new ArrayList<>();
		final String query;
		if (file == null) {
			query = ROOT_FILE_FILTER;
		} else {
			query = new StringBuilder("'").append(file.getId()).append('\'')
					.append(" in parents").toString();
		}
		
		if (logger.isDebugEnabled()) {
			logger.debug(query);
		}

		try {
			Files.List request = drive.files().list();
			request.setQ(query);
			FileList files = request.execute();
			for (File file: files.getItems()) {
				children.add(new GoogleDriveTreeNode(file, drive));
			}
			return children;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isFolder() {
		if (file == null) {
			return true;
		} else {
			return FOLDER_MIME_TYPE.equals(file.getMimeType());
		}
	}

	@Override
	public String getMd5Checksum() {
		if (isFolder()) {
			throw new UnsupportedOperationException("Unsupported for path [" 
					+ getName() 
					+ "]");
		}
		
		return file.getMd5Checksum();
	}

	@Override
	public long getFileSize() {
		if (isFolder()) {
			throw new UnsupportedOperationException("Unsupported for path [" 
					+ getName() 
					+ "]");
		}

		return file.getFileSize();
	}

	@Override
	public long getLastModified() {
		if (file != null) {
			return file.getModifiedDate().getValue();
		}

		//TODO root modified time ?
		return -1;
	}
	
	public Drive getDrive() {
		return drive;
	}
	
	public String getId(){
		if (file != null) {
			return file.getId();
		}

		//FIXME return root ID
		return "root";
	}
}
