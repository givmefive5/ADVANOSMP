package coordinator;

import indie.GSONConverter;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class CoordinatorMain {

	public static void main(String[] args) throws IOException,
			ClassNotFoundException, JSONException {

		int portNumber = 4441;
		ServerSocket serverSocket = new ServerSocket(portNumber);
		System.out.println("Running on port " + portNumber);
		CoordiFileManager.initializeFileLocks();
		while (true) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();

				ObjectInputStream in = new ObjectInputStream(
						socket.getInputStream());

				String jsonString = (String) in.readObject();

				JSONObject json = GSONConverter
						.convertJSONStringToObject(jsonString);

				String classType = json.getString("classType");

				if (classType.equals("Client")) {
					System.out.println("Client");
					new ClientHandlerThread(socket, in, json).start();
				} else if (classType.equals("Server")) {
					new ServerConnectionHandlerThread(socket, in, json)
							.start();
				}
				// BufferedReader in = new BufferedReader(new InputStreamReader(
				// socket.getInputStream()));
				// String firstLine = in.readLine();
				// if (firstLine != null && firstLine.equals("Client")) {
				// System.out.println("Client");
				// new ClientHandlerThread(socket, in).start();
				// } else if (firstLine != null) {
				// System.out.println("Server: " + firstLine);
				// String addressWithPortNumber = firstLine;
				// // server threads would send out IDs along with the word
				// Server
				// new ServerConnectionHandlerThread(socket, in,
				// addressWithPortNumber).start();
				// }
			} catch (EOFException e) {
			}
		}
	}
}
