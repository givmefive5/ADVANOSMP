package client;

import indie.FileManager;
import indie.ResponseHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.Timestamp;

public class ClientSaveReceive {
	private Socket socket;

	// Sending Message
	private BufferedReader in;
	private PrintWriter out;
	public static String folderPath;

	public ClientSaveReceive(String folderPath) throws UnknownHostException,
			IOException {

		ClientSaveReceive.folderPath = folderPath + "/";

		String address = "localhost";
		int portNumber = 4441;
		socket = new Socket(address, portNumber);

		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		sendFiles();
		receiveFiles();

		deleteFiles();

		socket.close();

	}

	private void deleteFiles() throws IOException {
		String startLine = in.readLine(); // START OF DELETE

		String line;
		while ((line = in.readLine()) != null) {
			if (line.equals("END OF TRANSACTION")) {
				// exits the loop after all files from server has been sent
				break;
			}
			String filename = line;
			File f = new File(folderPath + filename);
			FileManager.deleteFile(f);
		}

	}

	private void receiveFiles() throws IOException {
		String line;
		StringBuilder sb = new StringBuilder();

		String filename = null;
		Timestamp dateModified;
		while ((line = in.readLine()) != null) {
			if (line.equals("END OF TRANSACTION")) {
				// exits the loop after all files from client has been sent
				break;
			}
			if (ResponseHandler.isStartOfFile(line)
					&& ResponseHandler.isEndOfFile(line)) {
				// means that the file has a one line content
				String[] tokens = line.split("###");
				filename = tokens[0];
				dateModified = new Timestamp(Long.valueOf(tokens[1]));
				System.out.println("Name: " + filename);
				System.out.println("Time: " + dateModified);
				line = ResponseHandler.getFirstLineContent(line);

				sb.append(ResponseHandler.removeEndFileDelimiter(line));
				System.out.println(sb.toString());
				File f = new File(folderPath + filename);
				FileManager.writeToFile(f, sb.toString());
				System.out.println("Finished writing " + f.getAbsolutePath());

				sb = new StringBuilder();
			} else if (ResponseHandler.isStartOfFile(line)) {
				// means that the file has more than one line
				// extracts the file name and time modified
				String[] tokens = line.split("###");
				System.out.println("Name: " + tokens[0]);
				System.out.println("Time: "
						+ new Timestamp(Long.valueOf(tokens[1])));
				filename = tokens[0];
				dateModified = new Timestamp(Long.valueOf(tokens[1]));
				line = ResponseHandler.getFirstLineContent(line);
				sb.append(line);
			} else if (ResponseHandler.isEndOfFile(line)) {
				// if reader sees a end of file delimeter, it proceeds to build
				// the next file
				sb.append(ResponseHandler.removeEndFileDelimiter(line));
				System.out.println(sb.toString());
				File f = new File(folderPath + filename);
				FileManager.writeToFile(f, sb.toString());
				System.out.println("Finished writing " + f.getAbsolutePath());
				sb = new StringBuilder();
			} else
				// middle liners in a file
				sb.append(line);

			if (line != null && !ResponseHandler.isEndOfFile(line)) {
				sb.append("\n");
			}

		}
	}

	private void sendFiles() throws IOException {
		// simply sends all of the files to the server
		out.println("Client");
		System.out.println("Client");
		out.println("Sync");
		System.out.println("Sync");
		File folder = new File(folderPath);
		for (File f : folder.listFiles()) {
			// signal that that's the end of a file
			out.println(f.getName() + "###" + f.lastModified() + "###"
					+ FileManager.readFile(f) + "~!@#$");
		}
		// signal the server that no more files will be sent
		out.println("END OF TRANSACTION");
	}
}
