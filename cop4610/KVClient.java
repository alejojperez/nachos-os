package nachos.cop4610;

import java.io.IOException;
import java.net.Socket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * This class is used to communicate with (appropriately marshalling and unmarshalling) 
 * objects implementing the {@link KeyValueInterface}.
 *
 * @param <K> Java Generic type for the Key
 * @param <V> Java Generic type for the Value
 */
public class KVClient implements KeyValueInterface {

	private String server = null;
	private int port = 0;	
	/**
	 * @param server is the DNS reference to the Key-Value server
	 * @param port is the port on which the Key-Value server is listening
	 */
	public KVClient(String server, int port) {
		this.server = server;
		this.port = port;
	}
	
	private Socket connectHost() throws KVException {
		
		Socket socket = null;
		
//		// Create socket binding to local IP address on port 8080
//		try {
//			socket = new Socket();
//			socket.bind(new InetSocketAddress(InetAddress.getLocalHost().getHostName(), 8080));
//		} catch (IOException e) {
//			throw new KVException(new KVMessage("resp", "Network Error: Could not create socket"));
//		}

		// Connect client to host server on specified address and port
		try {
			socket = new Socket(this.server, this.port);
			socket.setSoTimeout(20000);
		} catch (IOException e) {
			throw new KVException(new KVMessage("resp", "Network Error: Could not create socket"));
		}
		
		return socket;
	}
	
	private void closeHost(Socket sock) throws KVException {
		
		// Close socket and catch IOException and wrap them in a KVException
	    try {
			sock.close();
	    } catch (IOException e) {
	    	throw new KVException(new KVMessage("resp", "Unknown Error: " + e.getMessage()));
	    }
	}
	
	/**
	 * Propogates up KVException from any of the following methods
	 * 	connectHost()
	 * 	KVMessage(String msgType)
	 * 	KVMessage(InputStream input)
	 * 	KVMessage.sendMessage(Socket sock)
	 * 	closeHost(Socket sock)
	 * OR
	 * Throws a KVException from a error response KVMessage of type "resp"
	 * OR
	 * Throws a KVException of Unknown Error type if KVMessage response is not of type "resp"
	 * 
	 * Catches IOExceptions or SocketExceptions and wraps them in KVExceptions of type "resp"
	 * with respective Unknown Error error messages
	 * 
	 * @param key
	 * @param value
	 * @throws KVException
	 */
	public void put(String key, String value) throws KVException {
		
		Socket socket = null;
		KVMessage message = null;
		KVMessage response = null;
		
		try {
			// Connect to server
			socket = connectHost();

			// Initialize a KVMessage of type putreq
			message = new KVMessage("putreq");

			// Set key and value then send message
			message.setKey(key);
			message.setValue(value);
			message.sendMessage(socket);
		
			// Get response from server
			response = new KVMessage(socket.getInputStream());
			closeHost(socket);
		} catch (IOException e) {
			throw new KVException(new KVMessage("resp", "Unknown Error: " + e.getMessage()));
		}
		
		// If we don't recieve a KVMessage of type "resp" throw unknown error
		if (! response.getMsgType().equals("resp")) {
			throw new KVException(new KVMessage("resp", "Uknown Error: Recieved a message not of type 'resp' from server"));
		}
		
		// If KVMessage response isn't successful, throw KVException with response
		if (! response.getMessage().equals("Success")) {
			throw new KVException(response);
		}
		
		return;
	}

	/**
	 * Propogates up KVException from any of the following methods
	 * 	connectHost()
	 * 	KVMessage(String msgType)
	 * 	KVMessage(InputStream input)
	 * 	KVMessage.sendMessage(Socket sock)
	 * 	closeHost(Socket sock)
	 * OR
	 * Throws a KVException with KVMessage response if response's value is not a String
	 * OR
	 * Throws a KVException of Unknown Error type if KVMessage response is not of type "resp"
	 * 
	 * Catches IOExceptions or SocketExceptions and wraps them in KVExceptions of type "resp"
	 * with respective Unknown Error error messages
	 * 
	 * @param key
	 * @throws KVException
	 * @return String value
	 */
	public String get(String key) throws KVException {
		
		Socket socket = null;
		KVMessage message = null;
		KVMessage response = null;
		
		try {
			// Connect to server
			socket = connectHost();
			
			// Initialize KVMessage of type getreq
			message = new KVMessage("getreq");
			
			// Set key and send message to server via socket
			message.setKey(key);
			message.sendMessage(socket);
			
			// Get the response from the server
			response = new KVMessage(socket.getInputStream());
			closeHost(socket);
		} catch (IOException e) {
			throw new KVException(new KVMessage("resp", "Unknown Error: " + e.getMessage()));
		}
		
		// If we don't recieve a KVMessage of type "resp" throw unknown error
		if (! response.getMsgType().equals("resp")) {
			throw new KVException(new KVMessage("resp", "Uknown Error: Recieved a message not of type 'resp' from server"));
		}
		
		// If KVMessage response doesn't have a value of type String, throw KVException with response
		if ( response.getValue() == null) {
			throw new KVException(response);
		} 
		
		return response.getValue();
	}
	
	/**
	 * Propogates up KVException from any of the following methods
	 * 	connectHost()
	 * 	KVMessage(String msgType)
	 * 	KVMessage(InputStream input)
	 * 	KVMessage.sendMessage(Socket sock)
	 * 	closeHost(Socket sock)
	 * OR
	 * Throws a KVException with KVMessage response if response doesn't have a successful message
	 * OR
	 * Throws a KVException of Unknown Error type if KVMessage response is not of type "resp"
	 * 
	 * Catches IOExceptions or SocketExceptions and wraps them in KVExceptions of type "resp"
	 * with respective Unknown Error error messages
	 * 
	 * @param key
	 * @throws KVException
	 */
	public void del(String key) throws KVException {
		
		Socket socket = null;
		KVMessage message = null;
		KVMessage response = null;
		
		try {
			// Connect to server
			socket = connectHost();

			// Initialize a KVMessage of type delreq
			message = new KVMessage("delreq");

			// Set key and value then send message
			message.setKey(key);
			message.sendMessage(socket);

			// Get response from server
			response = new KVMessage(socket.getInputStream());
			closeHost(socket);
		} catch (IOException e) {
			throw new KVException(new KVMessage("resp", "Unknown Error: " + e.getMessage()));
		}

		// If we don't recieve a KVMessage of type "resp" throw unknown error
		if (! response.getMsgType().equals("resp")) {
			throw new KVException(new KVMessage("resp", "Uknown Error: Recieved a message not of type 'resp' from server"));
		}
		
		// If KVMessage response isn't successful, throw KVException with response
		if (! response.getMessage().equals("Success")) {
			throw new KVException(response);
		} 
		
		return;
		
	}	
}
