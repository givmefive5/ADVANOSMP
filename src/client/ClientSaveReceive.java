package client;

import indie.FileManager;
import indie.GSONConverter;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import model.FileRep;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.reflect.TypeToken;

public class ClientSaveReceive {
	private Socket socket;

	// Sending Message
	// private BufferedReader in;
	// private PrintWriter out;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	public static String folderPath;

	public ClientSaveReceive(String folderPath) throws UnknownHostException,
			IOException, ClassNotFoundException, JSONException {

		ClientSaveReceive.folderPath = folderPath + "/";

		String address = "localhost";
		int portNumber = 4441;
		socket = new Socket(address, portNumber);

		// out = new PrintWriter(socket.getOutputStream(), true);
		// in = new BufferedReader(new
		// InputStreamReader(socket.getInputStream()));
		out = new ObjectOutputStream(socket.getOutputStream());
		sendFiles();
		in = new ObjectInputStream(socket.getInputStream());
		receiveFiles();

		deleteFiles();

		socket.close();

	}

	private void deleteFiles() throws IOException, ClassNotFoundException,
			JSONException {

		String jsonString = (String) in.readObject();

		JSONObject json = GSONConverter.convertJSONStringToObject(jsonString);

		JSONArray jsonArray = json.getJSONArray("files");

		Type type = new TypeToken<List<FileRep>>() {
		}.getType();
		List<FileRep> files = GSONConverter.convertJSONToObjectList(
				jsonArray.toString(), type);

		for (FileRep f : files) {
			File file = new File(folderPath + f.getFilename());
			FileManager.deleteFile(file);
			System.out.println("Deleted : " + f.getFilename());
		}

		// String startLine = in.readLine(); // START OF DELETE
		//
		// String line;
		// while ((line = in.readLine()) != null) {
		// if (line.equals("END OF TRANSACTION")) {
		// // exits the loop after all files from server has been sent
		// break;
		// }
		// String filename = line;
		// File f = new File(folderPath + filename);
		// FileManager.deleteFile(f);
		// }

	}

	private void receiveFiles() throws IOException, ClassNotFoundException,
			JSONException {

		String jsonString = (String) in.readObject();

		JSONObject json = GSONConverter.convertJSONStringToObject(jsonString);

		JSONArray jsonArray = json.getJSONArray("files");

		Type type = new TypeToken<List<FileRep>>() {
		}.getType();
		List<FileRep> files = GSONConverter.convertJSONToObjectList(
				jsonArray.toString(), type);

		for (FileRep f : files) {
			File file = new File(folderPath + f.getFilename());
			FileManager.writeToFile(file, f.getContent());
			System.out.println("Received " + f.getFilename());
		}

		// String line;
		// StringBuilder sb = new StringBuilder();

		// String filename = null;
		// Timestamp dateModified;
		// while ((line = in.readLine()) != null) {
		// if (line.equals("END OF TRANSACTION")) {
		// // exits the loop after all files from client has been sent
		// break;
		// }
		// if (ResponseHandler.isStartOfFile(line)
		// && ResponseHandler.isEndOfFile(line)) {
		// // means that the file has a one line content
		// String[] tokens = line.split("###");
		// filename = tokens[0];
		// dateModified = new Timestamp(Long.valueOf(tokens[1]));
		// System.out.println("Name: " + filename);
		// System.out.println("Time: " + dateModified);
		// line = ResponseHandler.getFirstLineContent(line);
		//
		// sb.append(ResponseHandler.removeEndFileDelimiter(line));
		// System.out.println(sb.toString());
		// File f = new File(folderPath + filename);
		// FileManager.writeToFile(f, sb.toString());
		// System.out.println("Finished writing " + f.getAbsolutePath());
		//
		// sb = new StringBuilder();
		// } else if (ResponseHandler.isStartOfFile(line)) {
		// // means that the file has more than one line
		// // extracts the file name and time modified
		// String[] tokens = line.split("###");
		// System.out.println("Name: " + tokens[0]);
		// System.out.println("Time: "
		// + new Timestamp(Long.valueOf(tokens[1])));
		// filename = tokens[0];
		// dateModified = new Timestamp(Long.valueOf(tokens[1]));
		// line = ResponseHandler.getFirstLineContent(line);
		// sb.append(line);
		// } else if (ResponseHandler.isEndOfFile(line)) {
		// // if reader sees a end of file delimeter, it proceeds to build
		// // the next file
		// sb.append(ResponseHandler.removeEndFileDelimiter(line));
		// System.out.println(sb.toString());
		// File f = new File(folderPath + filename);
		// FileManager.writeToFile(f, sb.toString());
		// System.out.println("Finished writing " + f.getAbsolutePath());
		// sb = new StringBuilder();
		// } else
		// // middle liners in a file
		// sb.append(line);
		//
		// if (line != null && !ResponseHandler.isEndOfFile(line)) {
		// sb.append("\n");
		// }
		//
		// }
	}

	private void sendFiles() throws IOException, JSONException {
		// simply sends all of the files to the server
		System.out.println("Sending Files");
		List<FileRep> files = new ArrayList<>();

		File folder = new File(folderPath);
		for (File f : folder.listFiles()) {
			// signal that that's the end of a file
			// out.println(f.getName() + "###" + f.lastModified() + "###"
			// + FileManager.readFile(f) + "~!@#$");
			files.add(new FileRep(f.getName(), f.lastModified(), FileManager
					.readFile(f)));
		}

		JSONArray jsonArray = GSONConverter.convertListToJSONArray(files);
		JSONObject json = new JSONObject();
		json.put("classType", "Client");
		json.put("actionType", "Sync");
		json.put("files", jsonArray);

		out.writeObject(json.toString());
		out.flush();
		// out.println("Client");
		// System.out.println("Client");
		// out.println("Sync");
		// System.out.println("Sync");

		// signal the server that no more files will be sent
		// out.println("END OF TRANSACTION");
	}
}
