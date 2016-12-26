// MyDialog.java	Created      : Sun Sep  7 15:35:34 2003
//			Last modified: Sun Sep 07 19:13:06 2003
// Compile: javac MyDialog.java #
// Execute: java MyDialog #
// FTP Directory: sources/java #
//------------------------------------------------------------
//
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;


public class MyDialog extends JDialog {
    String _str = null;
    Dialog _dialog = null;
    JTextField _field = null;
    MyDialog _self = null;
	
    public MyDialog(JFrame owner, String message) 
    {
	super(owner, message, true);
	JLabel l = new JLabel(message);
	_self = this;
	_field = new JTextField();
	_field.setColumns(20);

	_field.addKeyListener(new KeyAdapter() {
		public void keyPressed(KeyEvent e)
		{
		    int code = e.getKeyCode();
		    if (code == KeyEvent.VK_ENTER) {
			_str = _field.getText();
			_self.hide();
		    }
		    else if(code == KeyEvent.VK_ESCAPE) {
			_self.hide();
		    }
		}
	    });
	this.getContentPane().setLayout(new BorderLayout());
	this.getContentPane().add("North", l);
	this.getContentPane().add("Center", _field);
	this.pack();
    }
    
    public String getText() 
    {
	return _str;
    }

    public static String getTextByDialog(JFrame owner, String message)
    {
	JDialog.setDefaultLookAndFeelDecorated(owner.isDefaultLookAndFeelDecorated());
	MyDialog d = new MyDialog(owner, message);
	d.setVisible(true);
	String result = d.getText();
	d.dispose();
	return result;
    }
}
