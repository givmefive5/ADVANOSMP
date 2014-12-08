package client;

import indie.FileManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class ClientDelete {
	private Socket socket;

	// Sending Message
	private BufferedReader in;
	private PrintWriter out;
	public static String folderPath;

	public ClientDelete(String folderPath) throws UnknownHostException,
			IOException {

		ClientDelete.folderPath = folderPath + "/";

		String address = "localhost";
		int portNumber = 4441;
		socket = new Socket(address, portNumber);

		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		checkForDeletedFiles();
		socket.close();

	}

	private void checkForDeletedFiles() {
		File folder = new File(folderPath);
		List<String> newFiles = FileManager.getFileNames(folder);
		String[] filesToDelete = ClientDriver.getDeletedFiles(newFiles);

		if (filesToDelete.length > 0) {
			out.println("Client");
			System.out.println("Client");
			out.println("Delete");
			System.out.println("Delete");
			for (String f : filesToDelete) {
				out.println(f);
				System.out.println("Asked server to delete " + f);
			}
			out.println("END OF TRANSACTION");
		}
		ClientDriver.replaceList(newFiles);
	}
}
