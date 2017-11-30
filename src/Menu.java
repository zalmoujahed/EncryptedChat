import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;


public class Menu extends JMenuBar{

	private static GUI gui;
	private static JMenu fileMenu, helpMenu; ; // the menus that will be attached to the JManuBar
	private static JMenuItem exitItem; //  items for the "File" menu
	private static JMenuItem  connectionItem, aboutItem; //  items for the "Help" menu
	
	
	public Menu(GUI g){
		gui = g;
	}

	public static JMenu CreateFileMenu(){
		fileMenu = new JMenu( "File" );
		fileMenu.setMnemonic( 'F' );
		exitItem = new JMenuItem( "Exit");
		fileMenu.addSeparator();
		fileMenu.add(exitItem);
		
	    exitItem.addActionListener( new ActionListener() {
	    	public void actionPerformed( ActionEvent event ){
	    		System.exit(0);
	        }
	      }
	    );
	    return fileMenu;
	}

	public static JMenu CreateHelpMenu(){
		helpMenu = new JMenu( "Help" );
		helpMenu.setMnemonic( 'H' );
		aboutItem = new JMenuItem( "About Us" );
		connectionItem = new JMenuItem("Connection Help");
		helpMenu.addSeparator();
		helpMenu.add(aboutItem);
		helpMenu.addSeparator();
		helpMenu.add(connectionItem);
		
		
		connectionItem.addActionListener( new ActionListener() {
	    	public void actionPerformed( ActionEvent event ){
	    		JOptionPane.showMessageDialog( gui,"Fill this in "
		                , "Connection Help", JOptionPane.PLAIN_MESSAGE);

	        }
	      }
	    );
		
	    aboutItem.addActionListener( new ActionListener() {
	    	public void actionPerformed( ActionEvent event ){
	    		JOptionPane.showMessageDialog( gui,"Networked Chat with RSA Encryption\n"+
		                "Authors: Zaynab Almoujahed & Ahmad Atra\n"
		                , "About Us", JOptionPane.PLAIN_MESSAGE);
	        }
	      }
	    );
		return helpMenu;
	}
	
	
}
