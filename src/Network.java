
import java.net.*; 
import java.io.*; 

public class Network {
	PrintWriter output;
	BufferedReader input;
	//We have reusable code here that'll work for both the server and client
	
	//we need functions that send the data and receive the data
	
	//FUNC1 -> Send Data
	public void sendData(String data)
	{
		output.println(data);
		System.out.println("Client sent: " + data + "\n");
	}
	
	//FUNC2 -> Receive Data
	public void receiveData(String retreivedData) throws IOException
	{
		String label = null;
		
		while((label = input.readLine()) != null)
		{
			retreivedData = label;
			//System.out.println("This is the data retrieved");
		}
	}
	
}