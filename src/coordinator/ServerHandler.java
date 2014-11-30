package coordinator;

import java.util.ArrayList;
import java.util.List;

public class ServerHandler {

	private static int totalServers = 0;
	private static int activeCount = 0;
	private static List<ServerInfo> serverList = new ArrayList<>();

	public static void addNewServer(String addressWithPortNumber) {
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

	public static void setServerActive(String addressWithPortNumber) {
		ServerInfo si = get(addressWithPortNumber);
		int index = serverList.indexOf(si);
		si.setAlive(true);
		serverList.set(index, si);

		System.out.println("Set " + si.getIpAddress() + " "
				+ si.getPortNumber() + " " + si.isAlive());

		activeCount++;
	}

	public static void setServerDied(String addressWithPortNumber) {
		ServerInfo si = get(addressWithPortNumber);
		int index = serverList.indexOf(si);
		si.setAlive(false);
		serverList.set(index, si);

		System.out.println("Set " + si.getIpAddress() + " "
				+ si.getPortNumber() + " " + si.isAlive());

		activeCount--;
	}

	public static void addFile(String addressWithPortNumber, String filename) {
		ServerInfo si = get(addressWithPortNumber);
		int index = serverList.indexOf(si);
		si.addFilename(filename);
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
}
