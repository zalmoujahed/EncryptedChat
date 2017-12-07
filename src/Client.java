
import java.net.*;
import java.util.Vector;
import java.io.*; 

public class Client {

	private String clientID;
	private Socket clientSocket = null;
	private PrintWriter output = null;

	public Client(Socket s, String id)
	{
		clientSocket = s;
		clientID = id;
	}

	public String getID(){
		return clientID;
	}

	//function to setup the streams
	public void setOutputStream(PrintWriter pw) 
	{
		output = pw;
		 
	}

	public void sendData(String data)
	{
		output.println(data);
		System.out.println("Server sent: " + data );
	}

}
