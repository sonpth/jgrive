Overview
--------
A simple Java client to mimic GDrive after the client stop working due to Google deprecated its using API.

Configure Google Drive API Credential
----------------------------------------
1. Please follow the instructions to setup the Google account credential at: `https://developers.google.com/drive/web/quickstart/quickstart-java#step_1_enable_the_drive_api`
2. When creating the Client ID, choose: "OAuth Create new Client Id" -> "Installed application" (Other)
3. Copy and paste the CLIENT ID and CLIENT SECRET in `GoogleDriveServiceProvider.java`

Running the Project Locally
----------------------------------------
1. Locate the App.java in src/main/java source folder and right-click on it->Run As->Run Configurations, and fill a folder name in Program Parameters, such as "/mydrive/" or "C:\doc\".
2. When the program is running, you can copy/modify/delete files in that folder and check if the changes have been synced to your Google Drive account.

Note
----------------------------------------
1. In order to simplify the function, all the files will be sync-ed to the root folder in your Google Drive.
2. Only the first-level files will be monitored in your local folder. Sub-directories will not be monitored.
3. The project has been configured to use JDK 7. If you use other versions installed, please modify the pom.xml file. However, JDK 7+ (or 8) is recommended.

References:
----------------------------------------
* [Google Drive Java API | https://developers.google.com/drive/web/quickstart/quickstart-java].
* [Jumpstart | https://github.com/csupomona-cs585/ibox.git]
1. Watching a Directory for Changes. http://docs.oracle.com/javase/tutorial/essential/io/notification.html

