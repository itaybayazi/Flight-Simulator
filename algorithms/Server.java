package algorithms;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Server {

	public interface ClientHandler{
		// define...
		public void StartCLI(AnomalyDetectionHandler.SocketIO Client);
		public void Communicate(Socket C);
	}

	volatile boolean stop;
	public Server() {
		stop=false;
	}
	
	
	private void startServer(int port, ClientHandler ch){
		// implement here the server...
		Integer ClientLimit = 1 , Counter = 0;
		try {
			ServerSocket Server = new ServerSocket(port);
			Server.setSoTimeout(1000);
			while (!stop) {
				try {
					Socket Client = Server.accept();
					Counter++;
					if (Counter == ClientLimit) {
						ch.Communicate(Client);
					}
					Counter--;
					Client.close();
				} catch (SocketTimeoutException e) {
				}
			}
			Server.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	// runs the server in its own thread
	public void start(int port, ClientHandler ch) {
		new Thread(()->startServer(port,ch)).start();
	}
	
	public void stop() {
		stop=true;
	}
}
