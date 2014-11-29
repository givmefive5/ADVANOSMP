package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {

	public static void main(String[] args) throws IOException {

		int portNumber = 4441;
		ServerSocket serverSocket = new ServerSocket(portNumber);
		System.out.println("Running on port " + portNumber);
		while (true) {
			ServerFileManager.initializeFileLocks();

			Socket socket = serverSocket.accept();
			new ClientHandlerThread(socket).start();
		}

	}
}
