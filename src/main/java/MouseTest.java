// MouseTest.java	Created      : Sun Sep  7 14:22:43 2003
//			Last modified: Thu Sep 25 11:28:37 2003
// Compile: javac MouseTest.java #
// Execute: java MouseTest #
// FTP Directory: sources/java #
//------------------------------------------------------------
//
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class MouseTest {

    public static void main(String argv[])
    {
	JFrame f = new JFrame();
	f.addMouseListener(new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
		    System.out.println("Clicked: " + e.getButton());
		    if(e.getClickCount() == 2) 
			System.out.println("Double clicked");
		}
	    });
	f.setSize(200, 200);
	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	f.setVisible(true);
    }
}
