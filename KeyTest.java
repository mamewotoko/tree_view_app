// KeyTest.java		Created      : Sun Sep  7 14:26:54 2003
//			Last modified: Sun Sep 07 18:58:08 2003
// Compile: javac KeyTest.java #
// Execute: java KeyTest #
// FTP Directory: sources/java #
//------------------------------------------------------------
//
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class KeyTest {


    public static void main(String argv[])
    {
	JFrame f = new JFrame();
	
	f.addKeyListener(new KeyAdapter() {
		public void keyPressed(KeyEvent e) {
		    int mod = e.getModifiers();
		    char ch = e.getKeyChar();
		    System.out.println("Pressed: " + KeyEvent.getKeyText(e.getKeyCode()));
		    System.out.println("Modifiers: " + KeyEvent.getKeyModifiersText(mod));
		    System.out.println("keychar: " + ch);
		    if(ch == '') {
			System.out.println("Control-R (1)");
		    }
		    if(e.isControlDown() 
		       && KeyEvent.getKeyText(e.getKeyCode()).equals("R")) {
			System.out.println("Control-R (2)");
		    }
		}
	    });
	f.setSize(200, 200);
	f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	f.setVisible(true);
    }
}
