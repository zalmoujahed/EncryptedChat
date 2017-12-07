import java.net.*;
import java.util.Vector;
import java.io.*; 
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ClientGUI extends JFrame implements ActionListener
{  
	// GUI items
	private JButton sendButton;
	private JButton connectButton;
	private JTextField machineInfo;
	private JTextField portInfo;
	private JTextField message;
	private JTextField toID;
	private JTextArea history;
	private JTextArea clientList;
	private JPanel panel;
	private JLabel IDLabel;

	// Network Items
	private boolean connected;
	private Socket echoSocket;
	private PrintWriter out;
	private BufferedReader in;

	//Client info
	String ID = "";
	Vector<String> otherClients;


	// set up GUI
	public ClientGUI()
	{
		super( "Client" );

		otherClients = new Vector<String>();

		// get content pane and set its layout
		Container container = getContentPane();
		container.setLayout (new BorderLayout ());

		panel = new JPanel(new BorderLayout());

		clientList = new JTextArea(10, 10);
		clientList.setEditable(false);
		panel.add(new JScrollPane(clientList));
		panel.add(new JLabel("Users connected"), BorderLayout.NORTH);

		// set up the North panel
		JPanel upperPanel = new JPanel ();
		upperPanel.setLayout (new GridLayout (3,2));
		container.add (upperPanel, BorderLayout.NORTH);

		// create buttons
		connected = false;

		upperPanel.add ( new JLabel ("Server Address: ", JLabel.RIGHT) );
		machineInfo = new JTextField ("127.0.0.1");
		upperPanel.add( machineInfo );

		upperPanel.add ( new JLabel ("Server Port: ", JLabel.RIGHT) );
		portInfo = new JTextField ("");
		upperPanel.add( portInfo );

		connectButton = new JButton( "Connect to Server" );
		connectButton.addActionListener( this );
		IDLabel = new JLabel("Your client ID: ");
		
		upperPanel.add(IDLabel);
		upperPanel.add( connectButton );


		// Set up lower panel
		JPanel lowerPanel = new JPanel(new GridLayout(3, 2));
		container.add (lowerPanel, BorderLayout.SOUTH);

		lowerPanel.add ( new JLabel ("Message: ", JLabel.RIGHT) );
		message = new JTextField ("");
		message.addActionListener( this );
		lowerPanel.add( message );

		lowerPanel.add ( new JLabel ("Client ID: ", JLabel.RIGHT) );
		toID = new JTextField ("");
		toID.addActionListener( this );
		lowerPanel.add( toID );

		sendButton = new JButton( "Send Message" );
		sendButton.addActionListener( this );
		sendButton.setEnabled (false);
		lowerPanel.add( new JLabel(""));
		lowerPanel.add( sendButton );

		history = new JTextArea ( 10, 30 );
		history.setEditable(false);
		container.add( new JScrollPane(history) ,  BorderLayout.CENTER);
		container.add(panel, BorderLayout.EAST);


		setSize( 500, 500 );
		setVisible( true );

	} // end CountDown constructor

	//________________________________________________________________________//
	// handle button event
	public void actionPerformed( ActionEvent event )
	{
		if ( connected && 
				(event.getSource() == sendButton || 
				event.getSource() == message ) )
		{
			doSendMessage();
		}
		else if (event.getSource() == connectButton)
		{
			doManageConnection();
		}
	}

	//________________________________________________________________________//
	public void doSendMessage()
	{
		try
		{
			out.println(message.getText());
		}
		catch (Exception e) 
		{
			history.insert ("Error in processing message ", 0);
		}
	}

	//________________________________________________________________________//
	public void doManageConnection()
	{
		if (connected == false)
		{
			String machineName = null;
			int portNum = -1;
			try {
				machineName = machineInfo.getText();
				portNum = Integer.parseInt(portInfo.getText());
				echoSocket = new Socket(machineName, portNum );
				out = new PrintWriter(echoSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(
						echoSocket.getInputStream()));

				// start a new thread to read from the socket
				new CommunicationReadThread ();

				sendButton.setEnabled(true);
				connected = true;
				connectButton.setText("Disconnect from Server");
			} catch (NumberFormatException e) {
				history.insert ( "Server Port must be an integer\n", 0);
			} catch (UnknownHostException e) {
				history.insert("Don't know about host: " + machineName , 0);
			} catch (IOException e) {
				history.insert ("Couldn't get I/O for "
						+ "the connection to: " + machineName , 0);
			}

		}
		else	// Disconnect from server
		{
			try 
			{
				try
				{
					out.println("d " + ID);
					
				}
				catch (Exception e) 
				{
					history.insert ("Error in processing message ", 0);
				}

				otherClients.removeAllElements();
				clientList.setText(null);
				history.insert("You have been disconnected from the server\n", 0);
				out.close();
				in.close();
				echoSocket.close();
				sendButton.setEnabled(false);
				connected = false;
				connectButton.setText("Connect to Server");
			}
			catch (IOException e) 
			{
				history.insert ("Error in closing down Socket ", 0);
			}
		}


	}
	//________________________________________________________________________//
	void processInput(String input){
		
		if(input.startsWith("ic")){			//initialize client
			ID = input.substring(3);
			IDLabel.setText("Your client ID: " + ID);
		}
		else if(input.startsWith("io")){	//initialize otherClients
			initializeClientList(input);
		}
		
		else if(input.startsWith("c")){
			addNewlyConnected(input);			
		}
		else if(input.startsWith("d")){
			removeDisconnected(input);
		}
		
		updateClientList();
		
	}
	//________________________________________________________________________//
	void initializeClientList(String input){
		
		String[] otherIDs = input.split(" ");
		int i = 1;
		while(!otherIDs[i].equals(">>end<<")){
			if(!otherIDs[i].equals(ID)){
				otherClients.add(otherIDs[i]);
			}
			i++;
		}
	}
	//________________________________________________________________________//
	void addNewlyConnected(String input){
		
		String id = input.substring(2);
		history.insert("Client " + id + " connected to the server\n", 0);
		otherClients.add(id);
		
	}
	//________________________________________________________________________//
	void removeDisconnected(String input){
		
		String id = input.substring(2);
		otherClients.remove(id);
		
	}
	//________________________________________________________________________//
	//________________________________________________________________________//
	//________________________________________________________________________//
	void updateClientList(){

		clientList.setText(null);
		
		for(String s: otherClients){
			clientList.append("Client " + s + "\n");
		}
		
	}
	//________________________________________________________________________//
	class CommunicationReadThread extends Thread
	{ 

		public CommunicationReadThread ()
		{
			start();
			history.insert ("Communicating with Port\n", 0);
		}

		public void run()
		{
			System.out.println ("New Communication Thread Started");

			try {          
				String inputLine; 

				while ((inputLine = in.readLine()) != null) 
				{  

					processInput(inputLine);
					history.insert ("From Server: " + inputLine + "\n", 0);

					if (inputLine.equals("Bye.")) 
						break; 

				} 

				in.close(); 
				//clientSocket.close(); 
			} 
			catch (IOException e) 
			{ 
				System.err.println("Problem with Client Read");
			} 
		}
	} 

	//"m 45 >>begin<< he ll o  th er e >>end<<"

} 




