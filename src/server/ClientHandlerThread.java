package server;

import indie.ResponseHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.List;

public class ClientHandlerThread extends Thread {
	private final Socket socket;
	PrintWriter out;
	BufferedReader in;

	public ClientHandlerThread(Socket socket) throws IOException {
		this.socket = socket;
		out = new PrintWriter(socket.getOutputStream(), true);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	}

	@Override
	public void run() {

		try {
			List<File> files = receiveFiles();
		} catch (IOException e) {
			// Connection dropped
			e.printStackTrace();
			System.out.println("Finished Syncing");
		}
	}

	private List<File> receiveFiles() throws IOException {
		String line;
		StringBuilder sb = new StringBuilder();

		String filename;
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
				// ADD SYNCING PROCESS HERE AND DOWN THERE
				sb = new StringBuilder();
			} else if (ResponseHandler.isStartOfFile(line)) {
				// means that the file has more than one line
				// extracts the file name and time modified
				String[] tokens = line.split("###");
				System.out.println("Name: " + tokens[0]);
				System.out.println("Time: "
						+ new Timestamp(Long.valueOf(tokens[1])));
				line = ResponseHandler.getFirstLineContent(line);
				sb.append(line);
			} else if (ResponseHandler.isEndOfFile(line)) {
				// if reader sees a end of file delimeter, it proceeds to build
				// the next file
				sb.append(ResponseHandler.removeEndFileDelimiter(line));
				System.out.println(sb.toString());
				// ADD SYNCING PROCESS HERE AND UP THERE
				sb = new StringBuilder();
			} else
				// middle liners in a file
				sb.append(line);

			if (line != null && !ResponseHandler.isEndOfFile(line)) {
				sb.append("\n");
			}

		}

		return null;
	}

}
