package nachos.cop4610;

import java.io.IOException;

import nachos.cop4610.KVClientHandler;
import nachos.cop4610.KVServer;
import nachos.cop4610.NetworkHandler;
import nachos.cop4610.SocketServer;

public class Server {
	static KVServer key_server = null;
	static SocketServer server = null;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		System.out.println("Binding Server:");
		key_server = new KVServer(100, 10);
		server = new SocketServer("localhost", 8080);
		NetworkHandler handler = new KVClientHandler(key_server);
		server.addHandler(handler);
		server.connect();
		System.out.println("Starting Server");
		server.run();
	}

}
