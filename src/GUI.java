import java.awt.*;
import java.util.*;
import javax.swing.*;

public class GUI extends JFrame{

	private Menu menuBar;
	
	public GUI(){
		
		super("Networked Chat");
		this.setLocation(200, 100);
		initializeGUI();
		
	}
	public void initializeGUI(){
		menuBar = new Menu(this);
		setJMenuBar(menuBar);
		menuBar.add(Menu.CreateFileMenu());
		menuBar.add(Menu.CreateHelpMenu());
		
		setSize( 1000, 600 );

		setVisible( true );
	}
	
	
}
