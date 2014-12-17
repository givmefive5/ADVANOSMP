package coordinator;

import indie.GSONConverter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import model.FileRep;

import org.json.JSONException;
import org.json.JSONObject;

public class CoordiServerFileManager {

	private static int indexForSavingFile = 0;

	private static Map<String, FileUpdateInfo> lastModified = new HashMap<>();

	private static HashSet<String> filesDeleted = new HashSet<>();

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
			IOException, JSONException {
		if (lastModified.get(filename) != null
				&& getUpdateType(filename).equals(UpdateType.MODIFY))
			saveOldFile(filename, content);
		else
			saveNewFile(filename, content);
		lastModified.put(filename, new FileUpdateInfo(UpdateType.MODIFY,
				timeLastModified.getTime()));
		filesDeleted.remove(filename);
	}

	private static void saveOldFile(String filename, String content)
			throws UnknownHostException, IOException, JSONException {
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
			throws UnknownHostException, IOException, JSONException {
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
			IOException, JSONException {
		Socket socket = new Socket(ipAddress, portNumber);

		ObjectOutputStream out = new ObjectOutputStream(
				socket.getOutputStream());

		JSONObject json = new JSONObject();
		json.put("actionType", "Save");

		FileRep f = new FileRep(filename, content);
		json.put("file", GSONConverter.convertObjectToJSON(f));

		out.writeObject(json.toString());
		out.flush();

		socket.close();
		// PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		// BufferedReader in = new BufferedReader(new InputStreamReader(
		// socket.getInputStream()));
		// out.println("SAVE FILE");
		//
		// out.println(filename + "###" + System.currentTimeMillis() + "###"
		// + content + "~!@#$");
		// // signal the server that no more files will be sent
		// out.println("END OF TRANSACTION");
		// String line;
		// while ((line = in.readLine()) != null) {
		// if (line.equals("FINISHED")) {
		// break;
		// }
		// }
		// socket.close();
	}

	public static String loadFileContent(String filename)
			throws UnknownHostException, IOException, JSONException,
			ClassNotFoundException {
		ServerInfo si = findServerThatHasFile(filename);

		if (si == null) {
			System.out
					.println("Two-thirds of the server is probably down, the entire system will now crash. =D");
		}
		Socket socket = new Socket(si.getIpAddress(), si.getPortNumber());

		ObjectOutputStream out = new ObjectOutputStream(
				socket.getOutputStream());

		JSONObject json = new JSONObject();
		json.put("actionType", "Load");

		FileRep f = new FileRep(filename);
		json.put("file", GSONConverter.convertObjectToJSON(f));

		out.writeObject(json.toString());
		out.flush();

		ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
		String jsonString = (String) in.readObject();

		JSONObject jsonObject = GSONConverter
				.convertJSONStringToObject(jsonString);

		FileRep fi = GSONConverter.getGSONObjectGivenJsonObject(
				jsonObject.getJSONObject("file"), FileRep.class);

		socket.close();

		return fi.getContent();

		// ServerInfo si = findServerThatHasFile(filename);
		// if (si != null) {
		// Socket socket = new Socket(si.getIpAddress(), si.getPortNumber());
		// PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		// BufferedReader in = new BufferedReader(new InputStreamReader(
		// socket.getInputStream()));
		// out.println("LOAD FILE");
		// out.println(filename);
		// System.out.println("File found from "
		// + si.getAddressWithPortNumber());
		// String line;
		// StringBuilder sb = new StringBuilder();
		// while ((line = in.readLine()) != null) {
		// if (sb.toString() != null && !sb.toString().equals("")
		// && !line.equals("END OF TRANSACTION"))
		// sb.append("\n");
		//
		// if (line.equals("END OF TRANSACTION")) {
		// System.out.println(sb.toString());
		// return sb.toString();
		// } else {
		// sb.append(line);
		// }
		// }
		//
		// } else
		// System.out.println("No Server Found Holding the File");
		// return null;
	}

	public static void checkAndDeletePendingFiles() throws IOException,
			JSONException {
		for (String filename : filesDeleted) {
			deleteFile(filename);
		}
	}

	public static void deleteFile(String filename, Timestamp timeLastModified)
			throws IOException, JSONException {
		deleteFile(filename);
		lastModified.put(filename, new FileUpdateInfo(UpdateType.DELETE,
				timeLastModified.getTime()));
		filesDeleted.add(filename);
	}

	private static void deleteFile(String filename) throws IOException,
			JSONException {
		ServerInfo[] serversContainingFile = getAllServersContainingFile(filename);
		boolean hasFailed = false;
		for (ServerInfo si : serversContainingFile) {
			if (si.isAlive()) {
				deleteFileFromServer(si.getIpAddress(), si.getPortNumber(),
						filename);
				int index = ServerHandler.getServerInfos().indexOf(si);
				ServerHandler.removeFileFromServer(index, filename);
			}
			System.out.println("Deleted " + filename + " from "
					+ si.getAddressWithPortNumber());
		}
	}

	private static void deleteFileFromServer(String ipAddress, int portNumber,
			String filename) throws IOException, JSONException {
		Socket socket = new Socket(ipAddress, portNumber);

		ObjectOutputStream out = new ObjectOutputStream(
				socket.getOutputStream());

		JSONObject json = new JSONObject();
		json.put("actionType", "Delete");

		FileRep f = new FileRep(filename);
		json.put("file", GSONConverter.convertObjectToJSON(f));

		out.writeObject(json.toString());
		out.flush();

		socket.close();
		// PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		// BufferedReader in = new BufferedReader(new InputStreamReader(
		// socket.getInputStream()));
		// out.println("DELETE FILE");
		// out.println(filename);
		// socket.close();
	}

	public static Long getTimeLastModified(String filename) {
		if (lastModified.get(filename) != null)
			return lastModified.get(filename).getLastModified();
		else
			return null;
	}

	public static UpdateType getUpdateType(String filename) {
		if (lastModified.get(filename) != null)
			return lastModified.get(filename).getUpdateType();
		else
			return null;
	}

	private static ServerInfo findServerThatHasFile(String filename) {

		for (ServerInfo si : ServerHandler.getServerInfos()) {
			if (si.isAlive() && si.hasFile(filename) && si.isReady(filename))
				return si;
		}
		return null;
	}

	public static void recoverMissingFilesOfServer(ServerInfo si)
			throws UnknownHostException, IOException, JSONException,
			ClassNotFoundException {
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

	public static HashSet<String> getDeletedFiles() {
		return filesDeleted;
	}
}
