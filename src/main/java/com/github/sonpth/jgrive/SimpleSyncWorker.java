package com.github.sonpth.jgrive;

import static com.github.sonpth.jgrive.utils.DriveUtils.updateLocalFile;
import static com.github.sonpth.jgrive.utils.DriveUtils.updateRemoteFile;

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
		while (localIterator.hasNext()) {
			LocalTreeNode ltn = localIterator.next();
			if (ltn.isFolder() && ltn.getLastModified() < lastSync) {
				continue;
			}

			if (logger.isTraceEnabled()) {
				logger.trace("Processing local [" + ltn.getName() + "]");
			}

			Iterator<GoogleDriveTreeNode> remoteIterator = remoteNodes.iterator();
			while (remoteIterator.hasNext()) {
				GoogleDriveTreeNode gdtn = remoteIterator.next();

				if (logger.isTraceEnabled()) {
					logger.trace("Processing remote [" + gdtn.getName() + "]");
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
							logger.warn("Found conflict - different type for resouce named [" + ltn.getName()
									+ "]. IGNORED!");
						}
						
						//Remove processed item
//						localIterator.remove();
//						remoteIterator.remove();
					}
				} catch (IOException e) {
					logger.warn(e.getMessage());
				}
			}
		}
	}
}
