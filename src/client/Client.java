package client;

import indie.FileManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Client {
	private Socket socket;

	// Sending Message
	private BufferedReader in;
	private PrintWriter out;
	public static String folderPath;

	public Client(String folderPath) throws UnknownHostException, IOException {

		Client.folderPath = folderPath;

		String address = "localhost";
		int portNumber = 4441;
		socket = new Socket(address, portNumber);

		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		sendFiles();

		receiveFiles();

	}

	private void receiveFiles() throws IOException {
		List<String> files = new ArrayList<>();
		String line;
		StringBuilder sb = new StringBuilder();

		while ((line = in.readLine()) != null) {
			if (line.equals("END OF TRANSACTION")) {
				// exits the loop after all files from client has been sent
				break;
			}
			sb.append(line);

			if (line != null)
				sb.append("\n");

			if (line.substring(line.length() - 5, line.length())
					.equals("~!@#$")) {
				// if reader sees a end of file delimeter, it proceeds to build
				// the next file
				files.add(sb.toString());
				sb = new StringBuilder();
			}
		}

		for (int i = 0; i < files.size(); i++) {
			System.out.println(i);
			System.out.println(files.get(i));
		}
	}

	private void sendFiles() throws IOException {
		//simply sends all of the files to the server
		File folder = new File(folderPath);
		for (File f : folder.listFiles()) {
			//signal that that's the end of a file
			out.println(f.getName() + "###" + f.lastModified() + "###" + FileManager.readFile(f) + "~!@#$");
		}
		//signal the server that no more files will be sent
		out.println("END OF TRANSACTION");
	}

}
