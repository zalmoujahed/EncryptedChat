import java.net.*; 
import java.io.*; 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

public class ServerGUI extends JFrame {

	// ServerGUI items
	JButton ssButton;
	JLabel machineInfo;
	JLabel portInfo;
	JTextArea history;
	JTextArea clientList;
	private boolean running;
	int curID = 10000; 

	// Network Items
	boolean serverContinue;
	ServerSocket serverSocket;
	Vector <PrintWriter> outStreamList;

	Vector<Client> clients;
	Vector<String> clientIDs;


	// set up ServerGUI
	public ServerGUI()
	{
		super( "Server" );

		initializeGUI();

		setSize( 500, 500 );
		setVisible( true );

	}
	//__________________________________________________________________________________//
	void initializeGUI(){
		// set up the shared outStreamList
		outStreamList = new Vector<PrintWriter>();
		clients = new Vector<Client>();
		clientIDs = new Vector<String>();

		// get content pane and set its layout
		Container container = getContentPane();
		container.setLayout( new FlowLayout() );

		// create buttons
		running = false;
		ssButton = new JButton( "Start Listening" );
		ssButton.addActionListener( e -> doButton (e) );
		container.add( ssButton );

		String machineAddress = null;
		try
		{  
			InetAddress addr = InetAddress.getLocalHost();
			machineAddress = addr.getHostAddress();
		}
		catch (UnknownHostException e)
		{
			machineAddress = "127.0.0.1";
		}
		machineInfo = new JLabel (machineAddress);
		container.add( machineInfo );
		portInfo = new JLabel (" Not Listening ");
		container.add( portInfo );

		// initialize history pane
		history = new JTextArea ( 20, 30 );
		history.setEditable(false);
		container.add( new JScrollPane(history) );

		//initialize client list pane
		clientList = new JTextArea(20, 10);
		clientList.setEditable(false);
		container.add(new JScrollPane(clientList));


	}
	//__________________________________________________________________________________//
	public void updateClientList(){	
		clientList.insert("Client " + curID + "\n", 0);			
	}
	//__________________________________________________________________________________//
	void processInput(String input){
		
		if(input.charAt(0) == 'm'){
			relayMessage(input);
		}
		else if(input.charAt(0) == 'd'){
			disconnectClient(input);
		}
		
	}
	//__________________________________________________________________________________//
	void relayMessage(String s){
		
		//get ids
		//send message to appropriate client
		
	}	
	//__________________________________________________________________________________//
	void disconnectClient(String s){
		
		//send out disconnection message
		//update list
		
	}
	//__________________________________________________________________________________//
	
	//__________________________________________________________________________________//
	// handle button event
	public void doButton( ActionEvent event )
	{
		if (running == false)
		{
			new ConnectionThread ();
			running = true;
		}
		else
		{
			serverContinue = false;
			ssButton.setText ("Start Listening");
			portInfo.setText (" Not Listening ");
		}
	}
	//__________________________________________________________________________________//
	class ConnectionThread extends Thread
	{

		public ConnectionThread ()
		{
			start();
		}

		public void run()
		{
			serverContinue = true;

			try 
			{ 
				serverSocket = new ServerSocket(0); 
				portInfo.setText("Listening on Port: " + serverSocket.getLocalPort());
				System.out.println ("Connection Socket Created");
				try { 
					while (serverContinue)
					{
						ssButton.setText("Stop Listening");
						new CommunicationThread (serverSocket.accept()); 
						
					}
				} 
				catch (IOException e) 
				{ 
					System.err.println("Accept failed."); 
					System.exit(1); 
				} 
			} 
			catch (IOException e) 
			{ 
				System.err.println("Could not listen on port: 10008."); 
				System.exit(1); 
			} 
			finally
			{
				try {
					serverSocket.close(); 
				}
				catch (IOException e)
				{ 
					System.err.println("Could not close port: 10008."); 
					System.exit(1); 
				} 
			}
		}
	}

	//__________________________________________________________________________________//
	class CommunicationThread extends Thread
	{ 
		private Socket clientSocket;
		Client c;

		public CommunicationThread (Socket clientSoc)
		{
			Random rand = new Random(); 
			int value = rand.nextInt(100); 
			Client c = new Client(clientSoc, "" + curID);
			updateClientList();

			
			curID++;
			clients.add(c);

			clientSocket = clientSoc;
			history.insert ("Comminucating with Port" + clientSocket.getLocalPort()+"\n", 0);
						
			start();
		}

		public void run()
		{
			
			try { 
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); 
				outStreamList.add(out);

				BufferedReader in = new BufferedReader( 
						new InputStreamReader( clientSocket.getInputStream())); 

				String inputLine;  

				while ((inputLine = in.readLine()) != null) 
				{ 
					history.insert (inputLine+"\n", 0);

					// Loop through the outStreamList and send to all "active" streams

					for ( PrintWriter out1: outStreamList )
					{
						System.out.println ("Sending Message");
						out1.println (inputLine);
					}

					if (inputLine.equals("Bye.")) 
						break; 

					if (inputLine.equals("End Server.")) {
						serverContinue = false; 
						break;
					}
				} 

				outStreamList.remove(out);
				out.close(); 
				in.close(); 
				clientSocket.close(); 
			} 
			catch (IOException e) 
			{ 
				System.err.println("Problem with Communication Server");
			} 
		}
	}
} 






