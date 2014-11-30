package coordinator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class CoordiServerFileManager {

	static int indexForSavingFile = 0;

	public static List<String> getAllFilenamesFromServers() {

		return null;
	}

	public static void saveFile(String filename, String content)
			throws UnknownHostException, IOException {
		int twoThirdsOfServers = ServerHandler.twoThirdsOfTotalServers();

		int count = 0;
		while (count < twoThirdsOfServers) {
			ServerInfo si = ServerHandler.get(indexForSavingFile + count);
			saveFileContent(si.getIpAddress(), si.getPortNumber(), filename,
					content);
			count++;
		}
		indexForSavingFile++;
	}

	private static void saveFileContent(String ipAddress, int portNumber,
			String filename, String content) throws UnknownHostException,
			IOException {
		Socket socket = new Socket(ipAddress, portNumber);

		PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
		BufferedReader in = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));

		out.println("SAVE FILE");
		// signal that that's the end of a file
		out.println(filename + "###" + content + "~!@#$");
		// signal the server that no more files will be sent
		out.println("END OF TRANSACTION");

		String line;
		while ((line = in.readLine()) != null) {
			if (line.equals("FINISHED"))
				break;
		}
		socket.close();
	}

	public static String loadFileContent(String filename) {
		return null;
	}
}
