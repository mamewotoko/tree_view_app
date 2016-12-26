// TreeEdit.java	Created      : Fri Sep  5 17:23:47 2003
//			Last modified: Sun Sep 21 19:43:04 2003
// Compile: javac TreeEdit.java #
// Execute: java TreeEdit #
// FTP Directory: sources/java #
//------------------------------------------------------------
//
import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.text.html.*;
import javax.swing.text.html.HTML.*;
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;

public class TreeEdit {

    static DefaultMutableTreeNode _tree = null;
    static DefaultTreeModel _treeModel = null;
    private static StringBuffer _content_buffer = new StringBuffer();
    public static boolean loose_mode = true;
    //li を閉じる??
    
    public static boolean printEasyRegexp(DefaultMutableTreeNode node, boolean isPreviousNode)
    {
	String tagname = node.toString();
	if(node.isLeaf()) {
	    if(tagname.equals("hr")) {
		String data = "<hr[^>]*>";
		if(isPreviousNode) {
		    data = "[^<]*" + data;
		}
		System.out.print(data);
		return true;
	    }
	    else {
		//System.out.println("isPreviousNode: " + isPreviousNode);
		if(isPreviousNode) {
		    System.out.print("(.*?)");
		    return false;
		}
		else {
		    return false;
		}
	    }
	}
	else {
	    // TO BE MODIFIED
	    //if(tagname.equals("tr") || tagname.equals("td")) {
	    //open & close
	    boolean display_tag = !tagname.equals("p-implied");
	    boolean is_endopt = false;

	    if(display_tag) {
		String data = "<"+tagname+"[^>]*>";
		if(isPreviousNode) {
		    //previous is tag!
		    data = "[^<]*"+data;
		}
		System.out.print(data);
	    }

	    boolean isPrevChildNode = true;
	    Enumeration enum = node.children();
	    while(enum.hasMoreElements()) {
		DefaultMutableTreeNode current =
		    (DefaultMutableTreeNode)enum.nextElement();
		isPrevChildNode = printEasyRegexp(current, isPrevChildNode);
	    }
	    
	    if(display_tag) {
		String data = "</"+tagname+">";
		if(isPrevChildNode) {
		    // last children is tag!
		    data = "[^<]*" + data;
		}

		if(tagname.equals("li")) {
		    is_endopt = true;
		    //</li> のあとにはテキストはこないのである
		    data = "(" + data + "[^>]*)?";
		}
		System.out.print(data);
	    }
	    //display_tag <=> p-implied
	    return (is_endopt || !display_tag) ? isPrevChildNode : true;
	}
    }
    
    public static DefaultMutableTreeNode getChildTree(TreePath path)
    {
	DefaultMutableTreeNode currentNode =
	    (DefaultMutableTreeNode)(path.getLastPathComponent());
	return currentNode;
    }

    public static void printNode(DefaultMutableTreeNode node)
    {
	System.out.println(node);
	Enumeration enum = node.children();
	while(enum.hasMoreElements()) {
	    DefaultMutableTreeNode current = (DefaultMutableTreeNode)enum.nextElement();
	    printNode(current);
	}
    }

    public static void visit(Document doc, Element e, DefaultMutableTreeNode parent) 
    {
	String name = e.getName();

	if(name.equals("content")) {
	    try {
		_content_buffer.append(doc.getText(e.getStartOffset(), e.getEndOffset() - e.getStartOffset()));
	    }
 	    catch(Exception ex) {}
	}	    
	else if(! name.equals("comment")) {
	    // StringBuffer
	    int size = _content_buffer.length();
	    String content = _content_buffer.toString();
	    _content_buffer.delete(0, size);
	    
	    if(size != 0 && ! content.equals("\n") && !content.equals("\r\n")) {
		DefaultMutableTreeNode child = 
		    new DefaultMutableTreeNode(content);
 		_treeModel.insertNodeInto(child, parent,
 					  parent.getChildCount());
	    }

	    DefaultMutableTreeNode new_parent = new DefaultMutableTreeNode(e.getName());
	    _treeModel.insertNodeInto(new_parent, parent, parent.getChildCount());
	    if(! e.isLeaf()) {
		for(int i = 0; i < e.getElementCount(); i++) {
		    visit(doc, e.getElement(i), new_parent);
		}
	    }

	    size = _content_buffer.length();
	    content = _content_buffer.toString();
	    _content_buffer.delete(0, size);

	    if(size != 0 && ! content.equals("\n") && !content.equals("\r\n")) {
		DefaultMutableTreeNode child = new DefaultMutableTreeNode(content);
 		_treeModel.insertNodeInto(child, parent, parent.getChildCount());
	    }
	}
    }

    public static void main(String[] args) 
    {
	String filename = args.length == 1 ? args[0] : "/home/tak/homepage/index.html";
	try {
	    JFrame.setDefaultLookAndFeelDecorated(true);
	    _tree = new DefaultMutableTreeNode("ROOT");
	    _treeModel = new DefaultTreeModel(_tree);
	    //_treeModel.addTreeModelListener(new MyTreeModelListener());
	    
	    EditorKit kit = new HTMLEditorKit();
	    HTMLDocument doc = (HTMLDocument)kit.createDefaultDocument();

	    doc.putProperty("IgnoreCharsetDirective", Boolean.TRUE);
	    kit.read(new FileReader(filename), doc, 0);
	    Element[] el = doc.getRootElements();

	    for(int i = 0; i < el.length; i++) {
		Element current = el[i];
		visit(doc, current, _tree);
	    }
	    final JTree tree = new JTree(_treeModel);
	    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
	    tree.addMouseListener(new MouseAdapter () {
		    public void mouseClicked(MouseEvent e) {
			TreePath[] paths = tree.getSelectionPaths();
			switch(e.getButton()) {
			case MouseEvent.BUTTON3:
			    if(paths != null) {
				System.out.println("-------------");
				for(int i = 0; i < paths.length; i++) {
				    printNode(getChildTree(paths[i]));	
				}
				System.out.println("-------------");
			    }
			    break;
			case MouseEvent.BUTTON2:
			    if(paths != null) {
				for(int i = 0; i < paths.length; i++) {
				    printEasyRegexp(getChildTree(paths[i]), true);	
				}
			    }
			    System.out.println("");
			    break;
			default:
			    break;
			}
		    }
		});
	    JFrame f = new JFrame();
	    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    JScrollPane scroll = new JScrollPane(tree);
 	    f.getContentPane().add(scroll);	    
	    f.pack();
	    f.setVisible(true);
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
    }
}
