package com.github.sonpth.jgrive;

import static com.github.sonpth.jgrive.utils.DriveUtils.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.sonpth.jgrive.model.GoogleDriveTreeNode;
import com.github.sonpth.jgrive.model.LocalTreeNode;

public class SimpleSyncWorker {
	private final Log logger = LogFactory.getLog(this.getClass());
	private final long lastSync;
	
	public SimpleSyncWorker(long lastSync){
		this.lastSync = lastSync;
	}

	public void sync(LocalTreeNode localNode, GoogleDriveTreeNode remoteNode) {
		List<LocalTreeNode> localNodes = localNode.getChildren();
		List<GoogleDriveTreeNode> remoteNodes = remoteNode.getChildren();

		Iterator<LocalTreeNode> localIterator = localNodes.iterator();
		outerloop: while (localIterator.hasNext()) {
			LocalTreeNode ltn = localIterator.next();

			if (logger.isTraceEnabled()) {
				logger.trace("Processing local [" + ltn.getName() + "]");
			}

			Iterator<GoogleDriveTreeNode> remoteIterator = remoteNodes.iterator();
			while (remoteIterator.hasNext()) {
				GoogleDriveTreeNode gdtn = remoteIterator.next();

				if (logger.isTraceEnabled()) {
					logger.trace("Processing remote [" + gdtn.getName() + "]");
				}
				
				//If both folders have no change, no point to go further.
				if (ltn.isFolder() && ltn.getLastModified() < lastSync
						&& gdtn.isFolder() && gdtn.getLastModified() < lastSync) {
					remoteIterator.remove();
					localIterator.remove();
					continue outerloop;
				}

				try {
					if (ltn.getName().equals(gdtn.getName())) {
						if (ltn.isFolder() && gdtn.isFolder()) {
							logger.debug("Processing sub-folder [" + ltn.getName() + "]...");
							sync(ltn, gdtn);
						} else if (!ltn.isFolder() && !gdtn.isFolder()) {
							logger.debug("Processing file [" + ltn.getName() + "]...");
							if (ltn.getFileSize() != gdtn.getFileSize()
									|| !ltn.getMd5Checksum().equals(gdtn.getMd5Checksum())) {
								if (ltn.getLastModified() >= gdtn.getLastModified()) {
									updateRemoteFile(ltn.getFile(), gdtn.getId(), gdtn.getDrive());
								} else {
									updateLocalFile(ltn.getFile(), gdtn.getId(), gdtn.getDrive());
								}
							}
							
						} else {
							logger.warn("Found conflict - different type for resouce [" 
									+ ltn.getFile().getAbsolutePath()
									+ "]. IGNORED!");
						}
						
						//Remove processed item
						remoteIterator.remove();
						localIterator.remove();
						continue outerloop;
					}
				} catch (IOException e) {
					logger.warn(e.getMessage());
				}
			}
		}
		
		for(LocalTreeNode ltn: localNodes) {
			try {
				uploadFiles(ltn.getFile(), remoteNode.getId(), remoteNode.getDrive());
			} catch (IOException e) {
				logger.warn(e.getMessage());
			}
		}
	}
}
