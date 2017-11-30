import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public interface Network1 {
	//PrintWriter output;
	//BufferedReader input;
	//We have reusable code here that'll work for both the server and client
	
	//we need functions that send the data and receive the data
	
	//FUNC1 -> Send Data
	public default void sendData(String data, PrintWriter output)
	{
		output.println(data);
		System.out.println("Client sent: " + data + "\n");
	}
	
	//FUNC2 -> Receive Data
	public static void receiveData(String retreivedData, BufferedReader input) throws IOException
	{
		String label = null;
		
		while((label = input.readLine()) != null)
		{
			retreivedData = label;
			//System.out.println("This is the data retrieved");
		}
	}
}
