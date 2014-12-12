package model;

import java.util.ArrayList;
import java.util.List;

public class FileRep {

	String filename;
	Long lastModified;
	String content;

	public FileRep(String filename, Long lastModified, String content) {
		super();
		this.filename = filename;
		this.lastModified = lastModified;
		this.content = content;
	}

	public FileRep(String filename) {
		this.filename = filename;
	}

	public FileRep(String filename, String content) {
		super();
		this.filename = filename;
		this.content = content;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Long getLastModified() {
		return lastModified;
	}

	public void setLastModified(Long lastModified) {
		this.lastModified = lastModified;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public static List<FileRep> convertFilenamesToFileReps(
			List<String> filenames) {
		List<FileRep> files = new ArrayList<>();
		for (String s : filenames) {
			files.add(new FileRep(s));
		}

		return files;
	}

}
