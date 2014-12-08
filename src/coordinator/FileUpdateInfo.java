package coordinator;

public class FileUpdateInfo {

	UpdateType updateType;
	Long lastModified;

	public FileUpdateInfo(UpdateType updateType, Long lastModified) {
		super();
		this.updateType = updateType;
		this.lastModified = lastModified;
	}

	public UpdateType getUpdateType() {
		return updateType;
	}

	public void setUpdateType(UpdateType updateType) {
		this.updateType = updateType;
	}

	public Long getLastModified() {
		return lastModified;
	}

	public void setLastModified(Long lastModified) {
		this.lastModified = lastModified;
	}
}
