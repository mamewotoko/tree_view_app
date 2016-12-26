// TypeAheadTree.java	Created      : Thu Sep 18 05:30:05 2003
//			Last modified: Mon Oct 20 02:56:29 2003
// Compile: javac TypeAheadTree.java #
// Execute: java TypeAheadTree #
// FTP Directory: sources/java #
//------------------------------------------------------------
//
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.tree.*;

public class TypeAheadTree extends JTree {
    public StringBuffer _current_word = new StringBuffer();
    //int _row = -1;

    public TypeAheadTree() 
    {
	super();
	final JTree tree = this;

	this.addKeyListener(new KeyAdapter() {
		public void keyPressed(KeyEvent e)
		{
		    char keychar = e.getKeyChar();
		    String keystr = e.getKeyText(e.getKeyCode());
		    int[] selected_rows = tree.getSelectionRows();

		    if(e.isControlDown() && keystr.equals("G")) {
			//System.out.println("control-G");
			//System.out.println("selected_rows: " + selected_rows);
			//System.out.println("prefix: " + _current_word.toString());
			
			if(selected_rows != null && _current_word.length() > 0) {
			    int row = selected_rows[0] + 1 % tree.getRowCount();
			    
			    TreePath path = tree.getNextMatch(_current_word.toString(), row, Position.Bias.Forward);
			    
			    if(path == null) {
				_current_word.delete(0, _current_word.length());
				System.out.println("Not found");
			    }
			    else {
				System.out.println("found!! " + path);
				tree.clearSelection();
				tree.addSelectionPath(path);
				tree.scrollPathToVisible(path);
			    }
			}
		    }
		    else if((keychar >= 'a' && keychar <= 'z')
		       || (keychar >= 'A' && keychar <= 'Z')) {
			if(selected_rows != null) {
			    _current_word.append(keychar);

			    int row = selected_rows[0];
			    System.out.println("row: " + row);
			    TreePath path = tree.getNextMatch(_current_word.toString(), row, Position.Bias.Forward);
			    
			    if(path == null) {
				_current_word.delete(0, _current_word.length());
			    }
			    else {
				tree.clearSelection();
				tree.addSelectionPath(path);
				tree.scrollPathToVisible(path);
			    }
			}
			else {
			    _current_word.delete(0, _current_word.length());
			}
		    }
		}
	    });
    }

    public void addSelectionPath(TreePath p)
    {
	System.out.println("addSelectionPath called: " + p);
	//Thread.dumpStack();
	super.addSelectionPath(p);
    }

//     public static void main(String argv[])
//     {
//         JFrame.setDefaultLookAndFeelDecorated(true);
//         JFrame f = new JFrame();
//         f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

// 	TypeAheadTree tree = new TypeAheadTree();
// 	JScrollPane scroll = new JScrollPane(tree);
// 	f.getContentPane().add(scroll);

//         f.pack();
//         f.setVisible(true);
//     }
}
