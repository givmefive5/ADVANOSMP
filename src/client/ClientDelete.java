package client;

import indie.FileManager;
import indie.GSONConverter;

import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import model.FileRep;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ClientDelete {
	private Socket socket;

	// Sending Message
	// private BufferedReader in;
	// private PrintWriter out;
	// private ObjectInputStream in;
	private ObjectOutputStream out;
	public static String folderPath;

	public ClientDelete(String folderPath) throws UnknownHostException,
			IOException, JSONException {

		ClientDelete.folderPath = folderPath + "/";

		String address = "localhost";
		int portNumber = 4441;
		socket = new Socket(address, portNumber);

		// out = new PrintWriter(socket.getOutputStream(), true);
		// in = new BufferedReader(new
		// InputStreamReader(socket.getInputStream()));

		// in = new ObjectInputStream(socket.getInputStream());
		out = new ObjectOutputStream(socket.getOutputStream());

		checkForDeletedFiles();
		socket.close();

	}

	private void checkForDeletedFiles() throws IOException, JSONException {

		File folder = new File(folderPath);
		List<String> newFiles = FileManager.getFileNames(folder);
		String[] filesToDelete = ClientDriver.getDeletedFiles(newFiles);
		System.out.println(newFiles.size() + " " + filesToDelete.length);
		if (filesToDelete.length > 0) {
			List<FileRep> files = new ArrayList<>();
			for (String f : filesToDelete) {
				files.add(new FileRep(f));
				System.out.println("Asked server to delete : " + f);
			}
			JSONArray jsonArray = GSONConverter.convertListToJSONArray(files);
			JSONObject json = new JSONObject();
			json.put("classType", "Client");
			json.put("actionType", "Delete");
			json.put("files", jsonArray);
			out.writeObject(json.toString());
			out.flush();
		}
		ClientDriver.replaceList(newFiles);
		//
		// if (filesToDelete.length > 0) {
		// out.println("Client");
		// System.out.println("Client");
		// out.println("Delete");
		// System.out.println("Delete");
		// for (String f : filesToDelete) {
		// out.println(f);
		// System.out.println("Asked server to delete " + f);
		// }
		// out.println("END OF TRANSACTION");
		// }
		// ClientDriver.replaceList(newFiles);
	}
}
