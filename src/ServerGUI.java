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
  private boolean running;

  // Network Items
  boolean serverContinue;
  ServerSocket serverSocket;
  Vector <PrintWriter> outStreamList;

   // set up ServerGUI
   public ServerGUI()
   {
      super( "Echo Server" );
      
      // set up the shared outStreamList
      outStreamList = new Vector<PrintWriter>();

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

      history = new JTextArea ( 10, 40 );
      history.setEditable(false);
      container.add( new JScrollPane(history) );

      setSize( 500, 250 );
      setVisible( true );

   } // end CountDown constructor

    // handle button event
    public void doButton( ActionEvent event )
    {
       if (running == false)
       {
         new ConnectionThread (this);
       }
       else
       {
         serverContinue = false;
         ssButton.setText ("Start Listening");
         portInfo.setText (" Not Listening ");
       }
    }


 } // end class EchoServer4


class ConnectionThread extends Thread
 {
   ServerGUI ServerGUI;
   
   public ConnectionThread (ServerGUI es3)
   {
     ServerGUI = es3;
     start();
   }
   
   public void run()
   {
     ServerGUI.serverContinue = true;
     
     try 
     { 
       ServerGUI.serverSocket = new ServerSocket(0); 
       ServerGUI.portInfo.setText("Listening on Port: " + ServerGUI.serverSocket.getLocalPort());
       System.out.println ("Connection Socket Created");
       try { 
         while (ServerGUI.serverContinue)
         {
           System.out.println ("Waiting for Connection");
           ServerGUI.ssButton.setText("Stop Listening");
           new CommunicationThread (ServerGUI.serverSocket.accept(), ServerGUI, ServerGUI.outStreamList); 
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
         ServerGUI.serverSocket.close(); 
       }
       catch (IOException e)
       { 
         System.err.println("Could not close port: 10008."); 
         System.exit(1); 
       } 
     }
   }
 }
 

class CommunicationThread extends Thread
{ 
 //private boolean serverContinue = true;
 private Socket clientSocket;
 private ServerGUI ServerGUI;
 private Vector<PrintWriter> outStreamList;



 public CommunicationThread (Socket clientSoc, ServerGUI ec3, 
                             Vector<PrintWriter> oSL)
   {
    clientSocket = clientSoc;
    ServerGUI = ec3;
    outStreamList = oSL;
    ServerGUI.history.insert ("Comminucating with Port" + clientSocket.getLocalPort()+"\n", 0);
    start();
   }

 public void run()
   {
    System.out.println ("New Communication Thread Started");

    try { 
         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), 
                                      true); 
         outStreamList.add(out);
         
         BufferedReader in = new BufferedReader( 
                 new InputStreamReader( clientSocket.getInputStream())); 

         String inputLine;  

         while ((inputLine = in.readLine()) != null) 
             { 
              System.out.println ("Server: " + inputLine); 
              ServerGUI.history.insert (inputLine+"\n", 0);
              
              // Loop through the outStreamList and send to all "active" streams
              //out.println(inputLine); 
              for ( PrintWriter out1: outStreamList )
              {
                System.out.println ("Sending Message");
                out1.println (inputLine);
              }

              if (inputLine.equals("Bye.")) 
                  break; 

              if (inputLine.equals("End Server.")) 
                  ServerGUI.serverContinue = false; 
             } 

         outStreamList.remove(out);
         out.close(); 
         in.close(); 
         clientSocket.close(); 
        } 
    catch (IOException e) 
        { 
         System.err.println("Problem with Communication Server");
         //System.exit(1); 
        } 
    }
} 





 
