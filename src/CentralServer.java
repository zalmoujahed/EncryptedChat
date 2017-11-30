
import java.net.*; 
import java.io.*; 

public class CentralServer extends Thread implements Network1{
	
	private static boolean serverContinue = true;
	private Socket clientSocket;
	PrintWriter output;
	BufferedReader input;
	String userdata;
	
	//We need to create a constructor to set up the server
	public CentralServer(Socket ClientSocket)
	{
		clientSocket = ClientSocket;
		start();
	}
	
	//our code will be running on seperate thread so they 
	//are put in here
	public void run()
	{
		//setup stream between server and new client
		try {
			setupStreams(this.clientSocket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
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
	//function to close the sockets when needed
}
