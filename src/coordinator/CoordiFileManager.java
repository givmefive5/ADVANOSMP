package coordinator;

import indie.FileManager;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CoordiFileManager extends FileManager {

	public static String folderLocation = "Server/";

	private static HashMap<String, Monitor> fileMap = new HashMap<>();

	public static List<File> findFilesFromServerToGiveBackToClient(
			List<String> filenamesFromClient) {
		List<File> filesOfServer = new ArrayList<>();

		File folder = new File(folderLocation);
		File[] listOfFiles = folder.listFiles();

		for (File f : listOfFiles) {
			if (!filenamesFromClient.contains(f.getName()))
				filesOfServer.add(f);
		}

		return filesOfServer;
	}

	// will load all files from Server folder and assign each a monitor for
	// mutex.
	public static void initializeFileLocks() {
		File folder = new File(folderLocation);
		File[] listOfFiles = folder.listFiles();
		for (File f : listOfFiles) {
			String filename = f.getName();
			fileMap.put(filename, new Monitor());
		}
	}

	public static boolean clientHasALaterCopy(String filename, Timestamp t) {
		File serverFile = new File(folderLocation + filename);
		if (!serverFile.exists() || t.getTime() > serverFile.lastModified())
			return true;
		else
			return false;
	}

	public static synchronized void acquireLockOfFile(String filename) {

		if (fileMap.get(filename) == null)
			fileMap.put(filename, new Monitor());

		Monitor m = fileMap.get(filename);
		m.waiting();

		// if filename in keys, get monitor and lock
		// else create new monitor, put to fileMap
		// acquirelock
	}

	public static synchronized void releaseLockOfFile(String name) {

		Monitor m = fileMap.get(name);
		m.signaling();

		// get monitor of file from filemap
		// release lock
	}
}
