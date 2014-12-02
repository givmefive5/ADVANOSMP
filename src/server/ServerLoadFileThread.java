package server;

import indie.FileManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerLoadFileThread extends Thread {
	private final Socket socket;
	PrintWriter out;
	BufferedReader in;

	public ServerLoadFileThread(Socket socket, BufferedReader in)
			throws IOException {
		this.socket = socket;
		out = new PrintWriter(socket.getOutputStream(), true);
		this.in = in;
	}

	@Override
	public void run() {

		try {

			loadAndSendFile();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadAndSendFile() throws IOException {
		String filename = in.readLine();
		System.out.println(filename);
		File file = new File(ServerMain.folderPath + filename);
		out.println(FileManager.readFile(file));
		out.println("END OF TRANSACTION");
	}
}
