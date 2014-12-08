package coordinator;

import java.util.ArrayList;
import java.util.List;

public class ServerInfo {

	private String addressWithPortNumber;
	private String ipAddress;
	private int portNumber;
	private boolean isAlive;
	private List<FileInfo> fileInfos;

	public ServerInfo(String addressWithPortNumber, String ipAddress,
			int portNumber, boolean isAlive) {
		super();
		this.addressWithPortNumber = addressWithPortNumber;
		this.ipAddress = ipAddress;
		this.portNumber = portNumber;
		this.isAlive = isAlive;

		fileInfos = new ArrayList<>();
	}

	public ServerInfo(String ipAddress, int portNumber, boolean isAlive) {
		super();
		this.ipAddress = ipAddress;
		this.portNumber = portNumber;
		this.isAlive = isAlive;

		fileInfos = new ArrayList<>();
	}

	public String getAddressWithPortNumber() {
		return addressWithPortNumber;
	}

	public void setIpAddressWithPortNumber(String addressWithPortNumber) {
		this.addressWithPortNumber = addressWithPortNumber;
	}

	public List<FileInfo> getFileInfos() {
		return fileInfos;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	public boolean isAlive() {
		return isAlive;
	}

	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

	public void addFileInfo(FileInfo fileInfo) {
		fileInfos.add(fileInfo);
	}

	private FileInfo get(String filename) {
		for (FileInfo fi : fileInfos) {
			if (fi.getFilename().equals(filename))
				return fi;
		}
		return null;
	}

	public boolean hasFile(String filename) {
		if (get(filename) != null)
			return true;
		else
			return false;
	}

	public boolean isReady(String filename) {
		FileInfo fi = get(filename);
		if (fi.isHasAdded())
			return true;
		else
			return false;
	}

	public void remove(String filename) {
		FileInfo fi = get(filename);
		fileInfos.remove(fi);
	}
}
