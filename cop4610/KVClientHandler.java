package nachos.cop4610;

import java.io.IOException;
import java.net.Socket;

/**
 * This NetworkHandler will asynchronously handle the socket connections. 
 * It uses a threadpool to ensure that none of it's methods are blocking.
 *
 */
public class KVClientHandler implements NetworkHandler {
	private KVServer kv_Server = null;
	private ThreadPool threadpool = null;
	
	public KVClientHandler(KVServer kvServer) {
		initialize(kvServer, 1);
	}

	public KVClientHandler(KVServer kvServer, int connections) {
		initialize(kvServer, connections);
	}

	private void initialize(KVServer kvServer, int connections) {
		this.kv_Server = kvServer;
		threadpool = new ThreadPool(connections);	
	}
	

	private class ClientHandler implements Runnable {
		private KVServer kvServer = null;
		private Socket client = null;
		
		@Override
		public void run() {
			System.out.println("Handling");
			KVMessage message, res;
			try {
				message = null;
				res = new KVMessage("resp");
			} catch (KVException e) {
				res = e.getMsg();
				try {
					res.sendMessage(client);
					return;
				} catch (KVException e1) {
					return;
				}
			}
			try {
				message = new KVMessage(client.getInputStream());
				if ("getreq".equals(message.getMsgType())) {
					res.setKey(message.getKey());
					res.setValue(kvServer.get(message.getKey()));
				} else if ("putreq".equals(message.getMsgType())) {
					kvServer.put(message.getKey(), message.getValue());
					res.setMessage("Success");
				} else if ("delreq".equals(message.getMsgType())) {
					kvServer.del(message.getKey());
					res.setMessage("Success");
				} else {
					throw new KVException(new KVMessage("resp", "XML Error: Received unparseable message"));
				}
				System.out.println(res.getKey());
				System.out.println(res.getValue());
			} catch (KVException e) {
				res = e.getMsg();
			} catch (IOException e) {
				res.setMessage("Network Error: Could not receive data");
			}
			try {
				res.sendMessage(client);
			} catch (KVException e) {
				e.printStackTrace();
			}
		}

		public ClientHandler(KVServer kvServer, Socket client) {
			this.kvServer = kvServer;
			this.client = client;
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.berkeley.cs162.NetworkHandler#handle(java.net.Socket)
	 */
	@Override
	public void handle(Socket client) throws IOException {
		Runnable r = new ClientHandler(kv_Server, client);
		try {
			threadpool.addToQueue(r);
		} catch (InterruptedException e) {
			// Ignore this error
			return;
		}
	}
}
