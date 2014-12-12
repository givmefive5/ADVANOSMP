package coordinator;

import indie.GSONConverter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import model.FileRep;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.reflect.TypeToken;

public class ClientHandlerThread extends Thread {
	private final Socket socket;

	ObjectInputStream in;
	ObjectOutputStream out;

	JSONObject json;

	public ClientHandlerThread(Socket socket, ObjectInputStream in,
			JSONObject json) throws IOException {
		this.socket = socket;
		this.in = in;
		out = new ObjectOutputStream(socket.getOutputStream());

		this.json = json;
	}

	@Override
	public void run() {
		try {
			if (json.getString("actionType").equals("Delete")) {
				deleteFiles();
			} else if (json.getString("actionType").equals("Sync")) {
				List<String> filesToSendBack = syncFiles(json);
				sendFiles(filesToSendBack);

				askClientToDeleteFiles();
			}

		} catch (JSONException | IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void deleteFiles() throws IOException, JSONException {
		JSONArray jsonArray = json.getJSONArray("files");

		Type type = new TypeToken<List<FileRep>>() {
		}.getType();
		List<FileRep> files = GSONConverter.convertJSONToObjectList(
				jsonArray.toString(), type);

		for (FileRep f : files) {
			Timestamp timeDeleted = new Timestamp(System.currentTimeMillis());
			CoordiFileManager.acquireLockOfFile(f.getFilename());
			CoordiFileManager.deleteFile(f.getFilename(), timeDeleted);
			CoordiFileManager.releaseLockOfFile(f.getFilename());
		}

		// String line;
		// while ((line = in.readLine()) != null) {
		// if (line.equals("END OF TRANSACTION")) {
		// // exits the loop after all files from client has been sent
		// break;
		// }
		// String filename = line;
		// Timestamp timeDeleted = new Timestamp(System.currentTimeMillis());
		// CoordiFileManager.deleteFile(filename, timeDeleted);
		// }
	}

	private void askClientToDeleteFiles() throws JSONException, IOException {
		List<String> toDelete = new ArrayList<>();

		for (String s : CoordiServerFileManager.getDeletedFiles()) {
			toDelete.add(s);
		}

		List<FileRep> files = FileRep.convertFilenamesToFileReps(toDelete);
		JSONObject json = new JSONObject();
		JSONArray jsonArray = GSONConverter.convertListToJSONArray(files);
		json.put("files", jsonArray);
		System.out.println("To Delete in Client: " + json);
		out.writeObject(json.toString());
		out.flush();

	}

	private void sendFiles(List<String> filenames) throws IOException,
			JSONException, ClassNotFoundException {

		JSONObject json = new JSONObject();

		List<FileRep> files = new ArrayList<>();

		for (String s : filenames) {
			CoordiFileManager.acquireLockOfFile(s);
			if (CoordiFileManager.isDeleted(s) == false)
				files.add(new FileRep(s, CoordiFileManager.readFile(s)));
			CoordiFileManager.releaseLockOfFile(s);
		}

		JSONArray jsonArray = GSONConverter.convertListToJSONArray(files);
		json.put("files", jsonArray);
		System.out.println("To Sendback to Client: " + json);

		out.writeObject(json.toString());
		out.flush();
		// // simply sends all of the files to the server
		// for (String f : filenames) {
		// // signal that that's the end of a file
		// String content = CoordiFileManager.readFile(f);
		// if (content != null)
		// out.println(f + "###" + System.currentTimeMillis() + "###"
		// + content + "~!@#$");
		// }
		// // signal the server that no more files will be sent
		// out.println("END OF TRANSACTION");
	}

	private List<String> syncFiles(JSONObject json) throws JSONException,
			UnknownHostException, IOException {
		JSONArray jsonArray = json.getJSONArray("files");

		Type type = new TypeToken<List<FileRep>>() {
		}.getType();
		List<FileRep> files = GSONConverter.convertJSONToObjectList(
				jsonArray.toString(), type);

		List<String> filenamesFromClient = new ArrayList<>();
		List<String> filesToSendBack = new ArrayList<>();
		for (FileRep f : files) {
			filenamesFromClient.add(f.getFilename());
			String s = syncFile(f);
			if (s != null)
				filesToSendBack.add(s);
		}

		List<String> filesOfServer = CoordiFileManager
				.findFilesFromServerToGiveBackToClient(filenamesFromClient);
		filesToSendBack.addAll(filesOfServer);
		return filesToSendBack;
	}

	private String syncFile(FileRep f) throws UnknownHostException,
			IOException, JSONException {
		// lock for mutex for critical section
		CoordiFileManager.acquireLockOfFile(f.getFilename());
		// writes if client has a later copy.
		if (CoordiFileManager.clientHasALaterCopy(f.getFilename(),
				new Timestamp(f.getLastModified()))) {
			System.out.println("Receiving: " + f.getFilename() + " "
					+ new Timestamp(f.getLastModified()));
			CoordiFileManager.writeToFile(f.getFilename(), f.getContent(),
					new Timestamp(f.getLastModified()));
			System.out.println("Finished writing " + f.getFilename());
			CoordiFileManager.releaseLockOfFile(f.getFilename());
			return null;
		} else if (CoordiFileManager.equalTimeModified(f.getFilename(),
				new Timestamp(f.getLastModified()))) {
			CoordiFileManager.releaseLockOfFile(f.getFilename());
			return null;
		} else {
			CoordiFileManager.releaseLockOfFile(f.getFilename());
			return f.getFilename();
		}
	}
}
