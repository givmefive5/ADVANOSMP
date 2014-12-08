package server;

import indie.FileManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerDeleteFileThread extends Thread {
	private final Socket socket;
	PrintWriter out;
	BufferedReader in;

	public ServerDeleteFileThread(Socket socket, BufferedReader in)
			throws IOException {
		this.socket = socket;
		out = new PrintWriter(socket.getOutputStream(), true);
		this.in = in;
	}

	@Override
	public void run() {

		try {

			deleteFile();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void deleteFile() throws IOException {
		String filename = in.readLine();
		socket.close();
		File f = new File(ServerMain.folderPath + filename);
		FileManager.deleteFile(f);
		System.out.println("Deleted " + f.getName());
	}
}
