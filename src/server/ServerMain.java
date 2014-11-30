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

	public static void main(String[] args) throws IOException {

		String myIPAddress = "localhost";
		int myPortNumber = Integer.valueOf(args[0]);
		folderPath = args[1] + "/";

		serverSocket = new ServerSocket(myPortNumber);

		notifyCoordinator(myIPAddress, myPortNumber);

		handleCoordinatorRequests();
	}

	private static void notifyCoordinator(String myIPAddress, int myPortNumber)
			throws UnknownHostException, IOException {
		Socket socket = new Socket("localhost", 4441);
		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));

		out.println(myIPAddress + ":" + myPortNumber);
		// connector between server and coordi only.

	}

	private static void handleCoordinatorRequests() throws IOException {

		while (true) {
			Socket socket = serverSocket.accept();

			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			String firstLine = in.readLine();
			if (firstLine.equals("SAVE FILE")) {
				System.out.println("SAVE FILE");
				// String line;
				// while ((line = in.readLine()) != null)
				// System.out.println(line);
				new ServerSaveFileThread(socket, in).start();
			} else if (firstLine.equals("LOAD FILE")) {
				// server threads would send out IDs along with the word Server
				new ServerLoadFileThread(socket).start();
			}
		}
	}

}
