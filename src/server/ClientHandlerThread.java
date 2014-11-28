package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ClientHandlerThread extends Thread {
	private Socket socket;
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
		List<String> files = new ArrayList<>();
		String line;
		StringBuilder sb = new StringBuilder();

		String filename;
		Timestamp dateModified;
		while ((line = in.readLine()) != null) {
			if (line.equals("END OF TRANSACTION")) {
				// exits the loop after all files from client has been sent
				break;
			}
			
			
			if(isStartOfFile(line) && isEndOfFile(line)){
				//means that the file has a one line content
				String[] tokens = line.split("###");
				System.out.println("Name: " + tokens[0]);
				System.out.println("Time: " + new Timestamp(Long.valueOf(tokens[1])));
				line = getFirstLineContent(line);
				
				sb.append(removeEndFileDelimiter(line));
				files.add(sb.toString());
				sb = new StringBuilder();
			}
			else if(isStartOfFile(line)){
				//means that the file has more than one line
				//extracts the file name and time modified
				String[] tokens = line.split("###");
				System.out.println("Name: " + tokens[0]);
				System.out.println("Time: " + new Timestamp(Long.valueOf(tokens[1])));
				line = getFirstLineContent(line);
				sb.append(line);
			}
			else if (isEndOfFile(line)) {
				// if reader sees a end of file delimeter, it proceeds to build
				// the next file
				sb.append(removeEndFileDelimiter(line));
				files.add(sb.toString());
				sb = new StringBuilder();
			}
			else //middle liners in a file
				sb.append(line);

			if (line != null && !isEndOfFile(line)){
				sb.append("\n");
			}
			
		}

		for (int i = 0; i < files.size(); i++) {
			System.out.println(i);
			System.out.println(files.get(i));
		}
		return null;
	}
	
	private boolean isEndOfFile(String  s){
		
		if (s.substring(s.length() - 5, s.length())
				.equals("~!@#$"))
			return true;
		return false;
	}
	
	private String removeEndFileDelimiter(String s){
		if(s.length() == 5)
			return "";
		
		return s.substring(0, s.length() -5);
	}
	
	private boolean isStartOfFile(String s){
		if(s.split("###").length >= 2)
			return true;
		return false;
	}
	
	private String getFirstLineContent(String s){
		String[] tokens = s.split("###");
		if(tokens.length == 2)
			return "";
		else
			return tokens[2];
	}
}
