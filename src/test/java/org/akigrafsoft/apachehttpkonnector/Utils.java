package org.akigrafsoft.apachehttpkonnector;

import java.io.IOException;
import java.net.ServerSocket;

public class Utils {
	public static void sleep(int seconds) {
		System.out.println("Sleeping " + seconds + "s");
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static int findFreePort() {
		ServerSocket server;
		try {
			server = new ServerSocket(0);
			int port = server.getLocalPort();
			server.close();
			return port;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}
}
