package com.github.sonpth.jgrive;

import static com.github.sonpth.jgrive.utils.FileUtils.*;
import static com.github.sonpth.jgrive.utils.FileUtils.getProperties;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.github.sonpth.jgrive.model.GoogleDriveTreeNode;
import com.github.sonpth.jgrive.model.LocalTreeNode;
import com.github.sonpth.jgrive.service.DriveFactory;
import com.github.sonpth.jgrive.utils.FileUtils;

/**
 * @author Phan Son <https://github.com/sonpth>
 *
 */
public class JGrive2 {
	private static final Log LOGGER = LogFactory.getLog(JGrive2.class);
	
	public static void main(String[] args) throws Exception{
		final long start = System.currentTimeMillis();
		
		LOGGER.info("Loading settings ...");
		Properties appStates = FileUtils.getAppStates();
		final long lastSync = Long.valueOf(appStates.getProperty(APP_LAST_SYNC, "0"));
		Properties appProperties = getProperties(APP_PROPERTY_FILE);
		final String syncFolder = appProperties.getProperty("syncFolder");
//		localIgnorePatterns = appProperties.getProperty("localIgnorePattern", "").split("\\|");
		
		LOGGER.info("Synchronizing folders ...");
		SimpleSyncWorker worker = new SimpleSyncWorker(lastSync);
		worker.sync(LocalTreeNode.getInstance(syncFolder),
				GoogleDriveTreeNode.getInstance(new DriveFactory(false).getInstance()));
		
		LOGGER.info("Persisting states ...");
		final long end = System.currentTimeMillis();
		appStates.setProperty(FileUtils.APP_LAST_SYNC, Long.toString(System.currentTimeMillis()));
		FileUtils.saveAppStates();

		System.out.println(String.format("Time eslapsed: %d ms. Done!", end - start));
	}
}
