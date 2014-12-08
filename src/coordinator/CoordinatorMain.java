package coordinator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class CoordinatorMain {

	public static void main(String[] args) throws IOException {

		int portNumber = 4441;
		ServerSocket serverSocket = new ServerSocket(portNumber);
		System.out.println("Running on port " + portNumber);
		while (true) {
			CoordiFileManager.initializeFileLocks();

			Socket socket = serverSocket.accept();

			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			String firstLine = in.readLine();
			if (firstLine != null && firstLine.equals("Client")) {
				System.out.println("Client");
				new ClientHandlerThread(socket, in).start();
			} else if (firstLine != null) {
				System.out.println("Server: " + firstLine);
				String addressWithPortNumber = firstLine;
				// server threads would send out IDs along with the word Server
				new ServerConnectionHandlerThread(socket, in,
						addressWithPortNumber).start();
			}
		}

	}
}
