// DeletableTree.java	Created      : Thu Sep 25 10:21:40 2003
//			Last modified: Thu Sep 25 10:32:21 2003
// Compile: javac DeletableTree.java #
// Execute: java DeletableTree #
// FTP Directory: sources/java #
//------------------------------------------------------------
//
import java.io.*;
import java.util.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.tree.*;
import javax.swing.*;

public class DeletableTree extends JTree {
    public DeletableTree() 
    {
	super();
	final JTree treegui = this;

	this.addKeyListener(new KeyAdapter () {
		public void keyPressed(KeyEvent e) 
		{
		    TreePath[] paths = treegui.getSelectionPaths();
		    String keystr = KeyEvent.getKeyText(e.getKeyCode());
		    
		    if(keystr.equals("Delete")) {
			if(paths != null) {
			    DefaultMutableTreeNode current = (DefaultMutableTreeNode)paths[0].getLastPathComponent();
			    DefaultMutableTreeNode next = current.getNextSibling();
			    if(next == null)
				    next = current.getPreviousSibling();
			    if(next == null)
				next = (DefaultMutableTreeNode)current.getParent();
			    
			    if(next != null) {
				//JTree
				((DefaultTreeModel)treegui.getModel()).removeNodeFromParent((MutableTreeNode)current);
				treegui.addSelectionPath(new TreePath(next.getPath()));
			    }
			}
		    }
		}
	    });
    }

    public static void main(String argv[])
    {
	JFrame.setDefaultLookAndFeelDecorated(true);
	JFrame frame = new JFrame();
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.getContentPane().add(new JScrollPane(new DeletableTree()));
	frame.pack();
	frame.setVisible(true);
    }
}
