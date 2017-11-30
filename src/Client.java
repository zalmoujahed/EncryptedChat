
import java.net.*; 
import java.io.*; 

public class Client {

	String receivedLabel;
	Socket clientSocket = null;
	PrintWriter output = null;
	BufferedReader input = null;
	boolean isServer = false;
	
	public Client(int portServer)
	{
		
	}
	
	//function to setup the streams
	public void setupStreams(Socket clientSocket) throws IOException
	{
		 output = new PrintWriter(clientSocket.getOutputStream(), true); 
		 input = new BufferedReader(new InputStreamReader( clientSocket.getInputStream())); 
	}
	
	public void receiveData(String retreivedData) throws IOException
	{
		String label = null;
		
		while((label = input.readLine()) != null)
		{
			retreivedData = label;
			//System.out.println("This is the data retrieved");
		}
	}
	
	public void sendData(String data)
	{
		output.println(data);
		System.out.println("Client sent: " + data + "\n");
	}
}
