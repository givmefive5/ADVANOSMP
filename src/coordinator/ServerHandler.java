package coordinator;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

public class ServerHandler {

	private static int totalServers = 0;
	private static int activeCount = 0;
	private static List<ServerInfo> serverList = new ArrayList<>();

	public static synchronized void addNewServer(String addressWithPortNumber) {
		String[] tokens = addressWithPortNumber.split(":");
		String ipAddress = tokens[0];
		int portNumber = Integer.valueOf(tokens[1]);

		ServerInfo si = new ServerInfo(addressWithPortNumber, ipAddress,
				portNumber, true);
		serverList.add(si);

		totalServers++;
		activeCount++;
	}

	public static boolean exists(String addressWithPortNumber) {
		for (ServerInfo si : serverList) {
			if (si.getAddressWithPortNumber().equals(addressWithPortNumber))
				return true;
		}
		return false;
	}

	public static ServerInfo get(String addressWithPortNumber) {
		for (ServerInfo si : serverList) {
			if (si.getAddressWithPortNumber().equals(addressWithPortNumber))
				return si;
		}
		return null;
	}

	public static ServerInfo get(int index) {
		return serverList.get(index);
	}

	public static void setServerActive(String addressWithPortNumber)
			throws UnknownHostException, IOException, ClassNotFoundException,
			JSONException {
		ServerInfo si = get(addressWithPortNumber);
		int index = serverList.indexOf(si);
		si.setAlive(true);
		serverList.set(index, si);

		System.out.println("Set " + si.getIpAddress() + " "
				+ si.getPortNumber() + " " + si.isAlive());

		incrementActiveCount();

		CoordiServerFileManager.recoverMissingFilesOfServer(si);
		CoordiServerFileManager.checkAndDeletePendingFiles();
	}

	private synchronized static void incrementActiveCount() {
		activeCount++;
	}

	public static void setServerDied(String addressWithPortNumber) {
		ServerInfo si = get(addressWithPortNumber);
		int index = serverList.indexOf(si);
		si.setAlive(false);
		serverList.set(index, si);

		System.out.println("Set " + si.getIpAddress() + " "
				+ si.getPortNumber() + " " + si.isAlive());

		decrementActiveCount();
	}

	private synchronized static void decrementActiveCount() {
		activeCount--;
	}

	public static void addFileInfo(String addressWithPortNumber,
			FileInfo fileInfo) {
		ServerInfo si = get(addressWithPortNumber);
		int index = serverList.indexOf(si);
		si.addFileInfo(fileInfo);
		serverList.set(index, si);
	}

	public static void addFileInfo(int index, FileInfo fileInfo) {
		ServerInfo si = serverList.get(index);
		si.addFileInfo(fileInfo);
		serverList.set(index, si);
	}

	public static boolean hasTwoThirdServersOff() {
		if (activeCount * 1.0 / totalServers < 2.0 / 3)
			return true;
		else
			return false;
	}

	public static int twoThirdsOfTotalServers() {
		int twoThirds = totalServers * 2 / 3;

		if (twoThirds * 3 / 2 < totalServers)
			return twoThirds + 1;
		else
			return twoThirds;
	}

	public static int totalNumberOfServers() {
		return totalServers;
	}

	public static List<ServerInfo> getServerInfos() {
		return serverList;
	}

	public static void removeFileFromServer(int index, String filename) {
		ServerInfo si = get(index);
		si.remove(filename);
		serverList.set(index, si);
	}
}
