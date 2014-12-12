package coordinator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;

public class ServerConnectionHandlerThread extends Thread {
	private final Socket socket;

	ObjectInputStream in;
	ObjectOutputStream out;

	JSONObject json;

	ServerInfo serverInfo;
	String addressWithPortNumber;

	public ServerConnectionHandlerThread(Socket socket, ObjectInputStream in,
			JSONObject json) throws IOException, JSONException,
			ClassNotFoundException {
		this.socket = socket;
		this.in = in;
		out = new ObjectOutputStream(socket.getOutputStream());

		this.json = json;

		addressWithPortNumber = json.getString("addressWithPortNumber");
		System.out.println("Server: " + addressWithPortNumber);
		if (!ServerHandler.exists(addressWithPortNumber))
			ServerHandler.addNewServer(addressWithPortNumber);
		else {
			ServerHandler.setServerActive(addressWithPortNumber);
		}
		serverInfo = ServerHandler.get(addressWithPortNumber);
	}

	@Override
	public void run() {
		try {
			while (in.readObject() != null) {
				;
			}
		} catch (IOException | ClassNotFoundException e) {
			System.out.println(addressWithPortNumber);
			ServerHandler.setServerDied(addressWithPortNumber);
		}
	}
}
