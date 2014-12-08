package indie;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

	public static List<String> getFileNames(File folder) {
		List<String> list = new ArrayList<>();
		for (File f : folder.listFiles()) {
			list.add(f.getName());
		}
		return list;
	}

	public static String readFile(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);

				line = br.readLine();
				if (line != null)
					sb.append("\n");
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}

	public static void writeToFile(File file, String content)
			throws IOException {
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			bw.write(content);
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void deleteFile(File file) throws IOException {
		if (file.exists()) {
			file.delete();
			System.out.println("Deleted " + file.getName());
		}
	}

}
