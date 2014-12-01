package coordinator;

public class FileInfo {

	String filename;
	boolean hasAdded;

	public FileInfo(String filename, boolean hasAdded) {
		super();
		this.filename = filename;
		this.hasAdded = hasAdded;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public boolean isHasAdded() {
		return hasAdded;
	}

	public void setHasAdded(boolean hasAdded) {
		this.hasAdded = hasAdded;
	}

}
