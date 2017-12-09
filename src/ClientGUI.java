import java.net.*;
import java.util.ArrayList;
import java.util.*;
import java.math.*;
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
	private JTextField pText;
	private JTextField qText;
	
	
	// Network Items
	private boolean connected;
	private Socket echoSocket;
	private PrintWriter out;
	private BufferedReader in;

	//Client info
	private String ID = "";
	private BigInteger privateKey; 
	private Vector<OtherClient> otherClients;
	
	private ArrayList<Integer> prime = new ArrayList<Integer>();
	private int BLOCKING_SIZE = 4;
	private int P = 13;
	private int Q = 19;
	private BigInteger PHI;
	private BigInteger N;
	private BigInteger E;
	private BigInteger D;
	private BigInteger M;
	private BigInteger C;
	
	


	// set up GUI
	public ClientGUI()
	{	
		super( "Client" );

		try {
			setUpPrimeList();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
		JPanel lowerPanel = new JPanel(new GridLayout(4, 2));
		container.add (lowerPanel, BorderLayout.SOUTH);

		lowerPanel.add ( new JLabel ("Message: ", JLabel.RIGHT) );
		message = new JTextField ("");
		message.addActionListener( this );
		lowerPanel.add( message );

		lowerPanel.add ( new JLabel ("Client ID: ", JLabel.RIGHT) );
		toID = new JTextField ("");
		toID.addActionListener( this );
		lowerPanel.add( toID );

		
		pText = new JTextField ("");
		JPanel p = new JPanel(new GridLayout(1,2));
		p.add(new JLabel("P Value:"));
		p.add(pText);
		
		qText = new JTextField ("");
		JPanel q = new JPanel(new GridLayout(1,2));
		q.add(new JLabel("Q Value:"));
		q.add(qText);
		
		lowerPanel.add(p);
		lowerPanel.add(q);
		
		sendButton = new JButton( "Send Message" );
		sendButton.addActionListener( this );
		sendButton.setEnabled (false);
		lowerPanel.add( new JLabel(""));
		lowerPanel.add( sendButton );

		history = new JTextArea ( 10, 30 );
		history.setEditable(false);
		container.add( new JScrollPane(history) ,  BorderLayout.CENTER);
		container.add(panel, BorderLayout.EAST);


		setSize( 500, 600 );
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
			if(toID.getText().equals("")){
				JOptionPane.showMessageDialog( this,"Must have a valid client ID in the text box", "", JOptionPane.PLAIN_MESSAGE);
				return;
			}
	
//			else if(!otherClients.contains(toID )){
//				JOptionPane.showMessageDialog( this,"Must have a valid client ID in the text box", "", JOptionPane.PLAIN_MESSAGE);
//				return;
//			}
			
			String msg = encryptMessage(message.getText());
			out.println("m " + toID.getText() + " " + ID + " >>begin<<" + msg );
			message.setText("");
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
				history.setText(null);
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
			sendKey
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
		else if(input.startsWith("m")){
			
			String[] sub = input.split(" ");
			
			String fromID = sub[1];
			
			int index = input.indexOf(">>begin<<");		//size 9
			
			String in = input.substring(index+10);
			
			String msg = decrypt(in);
			history.append("Client " + fromID + ": " + msg + "\n");
		}
		else if(input.startsWith("key")){
			String [] in = input.split(" ");
			
			for(OtherClient o : otherClients){
				if(o.getID().
			}
			
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
		getkey and save
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
	String encryptMessage(String msg){

		String result = "";
		ArrayList<Character> splitMsg = new ArrayList<Character>();
		ArrayList<Character> block = new ArrayList<Character>();
		BigInteger value;
		
		//set up all values
		setP();
		setQ();
		setN();
		setPhi();
		
		setE();
		
		char [] temp = msg.toCharArray();
		for(char c: temp){
			splitMsg.add(c);
		}
		
		char nulChar = 0;
		// Add padding if too small 
		while(splitMsg.size() % BLOCKING_SIZE != 0){
			splitMsg.add(nulChar);
		}
		
		while(!splitMsg.isEmpty()){
			
			block.removeAll(block);
			for(int i = 0; i < BLOCKING_SIZE; i++){
				block.add(splitMsg.remove(0));
			}
			
			value = BigInteger.valueOf(block.get(0)*(int)Math.pow(128, 0) 
					+ block.get(1)*(int)Math.pow(128, 1)
					+ block.get(2)*(int)Math.pow(128, 2)
					+ block.get(3)*(int)Math.pow(128, 3));
			
			
			value = value.modPow(E, N);
			
			result = result + " " + value;
			
		}
				
		return result;

	}
	//________________________________________________________________________//
	void setN(){
		
		N = BigInteger.valueOf(P*Q);
	}
	//________________________________________________________________________//
	void setE(){

		E = BigInteger.valueOf(2);

		while (E.compareTo(PHI) == -1)
		{
			if (gcd(E.intValue(), PHI.intValue())==1)
				break;
			else
				E = E.add(BigInteger.valueOf(1));
		}

	}
	//________________________________________________________________________//
	void setUpPrimeList() throws NumberFormatException, IOException
	{	
		FileReader file = new FileReader("prime.txt");
		BufferedReader read = new BufferedReader(file);
		String line;
		
		while((line = read.readLine()) != null)
		{
			prime.add(Integer.parseInt(line));
		}
		for(int i = 0; i < prime.size(); i++)
		{
			System.out.println("Prime at: " + prime.get(i));
		}
	}
	//________________________________________________________________________//
	void setP() {	
		if(pText.getText() != "" || pText.getText() != null || (pText.equals("")))
		{
			Collections.shuffle(prime);
			P = prime.get(0);
			
		}
		else
		{
			P = Integer.parseInt(pText.getText());	
		}
	}	
	//________________________________________________________________________//
	void setQ(){
		if(qText.getText() != "" || qText.getText() != null || !(qText.equals("")))
		{
			Collections.shuffle(prime);
			Q = prime.get(0);
			if(Q == P)
				Q = prime.get(1);
			
		}
		else
		{			
			Q = Integer.parseInt(qText.getText());
		}
	}	
	//________________________________________________________________________//
	int gcd(int a, int h)
	{
	    int temp;
	    while (true)
	    {
	        temp = a%h;
	        if (temp == 0)
	          return h;
	        a = h;
	        h = temp;
	    }
	}
	//________________________________________________________________________//
	void setD(){
		
		boolean works = false;
		int k = 0;
		
		while(!works){
			D = (BigInteger.ONE.add(BigInteger.valueOf(k).multiply(PHI))).divide(E);
			
			if((E.multiply(D)).mod(PHI).compareTo(BigInteger.ONE) == 0){
				return;
			}
			
			k++;
		}
			
	}
	//________________________________________________________________________//
	void setPhi(){
		
		PHI = BigInteger.valueOf((P-1)*(Q-1));
		
	}
	//________________________________________________________________________//
	//________________________________________________________________________//

	//________________________________________________________________________//
	String decrypt(String msg){
		
		setD();
		String result = "";
		
		String [] temp = msg.split(" ");
		BigInteger tempBig;
		ArrayList<String> splitMsg = new ArrayList<String>();
		
		for(String s: temp){
			System.out.println(s);
			splitMsg.add(s);
		}

		for(String s: splitMsg){
			System.out.println(s);
			BigInteger value = new BigInteger(s);
			tempBig = value.modPow(D, N);	//decrypted value

			ArrayList<BigInteger> tempArr = new ArrayList<BigInteger>();
			for(int i  = 3; i >= 0; i--){

				tempArr.add(0, tempBig.mod(BigInteger.valueOf(128).pow(i)));
			}
			int j = 0;
			for(BigInteger i: tempArr){
				
				String finalChar = i.divide(BigInteger.valueOf(128)).toString();//.pow(j)).toString();
				char finChar = finalChar.charAt(0);
				int ascii = finChar;
				ascii  = ascii << 7;
				//finChar = (char) ascii;
				result = result + (char) ascii;
				
				j++;
			}
			
			
		}
		
		return result;
	}
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

} 




