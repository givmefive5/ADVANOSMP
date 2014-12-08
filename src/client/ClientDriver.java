package client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ClientDriver {

	private static List<String> fileList = new ArrayList<>();

	public static void main(String[] args) throws UnknownHostException,
			IOException, ClassNotFoundException, InterruptedException {

		while (true) {
			new ClientDelete(args[0]);
			new ClientSaveReceive(args[0]);
			Thread.sleep(6000);
		}
	}

	public static void replaceList(List<String> newList) {
		// System.out.println("Files in folder" + newList + " old files "
		// + fileList);
		fileList = newList;
	}

	public static String[] getDeletedFiles(List<String> newList) {
		List<String> deletedFiles = new ArrayList<>();

		for (String old : fileList) {
			if (!newList.contains(old))
				deletedFiles.add(old);
		}

		return deletedFiles.toArray(new String[deletedFiles.size()]);
	}
}
