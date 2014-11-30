package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class ServerMain {

	public static ServerSocket serverSocket;
	public static String folderPath;

	private static BufferedReader in;
	private static PrintWriter out;

	public static void main(String[] args) throws IOException {

		String myIPAddress = "localhost";
		int myPortNumber = Integer.valueOf(args[0]);
		folderPath = args[1];

		serverSocket = new ServerSocket(myPortNumber);

		notifyCoordinator(myIPAddress, myPortNumber);

		handleCoordinatorRequests();
	}

	private static void notifyCoordinator(String myIPAddress, int myPortNumber)
			throws UnknownHostException, IOException {
		Socket socket = new Socket("localhost", 4441);
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		out.println(myIPAddress + ":" + myPortNumber);
		// connector between server and coordi only.

	}

	private static void handleCoordinatorRequests() throws IOException {
		while (true) {

			Socket socket = serverSocket.accept();

		}
	}
}
