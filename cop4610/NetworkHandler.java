package nachos.cop4610;

import java.io.IOException;
import java.net.Socket;


public interface NetworkHandler {
	public void handle(Socket client) throws IOException;
}
