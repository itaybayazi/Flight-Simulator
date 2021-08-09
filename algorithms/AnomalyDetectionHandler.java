package algorithms;


import algorithms.Commands.DefaultIO;
import algorithms.Server.ClientHandler;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class AnomalyDetectionHandler implements ClientHandler{
	public class SocketIO implements DefaultIO{
		Scanner in;
		PrintWriter out;

		public SocketIO(InputStream InFromClient , OutputStream OutToClient)
		{
				in = new Scanner(InFromClient);
				out = new PrintWriter(OutToClient);
		}
		public SocketIO() {}
		@Override
		public String readText() {
			return in.nextLine();
		}

		@Override
		public void write(String text) {
			out.print(text);
			out.flush();
		}

		@Override
		public float readVal() {
			return in.nextFloat();
		}

		@Override
		public void write(float val) {
			out.print(val);
			out.flush();
		}

		public void close() {
			in.close();
			out.close();
		}
	}

	public void StartCLI(SocketIO Client)
	{
		CLI cli = new CLI(Client);
		cli.start();
	}

	public void Communicate(Socket C)  {
		try {
			SocketIO Client = new SocketIO(C.getInputStream(), C.getOutputStream());
			StartCLI(Client);
			Client.write("bye.");
			Client.close();
		}
		catch(IOException ignored) {}
	}

}
