package coordinator;

import indie.ResponseHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
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
			String line = in.readLine();
			System.out.println("Should received sync: " + line);
			if (line.equals("Sync")) {
				System.out.println("Sync");
				List<String> filenames = receiveFiles();
				sendFiles(filenames);
				socket.close();
			} else if (line.equals("Delete")) {
				System.out.println("To delete");
			}
		} catch (IOException e) {
			// Connection dropped
			e.printStackTrace();
			System.out.println("Finished Syncing");
		} finally {

		}
	}

	private void sendFiles(List<String> filenames) throws IOException {
		// simply sends all of the files to the server
		for (String f : filenames) {
			// signal that that's the end of a file
			String content = CoordiFileManager.readFile(f);
			out.println(f + "###" + System.currentTimeMillis() + "###"
					+ content + "~!@#$");
		}
		// signal the server that no more files will be sent
		out.println("END OF TRANSACTION");
	}

	private List<String> receiveFiles() throws IOException {
		List<String> filesToSendBack = new ArrayList<>();
		List<String> filenamesFromClient = new ArrayList<>();
		String line;
		StringBuilder sb = new StringBuilder();

		String filename = null;
		Timestamp dateModified = null;
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

				filenamesFromClient.add(filename);
				line = ResponseHandler.getFirstLineContent(line);

				sb.append(ResponseHandler.removeEndFileDelimiter(line));
				System.out.println(sb.toString());
				String s = syncFile(filename, dateModified, sb.toString());
				if (s != null)
					filesToSendBack.add(s);

				sb = new StringBuilder();
			} else if (ResponseHandler.isStartOfFile(line)) {
				// means that the file has more than one line
				// extracts the file name and time modified
				String[] tokens = line.split("###");
				filename = tokens[0];
				dateModified = new Timestamp(Long.valueOf(tokens[1]));

				filenamesFromClient.add(filename);

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
				String s = syncFile(filename, dateModified, sb.toString());
				if (s != null)
					filesToSendBack.add(s);

				sb = new StringBuilder();
			} else
				// middle liners in a file
				sb.append(line);

			if (line != null && !ResponseHandler.isEndOfFile(line)) {
				sb.append("\n");
			}

		}

		List<String> filesOfServer = CoordiFileManager
				.findFilesFromServerToGiveBackToClient(filenamesFromClient);
		filesToSendBack.addAll(filesOfServer);
		return filesToSendBack;
	}

	private String syncFile(String filename, Timestamp t, String content)
			throws IOException {

		// lock for mutex for critical section
		CoordiFileManager.acquireLockOfFile(filename);
		// writes if client has a later copy.
		if (CoordiFileManager.clientHasALaterCopy(filename, t)) {
			System.out.println("Receiving: " + filename + " " + t);
			CoordiFileManager.writeToFile(filename, content, t);
			System.out.println("Finished writing " + filename);
			CoordiFileManager.releaseLockOfFile(filename);
			return null;
		} else if (CoordiFileManager.equalTimeModified(filename, t)) {
			CoordiFileManager.releaseLockOfFile(filename);
			return null;
		} else {
			CoordiFileManager.releaseLockOfFile(filename);
			return filename;
		}

	}

}
