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
	private int P;
	private int Q;
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
		
		otherClients = new Vector<OtherClient>();

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
			
			boolean exists = false;
			OtherClient client = null;
			
			for(OtherClient c: otherClients){
				if(c.getID().equals(toID.getText())){
					client = c;
					exists = true;
					break;
				}
			}
			
			if(!exists){
				JOptionPane.showMessageDialog( this,"Client ID must exist in the client list", "", JOptionPane.PLAIN_MESSAGE);
				return;
			}
			
			String msg = encryptMessage(message.getText(), client);
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
				
				setP();
				setQ();
				
				machineName = machineInfo.getText();
				portNum = Integer.parseInt(portInfo.getText());
				echoSocket = new Socket(machineName, portNum );
				out = new PrintWriter(echoSocket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(
						echoSocket.getInputStream()));

				// start a new thread to read from the socket
				new CommunicationReadThread ();
				this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				sendButton.setEnabled(true);
				connected = true;
				history.setText(null);
				connectButton.setText("Disconnect from Server");
				
			} 
			catch (NumberFormatException e) {
				history.insert ( "Server Port must be an integer\n", 0);
			} 
			catch (UnknownHostException e) {
				history.insert("Don't know about host: " + machineName , 0);
			} 
			catch (IOException e) {
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
				this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    		
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
			
			setN();
			setPhi();
			setE();
			setD();
		
			//Send encryption info to server
			out.println("key " + ID + " " + E + " " + N );
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
			history.append("Client " + fromID + " sent: " + msg + "\n");
		}
		
		else if(input.startsWith("key")){
			String [] in = input.split(" ");
			
			for(OtherClient o : otherClients){
				if(o.getID().equals(in[1])){
					o.setE(in[2]);
					o.setN(in[2]);
					break;
				}
			}	
		}
		
		updateClientList();
		
	}
	//________________________________________________________________________//
	void initializeClientList(String input){
		
		String[] otherIDs = input.split(" ");
		int i = 1;
		while(i < otherIDs.length && !otherIDs[i].equals(">>end<<")){
			if(!otherIDs[i].equals(ID)){
				OtherClient c = new OtherClient(otherIDs[i], otherIDs[i+1], otherIDs[i+2]);
				otherClients.add(c);
			}
			i = i+3;
		}
	}
	//________________________________________________________________________//
	void addNewlyConnected(String input){
		String [] in = input.split(" ");
		String id = in[1];
		history.append("Client " + id + " connected to the server\n");
		otherClients.add(new OtherClient(id, in[2], in[3]));
		
	}
	//________________________________________________________________________//
	void removeDisconnected(String input){
		
		String id = input.substring(2);
		for(OtherClient o : otherClients){
			if(id.equals(o.getID())){
				otherClients.remove(o);
				break;
			}
		}
		
	}
	//________________________________________________________________________//
	String encryptMessage(String msg, OtherClient client){

		String result = "";
		ArrayList<Character> splitMsg = new ArrayList<Character>();
		ArrayList<Character> block = new ArrayList<Character>();
		BigInteger value;
		
		//set up all values	
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
			
			value = BigInteger.valueOf(block.get(0)*(int)Math.pow(128, 0));
			value = value.add(BigInteger.valueOf(block.get(1)*(int)Math.pow(128, 1)));
			value = value.add(BigInteger.valueOf(block.get(2)*(int)Math.pow(128, 2)));
			value = value.add(BigInteger.valueOf(block.get(3)*(int)Math.pow(128, 3)));
			
			
			value = value.modPow(new BigInteger(client.getE()), new BigInteger(client.getN()));
						
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
		read.close();
	}
	//________________________________________________________________________//
	void setP() {	
		
		String userInput;
		userInput = JOptionPane.showInputDialog(this, "Please enter a prime number greater than 16410 or cancel for default", "P", JOptionPane.QUESTION_MESSAGE);
		
		if(userInput != null ){
			while(Integer.parseInt(userInput) < 16411 || !isPrime(Integer.parseInt(userInput))){
				
				userInput = JOptionPane.showInputDialog(this, "Value not valid. Enter a prime number greater than 16410 or cancel for default", "P", JOptionPane.QUESTION_MESSAGE);
				if(userInput == null)
					break;
			}
			 
		}
		if(userInput == null){
			Collections.shuffle(prime);
			P = prime.get(0);	
		}
		else
			P = Integer.parseInt(userInput);
		
	}	
	//________________________________________________________________________//
	void setQ(){
		
		String userInput;
		userInput = JOptionPane.showInputDialog(this, "Please enter another prime number greater than 16410 or cancel for default", "Q", JOptionPane.QUESTION_MESSAGE);
		
		if(userInput != null){
			while(Integer.parseInt(userInput) < 16411 || !isPrime(Integer.parseInt(userInput)) || Integer.parseInt(userInput) == P){
				
				userInput = JOptionPane.showInputDialog(this, "Value not valid. Enter a different prime number greater than 16410 or cancel for default", "Q", JOptionPane.QUESTION_MESSAGE);
				if(userInput == null)
					break;
			}
			 
		}
		if(userInput == null){
			Collections.shuffle(prime);
			Q = prime.get(0);	
			if(Q == P)
				Q = prime.get(1);
		}
		else
			Q = Integer.parseInt(userInput);

	}	
	//________________________________________________________________________//
		//prime check taken code taken from https://beginnersbook.com/2014/01/java-program-to-check-prime-number/
		private boolean isPrime(int num) {
			int temp;
			for(int i=2;i<=num/2;i++)
			{
		           temp=num%i;
			   if(temp==0)
			   {
			      return false;
			   }
			}
			return true;
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
	String decrypt(String msg){
		
		setD();
		String result = "";
		
		String [] temp = msg.split(" ");
		ArrayList<BigInteger> splitMsg = new ArrayList<BigInteger>();
		
		for(String s: temp){
			
			splitMsg.add(new BigInteger(s));
		}

		for(BigInteger C: splitMsg){
			
			C = C.modPow(D, N);
			
			while(true){
				if(C.intValue() == 0)
				{
					break;
				}
				
				int value = C.mod(BigInteger.valueOf(128)).intValue();
				
				char c = (char)value;
				
				C = C.shiftRight(7);
				if(c != 0){
					result = result + c;
				}
			}
		}
		
		return result;
	}
	//________________________________________________________________________//
	void updateClientList(){

		clientList.setText(null);
		
		for(OtherClient o: otherClients){
			clientList.append("Client " + o.getID() + "\n");
		}
		
	}
	//________________________________________________________________________//
	class CommunicationReadThread extends Thread
	{ 

		public CommunicationReadThread ()
		{
			start();
			history.append ("Communicating with Port\n");
		}

		public void run()
		{
			System.out.println ("New Communication Thread Started");

			try {          
				String inputLine; 

				while ((inputLine = in.readLine()) != null) 
				{  

					processInput(inputLine);

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




