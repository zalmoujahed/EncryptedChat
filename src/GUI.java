import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class GUI extends JFrame{

	private Menu menuBar;
	private Container c;
	private JPanel panel;
	private JButton serverButton; 
	private JButton clientButton;
	
	private ServerGUI Server;
	private ClientGUI Client;
	
	public GUI(){
		
		super("Networked Chat");
		this.setLocation(200, 100);
		initializeGUI();
		c = getContentPane();
		c.setLayout(new BorderLayout());
		panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2));
		
		serverButton = new JButton("Connect as Server");
		serverButton.addActionListener( new  ActionListener(){
			public void actionPerformed( ActionEvent event ){
				Server = new ServerGUI();
				serverButton.setEnabled(false);
				Server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    		
	        }
		});
		
		clientButton = new JButton("Connect as Client");
		clientButton.addActionListener( new  ActionListener(){
			public void actionPerformed( ActionEvent event ){
				Client = new ClientGUI();
				Client.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    		
	        }
		});
		
		panel.add(serverButton);
		panel.add(clientButton);
		c.add(panel);
		setVisible(true);
		
		
	}
	public void initializeGUI(){
		menuBar = new Menu(this);
		setJMenuBar(menuBar);
		menuBar.add(Menu.CreateFileMenu());
		menuBar.add(Menu.CreateHelpMenu());
		
		setSize( 500, 300 );

		setVisible( true );
	}
	
	
}
