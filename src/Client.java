
import java.net.*;
import java.util.Vector;
import java.io.*; 

public class Client {

	private String clientID;
	private String E;
	private String N;
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
	public PrintWriter getOutput(){
		return output;
	}

	public void sendMessage(String data)
	{
		output.println(data);
		
	}
	
	public void setKey(String e, String n){
		E = e;
		N = n;
		
	}
	public String getE(){
		return E;
	}
	public String getN(){
		return N;
	}

}
