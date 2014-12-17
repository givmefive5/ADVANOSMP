package server;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class MaintainerThread extends Thread {

	private ObjectOutputStream out;

	public MaintainerThread(ObjectOutputStream out) {
		this.out = out;
	}

	@Override
	public void run() {
		while (true) {
			try {
				out.writeObject("Maintain");
				out.flush();
				Thread.sleep(3000);
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
