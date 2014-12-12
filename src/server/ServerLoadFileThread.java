package server;

import indie.FileManager;
import indie.GSONConverter;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import model.FileRep;

import org.json.JSONException;
import org.json.JSONObject;

public class ServerLoadFileThread extends Thread {
	private final Socket socket;

	ObjectInputStream in;
	ObjectOutputStream out;

	JSONObject json;

	public ServerLoadFileThread(Socket socket, ObjectInputStream in,
			JSONObject json) throws IOException {
		this.socket = socket;
		this.in = in;
		out = new ObjectOutputStream(socket.getOutputStream());

		this.json = json;
	}

	@Override
	public void run() {

		try {

			loadAndSendFile();
			socket.close();
		} catch (IOException | JSONException e) {
			e.printStackTrace();
		}
	}

	private void loadAndSendFile() throws IOException, JSONException {

		JSONObject jsonObject = json.getJSONObject("file");

		FileRep fileRep = GSONConverter.getGSONObjectGivenJsonObject(
				jsonObject, FileRep.class);

		String filename = fileRep.getFilename();
		System.out.println(filename);
		File file = new File(ServerMain.folderPath + filename);

		JSONObject jsonToSend = new JSONObject();
		FileRep f = new FileRep(filename, FileManager.readFile(file));
		jsonToSend.put("file", GSONConverter.convertObjectToJSON(f));

		out.writeObject(jsonToSend.toString());
		out.flush();
	}
}
