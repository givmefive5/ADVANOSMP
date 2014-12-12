package server;

import indie.GSONConverter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

public class ServerMain {

	public static ServerSocket serverSocket;
	public static String folderPath;

	public static void main(String[] args) throws IOException, JSONException,
			ClassNotFoundException {

		String myIPAddress = "localhost";
		int myPortNumber = Integer.valueOf(args[0]);
		folderPath = args[1] + "/";

		serverSocket = new ServerSocket(myPortNumber);

		notifyCoordinator(myIPAddress, myPortNumber);

		handleCoordinatorRequests();
	}

	private static void notifyCoordinator(String myIPAddress, int myPortNumber)
			throws UnknownHostException, IOException, JSONException {
		Socket socket = new Socket("localhost", 4441);
		ObjectOutputStream out = new ObjectOutputStream(
				socket.getOutputStream());

		JSONObject json = new JSONObject();
		json.put("classType", "Server");
		json.put("addressWithPortNumber", myIPAddress + ":" + myPortNumber);
		out.writeObject(json.toString());
		out.flush();
		// out.println(myIPAddress + ":" + myPortNumber);
		// connector between server and coordi only.

	}

	private static void handleCoordinatorRequests() throws IOException,
			JSONException, ClassNotFoundException {

		while (true) {
			Socket socket = serverSocket.accept();

			ObjectInputStream in = new ObjectInputStream(
					socket.getInputStream());

			String jsonString = (String) in.readObject();

			JSONObject json = GSONConverter
					.convertJSONStringToObject(jsonString);

			String actionType = json.getString("actionType");

			if (actionType.equals("Save")) {
				new ServerSaveFileThread(socket, in, json).start();
			} else if (actionType.equals("Load")) {
				new ServerLoadFileThread(socket, in, json).start();
			} else if (actionType.equals("Delete")) {
				new ServerDeleteFileThread(socket, in, json).start();
			}

			// BufferedReader in = new BufferedReader(new InputStreamReader(
			// socket.getInputStream()));
			// String firstLine = in.readLine();
			// if (firstLine.equals("SAVE FILE")) {
			// new ServerSaveFileThread(socket, in).start();
			// } else if (firstLine.equals("LOAD FILE")) {
			// new ServerLoadFileThread(socket, in).start();
			// } else if (firstLine.equals("DELETE FILE"))
			// new ServerDeleteFileThread(socket, in).start();
		}
	}
}
