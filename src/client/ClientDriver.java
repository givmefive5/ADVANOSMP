package client;


import java.io.IOException;
import java.net.UnknownHostException;

public class ClientDriver {
	public static void main(String[] args) throws UnknownHostException,
			IOException, ClassNotFoundException {
		new Client(args[0]);
	}
}
