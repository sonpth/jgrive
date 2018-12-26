package com.github.sonpth.jgrive.model;

import java.util.List;

public interface TreeNode {
	/**
	 * Assumption: filename is unique in the context (e.g: is is not possible for file and sub- folder
	 * has the same name.
	 * 
	 * @return filename
	 */
	String getName();
	
	/**
	 * @return
	 */
	TreeNode getParent();
	
	/**
	 * @return
	 */
	List<? extends TreeNode> getChildren();
	
	/**
	 * @return
	 */
	boolean isFolder();
	
	/**
	 * Only available if the node is a file (e.g: not a folder).
	 * @return
	 */
	String getMd5Checksum();
	
	/**
	 * @return The size of the file in bytes.
	 */
	long getFileSize();
	
	
	/**
	 * @return
	 */
	long getLastModified();
	
}
