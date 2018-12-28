package com.github.sonpth.jgrive;

import static com.github.sonpth.jgrive.utils.DriveUtils.updateLocalFile;
import static com.github.sonpth.jgrive.utils.DriveUtils.updateRemoteFile;
import static com.github.sonpth.jgrive.utils.DriveUtils.uploadFiles;
import static com.github.sonpth.jgrive.utils.FileUtils.APP_LAST_SYNC;

import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.sonpth.jgrive.model.GoogleDriveTreeNode;
import com.github.sonpth.jgrive.model.LocalTreeNode;

public class SimpleSyncWorker {
	private final Log logger = LogFactory.getLog(this.getClass());
	private final long lastSync;
	private final String localIgnorePatterns;
	private final List<FileFilter> fileFilters;
	
	public SimpleSyncWorker(Properties appProperties, Properties appStates){
		lastSync = Long.valueOf(appStates.getProperty(APP_LAST_SYNC, "0"));;

		fileFilters = new ArrayList<>();
		//For example, we don't want to re-sync a file we manually delete
		fileFilters.add(f -> f.isDirectory() || f.lastModified() > this.lastSync);
		//Explicitly declare to ignore.
		localIgnorePatterns = appProperties.getProperty("localIgnorePattern");
		if (localIgnorePatterns != null) {
			fileFilters.add(f -> !f.getName().matches(localIgnorePatterns));
		}
	}

	public void sync(LocalTreeNode localNode, GoogleDriveTreeNode remoteNode) {
		List<LocalTreeNode> localNodes = localNode.getChildren();
		List<GoogleDriveTreeNode> remoteNodes = remoteNode.getChildren();

		Iterator<LocalTreeNode> localIterator = localNodes.iterator();
		outerloop: while (localIterator.hasNext()) {
			LocalTreeNode ltn = localIterator.next();
			if (localIgnorePatterns != null && ltn.getName().matches(localIgnorePatterns)) {
				if (logger.isTraceEnabled()) {
					logger.trace("Ignore local [" + ltn.getName() + "] due to IngnorePattern filter.");
				}
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
				
				//[https://stackoverflow.com/questions/3620684/directory-last-modified-date]
				//[https://webapps.stackexchange.com/questions/37858/is-it-possible-to-see-the-date-time-for-a-google-drive-folder-for-the-most-recen]
/*				//If both folders have no change, no point to go further.
				if (ltn.isFolder() && ltn.getLastModified() < lastSync
						&& gdtn.isFolder() && gdtn.getLastModified() < lastSync) {
					remoteIterator.remove();
					localIterator.remove();
					continue outerloop;
				}
*/
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
				uploadFiles(ltn.getFile(), fileFilters,
						remoteNode.getId(), remoteNode.getDrive());
			} catch (IOException e) {
				logger.warn(e.getMessage());
			}
		}
	}
}
