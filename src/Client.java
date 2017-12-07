
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

	public String getMachineName()
	{

		return null;
	}

	public int getPortNumber()
	{

		return 0;
	}

	public void connectToServer(String machineName, int portNum) throws UnknownHostException, IOException
	{
		clientSocket = new Socket(machineName, portNum);
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

	public void closeConnections() throws IOException
	{
		output.close();
		input.close();
		clientSocket.close();
	}
}
