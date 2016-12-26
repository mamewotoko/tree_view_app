// MenuTest.java	Created      : Sun Sep 14 16:22:10 2003
//			Last modified: Sun Sep 14 16:56:33 2003
// Compile: javac MenuTest.java #
// Execute: java MenuTest #
// FTP Directory: sources/java #
//------------------------------------------------------------
// ??? 
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.plaf.basic.*;

public class MenuTest {


    public static void main(String argv[])
    {
	JFrame.setDefaultLookAndFeelDecorated(true);
	final JFrame frame = new JFrame();
	JMenuBar menubar = new JMenuBar();

	JMenu menu = new JMenu("File");
	menubar.add(menu);

	JMenuItem menuitem = menu.add("Open");
	
	menuitem.addMouseListener(new MouseAdapter() {
		public void mousePressed(MouseEvent e) 
		{
		    JFileChooser filechooser = new JFileChooser();
		    //ファイルを選択してくれるまでまつ
		    filechooser.showOpenDialog(frame);
		    
		    File f = filechooser.getSelectedFile();
		    if(f != null) 
			System.out.println("FILE: " + f.getName());
		    filechooser.dispose();
		}
	    });
	frame.setJMenuBar(menubar);
	frame.setSize(200, 200);
	frame.setVisible(true);
    }
}
