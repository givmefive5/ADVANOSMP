package coordinator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerConnectionHandlerThread extends Thread {
	private final Socket socket;
	PrintWriter out;
	BufferedReader in;

	ServerInfo serverInfo;
	String addressWithPortNumber;

	public ServerConnectionHandlerThread(Socket socket, BufferedReader in,
			String addressWithPortNumber) throws IOException {
		this.addressWithPortNumber = addressWithPortNumber;

		if (!ServerHandler.exists(addressWithPortNumber))
			ServerHandler.addNewServer(addressWithPortNumber);
		else {
			ServerHandler.setServerActive(addressWithPortNumber);
		}
		serverInfo = ServerHandler.get(addressWithPortNumber);
		this.socket = socket;
		this.in = in;
		out = new PrintWriter(socket.getOutputStream(), true);
	}

	@Override
	public void run() {
		try {
			while (in.readLine() != null) {
				;
			}
		} catch (IOException e) {
			ServerHandler.setServerDied(addressWithPortNumber);
		}
	}
}
