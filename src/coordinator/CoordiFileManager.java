package coordinator;

import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class CoordiFileManager {

	// public static String folderLocation = "Server/";

	private static HashMap<String, Monitor> fileMap = new HashMap<>();

	public static List<String> findFilesFromServerToGiveBackToClient(
			List<String> filenamesFromClient) {
		List<String> filesOfServer = new ArrayList<>();

		// File folder = new File(folderLocation);
		// File[] listOfFiles = folder.listFiles();
		List<String> filenames = CoordiServerFileManager
				.getAllFilenamesFromServers();
		for (String f : filenames) {
			if (!filenamesFromClient.contains(f)
					&& CoordiServerFileManager.getUpdateType(f).equals(
							UpdateType.MODIFY))
				filesOfServer.add(f);
		}

		return filesOfServer;
	}

	// will load all files from Server folder and assign each a monitor for
	// mutex.
	public static void initializeFileLocks() {
		// File folder = new File(folderLocation);
		// File[] listOfFiles = folder.listFiles();
		List<String> filenames = CoordiServerFileManager
				.getAllFilenamesFromServers();
		for (String f : filenames) {
			fileMap.put(f, new Monitor());
		}
	}

	public static boolean clientHasALaterCopy(String filename, Timestamp t) {
		// File serverFile = new File(folderLocation + filename);
		Long serverTime = CoordiServerFileManager.getTimeLastModified(filename);
		if (!CoordiServerFileManager.fileExists(filename)
				|| t.getTime() > serverTime)
			return true;
		else
			return false;
	}

	public static boolean equalTimeModified(String filename, Timestamp t) {
		// File serverFile = new File(folderLocation + filename);
		Long serverTime = CoordiServerFileManager.getTimeLastModified(filename);
		if (t.getTime() == serverTime)
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

	public static void writeToFile(String filename, String content,
			Timestamp lastModified) throws UnknownHostException, IOException {
		CoordiServerFileManager.saveFile(filename, content, lastModified);
	}

	public static String readFile(String filename) throws UnknownHostException,
			IOException {
		return CoordiServerFileManager.loadFileContent(filename);
	}

	public static void deleteFile(String filename, Timestamp timeLastModified)
			throws IOException {
		CoordiServerFileManager.deleteFile(filename, timeLastModified);
	}

	public static HashSet<String> getDeletedFiles() {
		return CoordiServerFileManager.getDeletedFiles();
	}
}
