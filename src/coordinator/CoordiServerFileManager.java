package coordinator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class CoordiServerFileManager {

	private static int indexForSavingFile = 0;

	private static Map<String, Long> lastModified = new HashMap<>();

	private static HashSet<String> pendingFilesToBeDeleted = new HashSet<>();

	public static List<String> getAllFilenamesFromServers() {
		return new ArrayList<String>(lastModified.keySet());
	}

	public static boolean fileExists(String filename) {
		if (lastModified.get(filename) == null)
			return false;
		else
			return true;
	}

	public static void saveFile(String filename, String content,
			Timestamp timeLastModified) throws UnknownHostException,
			IOException {

		if (lastModified.get(filename) != null)
			saveOldFile(filename, content);
		else
			saveNewFile(filename, content);
		lastModified.put(filename, timeLastModified.getTime());
	}

	private static void saveOldFile(String filename, String content)
			throws UnknownHostException, IOException {
		ServerInfo[] serversContainingFile = getAllServersContainingFile(filename);
		for (ServerInfo si : serversContainingFile) {
			FileInfo fileInfo;
			if (si.isAlive()) {
				saveFileContent(si.getIpAddress(), si.getPortNumber(),
						filename, content);
				fileInfo = new FileInfo(filename, true);
			} else {
				fileInfo = new FileInfo(filename, false);
			}
			System.out.println("Added " + filename + " to "
					+ si.getAddressWithPortNumber());
			int index = ServerHandler.getServerInfos().indexOf(si);
			ServerHandler.addFileInfo(index, fileInfo);
		}
	}

	private static ServerInfo[] getAllServersContainingFile(String filename) {
		List<ServerInfo> serverList = new ArrayList<>();
		for (ServerInfo si : ServerHandler.getServerInfos()) {
			if (si.hasFile(filename))
				serverList.add(si);
		}
		return serverList.toArray(new ServerInfo[serverList.size()]);
	}

	private static void saveNewFile(String filename, String content)
			throws UnknownHostException, IOException {
		int twoThirdsOfServers = ServerHandler.twoThirdsOfTotalServers();

		int count = 0;
		while (count < twoThirdsOfServers) {
			int index = indexForSavingFile + count;
			if (index >= ServerHandler.totalNumberOfServers()) {
				index = index - ServerHandler.totalNumberOfServers();
			}
			ServerInfo si = ServerHandler.get(index);
			FileInfo fileInfo;
			if (si.isAlive()) {
				saveFileContent(si.getIpAddress(), si.getPortNumber(),
						filename, content);
				fileInfo = new FileInfo(filename, true);
			} else {
				fileInfo = new FileInfo(filename, false);
			}
			System.out.println("Added " + filename + " to "
					+ ServerHandler.get(index).getAddressWithPortNumber());
			ServerHandler.addFileInfo(index, fileInfo);
			count++;
		}
		indexForSavingFile++;
		if (indexForSavingFile == ServerHandler.totalNumberOfServers())
			indexForSavingFile = 0;
	}

	private static void saveFileContent(String ipAddress, int portNumber,
			String filename, String content) throws UnknownHostException,
			IOException {
		Socket socket = new Socket(ipAddress, portNumber);

		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		out.println("SAVE FILE");

		out.println(filename + "###" + System.currentTimeMillis() + "###"
				+ content + "~!@#$");
		// signal the server that no more files will be sent
		out.println("END OF TRANSACTION");
		String line;
		while ((line = in.readLine()) != null) {
			if (line.equals("FINISHED")) {
				break;
			}
		}
		socket.close();
		lastModified.put(filename, System.currentTimeMillis());
	}

	public static String loadFileContent(String filename)
			throws UnknownHostException, IOException {
		ServerInfo si = findServerThatHasFile(filename);
		if (si != null) {
			Socket socket = new Socket(si.getIpAddress(), si.getPortNumber());
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			out.println("LOAD FILE");
			out.println(filename);
			System.out.println("File found from "
					+ si.getAddressWithPortNumber());
			String line;
			StringBuilder sb = new StringBuilder();
			while ((line = in.readLine()) != null) {
				if (sb.toString() != null && !sb.toString().equals("")
						&& !line.equals("END OF TRANSACTION"))
					sb.append("\n");

				if (line.equals("END OF TRANSACTION")) {
					System.out.println(sb.toString());
					return sb.toString();
				} else {
					sb.append(line);
				}
			}

		} else
			System.out.println("No Server Found Holding the File");
		return null;
	}

	public static void checkAndDeletePendingFiles() throws IOException {
		for (String filename : pendingFilesToBeDeleted) {
			deleteFile(filename);
		}
	}

	public static void deleteFile(String filename) throws IOException {
		ServerInfo[] serversContainingFile = getAllServersContainingFile(filename);
		boolean hasFailed = false;
		for (ServerInfo si : serversContainingFile) {
			System.out.println("Deleting " + filename + " from"
					+ si.getAddressWithPortNumber());
			if (si.isAlive()) {
				deleteFileFromServer(si.getIpAddress(), si.getPortNumber(),
						filename);
				int index = ServerHandler.getServerInfos().indexOf(si);
				ServerHandler.removeFileFromServer(index, filename);
			} else {
				hasFailed = true;
				pendingFilesToBeDeleted.add(filename);
			}
			System.out.println("Deleted " + filename + " from "
					+ si.getAddressWithPortNumber());
		}

		if (hasFailed == false)
			pendingFilesToBeDeleted.remove(filename);
	}

	private static void deleteFileFromServer(String ipAddress, int portNumber,
			String filename) throws IOException {
		Socket socket = new Socket(ipAddress, portNumber);

		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		out.println("DELETE FILE");
		out.println(filename);
		socket.close();
	}

	public static Long getTimeLastModified(String filename) {
		return lastModified.get(filename);
	}

	private static ServerInfo findServerThatHasFile(String filename) {

		for (ServerInfo si : ServerHandler.getServerInfos()) {
			if (si.isAlive() && si.hasFile(filename) && si.isReady(filename))
				return si;
		}
		return null;
	}

	public static void recoverMissingFilesOfServer(ServerInfo si)
			throws UnknownHostException, IOException {
		String[] unsavedFiles = getUnsavedFiles(si);

		for (String unsavedFile : unsavedFiles) {
			// from other server
			String content = loadFileContent(unsavedFile);
			saveFileContent(si.getIpAddress(), si.getPortNumber(), unsavedFile,
					content);
		}
	}

	public static String[] getUnsavedFiles(ServerInfo si) {
		List<FileInfo> files = si.getFileInfos();

		List<String> unsavedFiles = new ArrayList<>();
		for (FileInfo fi : files) {
			if (fi.isHasAdded() == false)
				unsavedFiles.add(fi.getFilename());
		}

		return unsavedFiles.toArray(new String[unsavedFiles.size()]);
	}
}
