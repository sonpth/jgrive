package com.github.sonpth.jgrive.model;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import com.github.sonpth.jgrive.utils.FileUtils;

/**
 * @author Phan Son <https://github.com/sonpth>
 *
 */
public class LocalTreeNode implements TreeNode {
	private LocalTreeNode parent;
	private File file;
	private List<LocalTreeNode> children;
	
	public LocalTreeNode(File file, LocalTreeNode parent) {
		this.file = file;
		this.parent = parent;
	}

	public static LocalTreeNode getInstance(final String path) {
		File file = new File(path);
		if (!file.exists()) {
			throw new IllegalArgumentException("The given Path must exits!");
		}
		
		LocalTreeNode result = new LocalTreeNode(file, null);
		
		return result;
	}

	@Override
	public String getName() {
		return this.file.getName();
	}

	@Override
	public TreeNode getParent() {
		return parent;
	}

	@Override
	public List<LocalTreeNode> getChildren() {
		if (file.isFile() || children != null) {
			return children;
		}

		children = new ArrayList<>();
		for (File child : file.listFiles()) {
			children.add(new LocalTreeNode(child, this));
		}
		return children;
	}

	@Override
	public boolean isFolder() {
		return file.isDirectory();
	}

	@Override
	public String getMd5Checksum() {
		if (file.isDirectory()) {
			throw new UnsupportedOperationException("Unsupported for path [" 
					+ file.getAbsolutePath() 
					+ "]");
		}
		
		try {
			return FileUtils.getMd5Checksum(file);
		} catch (NoSuchAlgorithmException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getFileSize() {
		if (file.isDirectory()) {
			throw new UnsupportedOperationException("Unsupported for path [" 
					+ file.getAbsolutePath() 
					+ "]");
		}

		return file.length();
	}

	@Override
	public long getLastModified() {
		return file.lastModified();
	}
	
	public File getFile() {
		return file;
	}
}
