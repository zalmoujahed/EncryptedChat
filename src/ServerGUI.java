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
		
		clientList.setText(null);
		
		for(Client c: clients){
			clientList.insert("Client " + c.getID() + "\n", 0);
		}
					
	}
	//__________________________________________________________________________________//
	void processInput(String input){
		
		if(input.charAt(0) == 'm'){
			relayMessage(input);
		}
		else if(input.charAt(0) == 'd'){
			disconnectClient(input);
		}
		
		updateClientList();
		
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
		String id = s.substring(2);
		history.insert(s, 0);
		
		for(Client c: clients){
			if(c.getID().equals(id)){
				clients.remove(c);
				break;
			}
		}
		broadcast('d', id);
		
	}
	//__________________________________________________________________________________//
	void broadcast(char type, String id){
		
		for(Client c: clients){
			if(!c.getID().equals(id))
				c.sendMessage(type + " "+ id);
		}
		
	}
	//__________________________________________________________________________________//
	void initializeClient(PrintWriter output){
		
		//initialize client info
		output.println("ic "+ curID);
		
		//initialize other clients' info
		String others = "";
		
		for(Client c: clients){
			if(!c.getID().equals(curID)){
				others += " " + c.getID();
			}
		}
		
		output.println("io" + others + " >>end<<");
		
		broadcast('c', "" + curID);
	}
	
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
			curID++;

			c = new Client(clientSoc, "" + curID);
			clients.add(c);
			
			updateClientList();
			
			clientSocket = clientSoc;
			history.insert ("Comminucating with Port" + clientSocket.getLocalPort()+"\n", 0);
						
			start();
		}

		public void run()
		{
			
			try { 
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); 				
				BufferedReader in = new BufferedReader( new InputStreamReader( clientSocket.getInputStream())); 
				
				c.setOutputStream(out);
				outStreamList.add(out);
				
				// Send an initializing message to the newly connected client
				initializeClient(out);
				
				String inputLine;  

				while ((inputLine = in.readLine()) != null) 
				{ 
					processInput(inputLine);
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






